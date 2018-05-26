package com.example.xyzreader.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class ImageLoaderHelper {
    private final LruCache<String, Bitmap> imageCache = new LruCache<>(20);
    private ImageLoader imageLoader;

    public ImageLoaderHelper(Context context) {
        final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        final ImageLoader.ImageCache imageCache = new ImageLoader.ImageCache() {
            @Override
            public void putBitmap(String key, Bitmap value) {
                ImageLoaderHelper.this.imageCache.put(key, value);
            }

            @Override
            public Bitmap getBitmap(String key) {
                return ImageLoaderHelper.this.imageCache.get(key);
            }
        };
        imageLoader = new ImageLoader(queue, imageCache);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
