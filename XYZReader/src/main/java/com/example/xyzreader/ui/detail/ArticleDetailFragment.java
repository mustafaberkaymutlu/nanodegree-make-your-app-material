package com.example.xyzreader.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
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
import com.example.xyzreader.util.DrawInsetsFrameLayout;
import com.example.xyzreader.util.ObservableScrollView;
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
    private static final float PARALLAX_FACTOR = 1.25f;

    @Inject
    ImageLoader imageLoader;

    private Cursor cursor;
    private long itemId;
    private View rootView;
    private int mutedColor = 0xFF333333;
    private ObservableScrollView scrollView;
    private DrawInsetsFrameLayout drawInsetsFrameLayout;
    private ColorDrawable statusBarColorDrawable;

    private int topInset;
    private View photoContainerView;
    private ImageView photoView;
    private int scrollY;
    private boolean isCard = false;
    private int statusBarFullOpacityBottom;

    private FragmentListener fragmentListener;

    interface FragmentListener {
        void onUpButtonFloorChanged(long itemId, int upButtonFloor);
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
            itemId = getArguments().getLong(ARG_ITEM_ID);
        }

        isCard = getResources().getBoolean(R.bool.detail_is_card);
        statusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
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
        drawInsetsFrameLayout = rootView.findViewById(R.id.draw_insets_frame_layout);
        drawInsetsFrameLayout.setOnInsetsCallback(insets -> topInset = insets.top);

        scrollView = rootView.findViewById(R.id.scrollview);
        scrollView.setCallbacks(() -> {
            scrollY = scrollView.getScrollY();
            fragmentListener.onUpButtonFloorChanged(itemId, getUpButtonFloor());
            photoContainerView.setTranslationY((int) (scrollY - scrollY / PARALLAX_FACTOR));
            updateStatusBar();
        });

        photoView = rootView.findViewById(R.id.photo);
        photoContainerView = rootView.findViewById(R.id.photo_container);

        statusBarColorDrawable = new ColorDrawable(0);

        rootView.findViewById(R.id.share_fab)
                .setOnClickListener(view -> startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share))));

        bindViews();
        updateStatusBar();
        return rootView;
    }

    private void updateStatusBar() {
        final int color;

        if (photoView != null && topInset != 0 && scrollY > 0) {
            final float f = progress(scrollY,
                    statusBarFullOpacityBottom - topInset * 3,
                    statusBarFullOpacityBottom - topInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mutedColor) * 0.9),
                    (int) (Color.green(mutedColor) * 0.9),
                    (int) (Color.blue(mutedColor) * 0.9));
        } else {
            color = 0;
        }

        statusBarColorDrawable.setColor(color);
        drawInsetsFrameLayout.setInsetBackground(statusBarColorDrawable);
    }

    private static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    private static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (rootView == null) {
            return;
        }

        TextView titleView = rootView.findViewById(R.id.textViewArticleTitle);
        TextView bylineView = rootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = rootView.findViewById(R.id.article_body);

        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (cursor != null) {
            rootView.setAlpha(0);
            rootView.setVisibility(View.VISIBLE);
            rootView.animate().alpha(1);
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
                                updateStatusBar();
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

    public int getUpButtonFloor() {
        if (photoContainerView == null || photoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return isCard
                ? (int) photoContainerView.getTranslationY() + photoView.getHeight() - scrollY
                : photoView.getHeight() - scrollY;
    }
}
