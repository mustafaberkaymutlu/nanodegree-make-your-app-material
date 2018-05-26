package com.example.xyzreader.di;

import com.example.xyzreader.XyzApp;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {SingletonModule.class,
        ActivityBuilderModule.class,
        AndroidInjectionModule.class})
public interface SingletonComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(XyzApp app);

        SingletonComponent build();
    }

    void inject(XyzApp app);

}
