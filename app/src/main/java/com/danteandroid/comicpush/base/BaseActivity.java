package com.danteandroid.comicpush.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;

import com.bugtags.library.Bugtags;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.utils.Database;

import butterknife.ButterKnife;
import io.realm.Realm;
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
    private Realm realm;
    public Database database;

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
        realm = Realm.getDefaultInstance();
        database = Database.getInstance(realm);
    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
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

    @Override
    protected void onResume() {
        super.onResume();
        Bugtags.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Bugtags.onPause(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Bugtags.onDispatchTouchEvent(this, event);
        return super.dispatchTouchEvent(event);
    }

    public int color(int colorRes){
        return ContextCompat.getColor(getApplicationContext(), colorRes);
    }

}
