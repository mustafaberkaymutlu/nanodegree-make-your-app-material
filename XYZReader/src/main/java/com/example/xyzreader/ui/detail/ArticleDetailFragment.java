package com.example.xyzreader.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.list.ArticleListActivity;
import com.example.xyzreader.util.DateUtil;
import com.example.xyzreader.util.Preconditions;

import java.util.Date;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_ITEM_ID = "item_id";

    @Inject
    ImageLoader imageLoader;

    private Cursor cursor;
    private long itemId;
    private int mutedColor = 0xFF333333;

    private View viewMetaBar;
    private ImageView photoView;
    private TextView textViewTitle;
    private TextView textViewByLine;
    private TextView textViewBody;

    private Toolbar toolbar;

    public static ArticleDetailFragment newInstance(long itemId) {
        final ArticleDetailFragment fragment = new ArticleDetailFragment();
        final Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = Preconditions.checkNotNull(getArguments(),
                "Arguments must not be null. ");
        if (args.containsKey(ARG_ITEM_ID)) {
            itemId = args.getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        toolbar = view.findViewById(R.id.toolbar);
        appCompatActivity.setSupportActionBar(toolbar);
        final ActionBar ab = appCompatActivity.getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        photoView = view.findViewById(R.id.imageViewPhoto);
        textViewTitle = view.findViewById(R.id.textViewArticleTitle);
        textViewByLine = view.findViewById(R.id.article_byline);
        textViewByLine.setMovementMethod(new LinkMovementMethod());
        textViewBody = view.findViewById(R.id.article_body);
        viewMetaBar = view.findViewById(R.id.meta_bar);

        view.findViewById(R.id.fabShare).setOnClickListener(view2 -> share());
    }

    private void bindViews() {
        if (cursor != null) {
            final String title = cursor.getString(ArticleLoader.Query.TITLE);
            textViewTitle.setText(title);
            toolbar.setTitle(title);

            final Date publishedDate = DateUtil.parsePublishedDate(cursor.getString(ArticleLoader.Query.PUBLISHED_DATE));
            if (DateUtil.isBefore1902(publishedDate)) {
                // If date is before 1902, just show the string
                textViewByLine.setText(Html.fromHtml(
                        DateUtil.formatOutput(publishedDate) + " by <font color='#ffffff'>"
                                + cursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
            } else {
                textViewByLine.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + cursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
            }

            final String body = cursor.getString(ArticleLoader.Query.BODY);
            textViewBody.setText(Html.fromHtml(body.replaceAll("(\r\n|\n)", "<br />")));

            imageLoader.get(cursor.getString(ArticleLoader.Query.PHOTO_URL),
                    new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            final Bitmap bitmap = imageContainer.getBitmap();

                            if (bitmap == null) {
                                return;
                            }

                            new Palette.Builder(bitmap).generate(palette -> {
                                mutedColor = palette.getDarkMutedColor(0xFF333333);
                                photoView.setImageBitmap(imageContainer.getBitmap());
                                viewMetaBar.setBackgroundColor(mutedColor);
                            });
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            // no-op
                        }
                    });
        } else {
            clearViews();
        }
    }

    private void clearViews() {
        textViewTitle.setText("N/A");
        textViewByLine.setText("N/A");
        textViewBody.setText("N/A");
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor receivedDataCursor) {
        if (!isAdded()) {
            receivedDataCursor.close();
            return;
        }

        cursor = receivedDataCursor;

        if (!cursor.moveToFirst()) {
            Timber.e("Error reading item detail cursor");
            cursor.close();
            cursor = null;
            clearViews();
            return;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        cursor = null;
        clearViews();
    }

    private void share() {
        if (cursor == null || !cursor.moveToFirst()) {
            Timber.e("Cannot share, data is not available. ");
            return;
        }

        final String title = cursor.getString(ArticleLoader.Query.TITLE);
        final Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText(getString(R.string.share_text, title))
                .getIntent();
        final Intent chooserIntent = Intent.createChooser(shareIntent, getString(R.string.action_share));
        startActivity(chooserIntent);
    }
}
