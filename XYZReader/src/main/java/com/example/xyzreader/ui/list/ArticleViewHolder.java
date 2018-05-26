package com.example.xyzreader.ui.list;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.DynamicHeightNetworkImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import timber.log.Timber;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private static final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private final ImageLoader imageLoader;

    private final DynamicHeightNetworkImageView thumbnailView;
    private final TextView titleView;
    private final TextView subtitleView;

    ArticleViewHolder(View view, ImageLoader imageLoader, ItemClickListener itemClickListener) {
        super(view);

        this.imageLoader = imageLoader;

        thumbnailView = view.findViewById(R.id.thumbnail);
        titleView = view.findViewById(R.id.article_title);
        subtitleView = view.findViewById(R.id.article_subtitle);

        view.setOnClickListener(v -> itemClickListener.onItemClicked(getAdapterPosition()));
    }

    public void bind(Cursor cursor) {
        titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));

        final Date publishedDate = parsePublishedDate(cursor.getString(ArticleLoader.Query.PUBLISHED_DATE));

        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            subtitleView.setText(Html.fromHtml(
                    OUTPUT_FORMAT.format(publishedDate)
                            + "<br/>" + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)));
        }
        thumbnailView.setImageUrl(
                cursor.getString(ArticleLoader.Query.THUMB_URL),
                imageLoader);
        thumbnailView.setAspectRatio(cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
    }


    private Date parsePublishedDate(String date) {
        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException ex) {
            Timber.e(ex);
            Timber.i("passing today's date");
            return new Date();
        }
    }
}
