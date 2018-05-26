package com.example.xyzreader.di;

import com.example.xyzreader.di.scope.ActivityScoped;
import com.example.xyzreader.ui.detail.ArticleDetailActivity;
import com.example.xyzreader.ui.detail.ArticleDetailActiviyModule;
import com.example.xyzreader.ui.list.ArticleListActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityBuilderModule {

    @ActivityScoped
    @ContributesAndroidInjector
    abstract ArticleListActivity contributeArticleListActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = ArticleDetailActiviyModule.class)
    abstract ArticleDetailActivity contributeArticleDetailActivity();

}
