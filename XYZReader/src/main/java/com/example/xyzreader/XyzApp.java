package com.example.xyzreader;

import android.app.Activity;
import android.app.Application;

import com.example.xyzreader.di.DaggerSingletonComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import timber.log.Timber;

/**
 * Created by Mustafa Berkay Mutlu on 23.05.18.
 */
public class XyzApp extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        initSingletonComponent();


        Timber.plant(new Timber.DebugTree());
    }

    private void initSingletonComponent() {
        DaggerSingletonComponent.builder()
                .application(this)
                .build()
                .inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
