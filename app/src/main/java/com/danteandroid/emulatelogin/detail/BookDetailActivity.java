package com.danteandroid.emulatelogin.detail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.blankj.utilcode.utils.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.danteandroid.emulatelogin.Constants;
import com.danteandroid.emulatelogin.R;
import com.danteandroid.emulatelogin.base.BaseActivity;
import com.danteandroid.emulatelogin.main.Book;
import com.danteandroid.emulatelogin.net.DataFetcher;

import java.util.List;

import butterknife.BindView;
import rx.Observable;

/**
 * Created by yons on 17/11/23.
 */

public class BookDetailActivity extends BaseActivity {
    private static final String TAG = "BookDetailActivity";
    @BindView(R.id.cover)
    ImageView cover;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsingToolbar)
    SubtitleCollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private String title;
    private String coverUrl;
    private boolean collapsed;
    private String author;
    private String link;


    @Override
    protected int initLayoutId() {
        return R.layout.bookdetail_activity;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        fetchData(getIntent());
    }

    private void fetchData(Intent intent) {
        if (intent == null) return;
        title = intent.getExtras().getString(Constants.TITLE);
        author = intent.getExtras().getString(Constants.AUTHOR);
        coverUrl = intent.getExtras().getString(Constants.COVER);
        link = intent.getExtras().getString(Constants.LINK);
        loadCover();
        initToolbar();
        fetch();
    }

    private void fetch() {
        Observable<Book> books = DataFetcher.getInstance(link).fetchDetail();
    }

    private void updateUI(Observable<List<Book>> data) {
        data.compose(applySchedulers())
                .subscribe(books -> {
                    progress.setVisibility(View.GONE);
                    Log.d(TAG, "call: " + books.size());


                }, throwable -> {
                    ToastUtils.showShortToast(throwable.getMessage());
                    progress.setVisibility(View.GONE);

                });
    }

    private void initToolbar() {
        collapsingToolbar.setTitle(title);
        collapsingToolbar.setSubtitle(author);
        appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            collapsed = verticalOffset == 0;
            if (collapsed) {
            }
        });

    }

    private void loadCover() {
        ViewCompat.setTransitionName(cover, title);
        Glide.with(this).load(coverUrl)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        supportStartPostponedEnterTransition();
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        cover.setImageBitmap(resource);
                        supportStartPostponedEnterTransition();
                        fab.animate().scaleX(1).scaleY(1).setStartDelay(500).start();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                getWindow().getEnterTransition().addListener(new TransitionListenerAdapter() {
                                    @Override
                                    public void onTransitionEnd(Transition transition) {
                                        super.onTransitionEnd(transition);
                                    }
                                });
                        }
                    }
                });

    }

}
