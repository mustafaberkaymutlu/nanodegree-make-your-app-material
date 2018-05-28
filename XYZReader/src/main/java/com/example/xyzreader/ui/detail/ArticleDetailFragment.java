package com.example.xyzreader.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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
    private View rootView;
    private int mutedColor = 0xFF333333;

    private ImageView photoView;

    private FragmentListener fragmentListener;

    interface FragmentListener {

    }

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

        try {
            fragmentListener = (FragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().toString() + " must implement FragmentListener. ");
        }
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
        rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        photoView = rootView.findViewById(R.id.imageViewPhoto);

        rootView.findViewById(R.id.fabShare).setOnClickListener(view -> share());

        bindViews();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        final Toolbar toolbar = view.findViewById(R.id.toolbar);
        appCompatActivity.setSupportActionBar(toolbar);
        final ActionBar ab = appCompatActivity.getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void share() {
        final Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText("Some sample text")
                .getIntent();
        final Intent chooserIntent = Intent.createChooser(shareIntent, getString(R.string.action_share));
        startActivity(chooserIntent);
    }

    private void bindViews() {
        if (rootView == null) {
            return;
        }

        TextView titleView = rootView.findViewById(R.id.textViewArticleTitle);
        TextView bylineView = rootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = rootView.findViewById(R.id.article_body);

        if (cursor != null) {
            rootView.setVisibility(View.VISIBLE);

            titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
            final Date publishedDate = DateUtil.parsePublishedDate(cursor.getString(ArticleLoader.Query.PUBLISHED_DATE));
            if (DateUtil.isBefore1902(publishedDate)) {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        DateUtil.formatOutput(publishedDate) + " by <font color='#ffffff'>"
                                + cursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
            } else {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + cursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
            }

            final String body = cursor.getString(ArticleLoader.Query.BODY);
            bodyView.setText(Html.fromHtml(body.replaceAll("(\r\n|\n)", "<br />")));

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
                                rootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mutedColor);
                            });
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            // no-op
                        }
                    });
        } else {
            rootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        this.cursor = cursor;
        if (this.cursor != null && !this.cursor.moveToFirst()) {
            Timber.e("Error reading item detail cursor");
            this.cursor.close();
            this.cursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        cursor = null;
        bindViews();
    }
}
