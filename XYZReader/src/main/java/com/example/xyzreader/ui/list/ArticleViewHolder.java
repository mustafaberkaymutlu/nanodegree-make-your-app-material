package com.example.xyzreader.ui.list;

import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.DateUtil;

import java.util.Date;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
    private final ImageLoader imageLoader;

    private final ConstraintLayout constraintLayoutContainer;
    private final NetworkImageView imageViewThumbnail;
    private final TextView textViewTitle;
    private final TextView textViewSubtitle;

    ArticleViewHolder(View view, ImageLoader imageLoader, ItemClickListener itemClickListener) {
        super(view);

        this.imageLoader = imageLoader;

        this.constraintLayoutContainer = view.findViewById(R.id.constraintLayoutContainer);
        this.imageViewThumbnail = view.findViewById(R.id.imageViewThumbnail);
        this.textViewTitle = view.findViewById(R.id.textViewArticleTitle);
        this.textViewSubtitle = view.findViewById(R.id.textViewArticleSubtitle);

        view.setOnClickListener(v -> itemClickListener.onItemClicked(getAdapterPosition()));
    }

    public void bind(Cursor cursor) {
        textViewTitle.setText(cursor.getString(ArticleLoader.Query.TITLE));

        final Date publishedDate = DateUtil.parsePublishedDate(cursor.getString(ArticleLoader.Query.PUBLISHED_DATE));

        if (DateUtil.isBefore1902(publishedDate)) {
            textViewSubtitle.setText(Html.fromHtml(
                    DateUtil.formatOutput(publishedDate)
                            + "<br/>" + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            textViewSubtitle.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)));
        }
        imageViewThumbnail.setImageUrl(
                cursor.getString(ArticleLoader.Query.THUMB_URL),
                imageLoader);

        final float aspectRatio = cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);
        setImageViewThumbnailAspectRatio(aspectRatio);
    }

    private void setImageViewThumbnailAspectRatio(float aspectRatio) {
        final ConstraintSet set = new ConstraintSet();
        set.clone(constraintLayoutContainer);
        set.setDimensionRatio(imageViewThumbnail.getId(), "1:" + aspectRatio);
        set.applyTo(constraintLayoutContainer);
    }
}
