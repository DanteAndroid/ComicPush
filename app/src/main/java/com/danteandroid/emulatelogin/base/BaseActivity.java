package com.danteandroid.emulatelogin.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.danteandroid.emulatelogin.R;

import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by yons on 17/11/22.
 */

public abstract class BaseActivity extends AppCompatActivity {
    public String TAG = getClass().getName();
    public CompositeSubscription compositeSubscription = new CompositeSubscription();
    private ProgressDialog dialog;
    public Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews(savedInstanceState);
    }

    @CallSuper
    protected void initViews(Bundle savedInstanceState) {
        setContentView(initLayoutId());
        ButterKnife.bind(this);
        initAppBar();
        initSDK();
    }

    private void initSDK() {

    }


    public void initAppBar() {
        toolbar = findViewById(R.id.toolbar);
        if (null != toolbar) {
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(needNavigation());
            if (needNavigation() && toolbar != null) {
                toolbar.setNavigationOnClickListener(v -> onBackPressed());
            }
        }
    }

    protected boolean needNavigation() {
        return true;
    }


    protected abstract int initLayoutId();

    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    public <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinct();
    }
    public void showProgress() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.show();
    }

    public void hideProgress() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
