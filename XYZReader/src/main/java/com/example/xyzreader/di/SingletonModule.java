package com.example.xyzreader.di;

import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.XyzApp;
import com.example.xyzreader.util.ImageLoaderHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SingletonModule {

    @Singleton
    @Provides
    ImageLoaderHelper provideImageLoaderHelper(XyzApp app) {
        return new ImageLoaderHelper(app);
    }

    @Singleton
    @Provides
    ImageLoader provideImageLoader(ImageLoaderHelper imageLoaderHelper) {
        return imageLoaderHelper.getImageLoader();
    }

}
