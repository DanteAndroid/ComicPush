package com.danteandroid.comicpush.detail;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.blankj.utilcode.utils.ConvertUtils;
import com.blankj.utilcode.utils.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.MainActivity;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.base.BaseActivity;
import com.danteandroid.comicpush.model.Book;
import com.danteandroid.comicpush.model.Volume;
import com.danteandroid.comicpush.net.API;
import com.danteandroid.comicpush.net.DataFetcher;
import com.danteandroid.comicpush.net.HttpErrorAction;
import com.danteandroid.comicpush.net.NetService;
import com.danteandroid.comicpush.utils.AppUtil;
import com.danteandroid.comicpush.utils.SpUtil;
import com.danteandroid.comicpush.utils.TextUtil;
import com.danteandroid.comicpush.utils.UiUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yons on 17/11/23.
 */

public class BookDetailActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "BookDetailActivity";
    @BindView(R.id.cover)
    ImageView cover;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsingToolbar)
    SubtitleCollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.title)
    TextView tvTitle;
    @BindView(R.id.author)
    TextView tvAuthor;
    @BindView(R.id.desc)
    TextView tvDesc;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.push)
    Button push;
    List<Volume> checked;
    @BindView(R.id.bookState)
    TextView bookState;
    @BindView(R.id.publishState)
    TextView publishState;
    @BindView(R.id.bookInSiteState)
    TextView bookInSiteState;
    @BindView(R.id.rate)
    TextView rate;
    @BindView(R.id.rateNumber)
    TextView rateNumber;
    @BindView(R.id.fen)
    TextView fen;
    @BindView(R.id.douban)
    TextView douban;
    @BindView(R.id.selectAll)
    CheckBox selectAll;
    @BindView(R.id.selectReverse)
    CheckBox selectReverse;
    @BindView(R.id.pushLayout)
    LinearLayout pushLayout;
    private String title;
    private String coverUrl;
    private boolean expand;
    private String author;
    private String link;
    private int bookId;
    private BookDetailAdapter adapter;
    private String pushMessage;

    @OnClick(R.id.selectAll)
    void onClick(View v) {
        CheckBox checkBox = (CheckBox) v;
        adapter.selectAll(checkBox.isChecked());
        sumPushSize();
    }

    @OnClick(R.id.selectReverse)
    void onClick() {
        adapter.selectReverse();
        sumPushSize();

    }

    @Override
    protected int initLayoutId() {
        return R.layout.bookdetail_activity;
    }

    private void initMain() {
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setColorSchemeColors(color(R.color.colorPrimary),
                color(R.color.colorPrimaryDark), color(R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(this);
//        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);
        adapter = new BookDetailAdapter(this);
        recyclerView.setAdapter(adapter);
        push.setOnClickListener(v -> new AlertDialog.Builder(BookDetailActivity.this)
                .setMessage(pushMessage)
                .setPositiveButton(R.string.push, (dialog, which) -> pushAll())
                .show());
        fab.setOnClickListener(v -> {
            followOrFavorite(false);
            if (!SpUtil.getBoolean(Constants.HAS_HINT)) {
                SpUtil.save(Constants.HAS_HINT, true);
                ToastUtils.showLongToast(R.string.subscribe_hint);
            }
        });
        fab.setOnLongClickListener(v -> {
            followOrFavorite(true);
            return true;
        });

        Space space = new Space(this);
        adapter.addFooterView(space);
        ViewGroup.LayoutParams params = space.getLayoutParams();
        params.height = ConvertUtils.dp2px(36);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        space.setLayoutParams(params);
    }

    private void showPushButton(boolean show) {
        pushLayout.animate().scaleX(show ? 1 : 0).start();
    }


    private void followOrFavorite(boolean isFollow) {
        NetService.request().followOrFavorite(bookId, isFollow ? 1 : 0, 1)
                .compose(applySchedulers())
                .subscribe(responseBody -> {
                    String data;
                    try {
                        data = responseBody.string();
                        if (data.contains("c102")) {
                            UiUtils.showSnack(push, isFollow ? R.string.subscribe_success : R.string.collect_success);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        ToastUtils.showShortToast(errorMessage);
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void sumPushSize() {
        checked = adapter.getChecked();
        float s = 0;
        for (int i = 0; i < checked.size(); i++) {
            s += Float.parseFloat(checked.get(i).size);
        }
        int size = Math.round(s);
        pushMessage = String.format(Locale.getDefault(), getString(R.string.push_select_comics), checked.size(), size);
    }

    private void pushAll() {
        String[] volList = new String[checked.size()];
        for (int i = 0; i < checked.size(); i++) {
            volList[i] = checked.get(i).value;
        }
        NetService.request().push(bookId, volList)
                .compose(applySchedulers())
                .subscribe(responseBody -> {
                    String data;
                    try {
                        data = responseBody.string();
                        if (data.contains("c100")) {
                            UiUtils.showSnack(push, R.string.push_success);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }


    @Override
    protected void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        initMain();
        fetchData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fetchData(intent);
    }

    private void fetchData(Intent intent) {
        if (intent.getExtras() == null) return;
        title = intent.getExtras().getString(Constants.TITLE);
        author = intent.getExtras().getString(Constants.AUTHOR);
        coverUrl = intent.getExtras().getString(Constants.COVER);
        bookId = intent.getExtras().getInt(Constants.BOOK_ID);
        loadCover();
        initToolbar();
        Book book = database.findBook(bookId);
        if (book == null) {
            fetch();
        } else {
            updateUI(book);
        }
    }

    private void fetch() {
        swipeRefresh.setRefreshing(true);

        DataFetcher.getInstance(bookId).fetchDetail()
                .compose(applySchedulers())
                .subscribe(b -> {
                    swipeRefresh.setRefreshing(false);
                    b.title = title;
                    b.author = author;
                    b.bookId = bookId;
                    database.save(b);
                    updateUI(b);

                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        ToastUtils.showShortToast(errorMessage);
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void updateUI(Book b) {
        populate(b);
        adapter.setNewData(b.volList);
    }

    private void populate(Book book) {
        tvDesc.setText(book.desc);
        tvDesc.setOnClickListener(v -> UiUtils.showDetailDialog(BookDetailActivity.this, book.desc));
        tvAuthor.setText(book.author);
        tvAuthor.setOnClickListener(v -> {
            Intent author = new Intent(getApplicationContext(), MainActivity.class);
//            author.setAction(ACTION_FETCH_AUTHOR);
            author.putExtra(Constants.QUERY, TextUtil.removeBrace(book.author));
            startActivity(author);
        });
        tvTitle.setText(book.title);
        bookState.setText(book.bookState);
        bookInSiteState.setText(book.bookInSiteState);
        publishState.setText(book.publishState);
        rate.setText(book.rate);
        fen.setVisibility(View.VISIBLE);
        rateNumber.setText(String.format("（%s）", book.rateNumber));
        douban.setVisibility(View.VISIBLE);
        douban.setOnClickListener(v -> {
            String link = String.format(API.DOUBAN, book.title);
            AppUtil.openBrowser(BookDetailActivity.this, link);
        });

    }

    private void initToolbar() {
        appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            expand = verticalOffset > -100;
            if (expand) {
                collapsingToolbar.setTitle("");
                collapsingToolbar.setSubtitle("");
            } else {
                collapsingToolbar.setTitle(title);
                collapsingToolbar.setSubtitle(author);
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
                        new Handler().postDelayed(() -> fab.show(), 400);

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


    @Override
    public void finishAfterTransition() {
        if (expand) {
            fab.setVisibility(View.GONE);
            super.finishAfterTransition();
        } else {
            finish();
        }
    }


    @Override
    public void onRefresh() {
        if (checked != null) checked.clear();

        push.animate().scaleX(0).start();
        fetch();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            showPushButton(true);
        } else {
            if (adapter.getChecked().size() <= 0) {
                showPushButton(false);
            }
        }
        sumPushSize();
    }
}
