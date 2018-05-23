package com.example.xyzreader;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Mustafa Berkay Mutlu on 23.05.18.
 */
public class XyzApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
