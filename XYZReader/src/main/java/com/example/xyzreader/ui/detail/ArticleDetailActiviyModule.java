package com.example.xyzreader.ui.detail;

import com.example.xyzreader.di.scope.FragmentScoped;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ArticleDetailActiviyModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract ArticleDetailFragment contributeArticleDetailFragment();

}
