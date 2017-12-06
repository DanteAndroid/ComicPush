package com.danteandroid.comicpush.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Space;
import android.widget.TextView;

import com.blankj.utilcode.utils.ConvertUtils;
import com.blankj.utilcode.utils.ToastUtils;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.MainActivity;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.base.BaseActivity;
import com.danteandroid.comicpush.custom.BottomDialogFragment;
import com.danteandroid.comicpush.model.Book;
import com.danteandroid.comicpush.model.Comment;
import com.danteandroid.comicpush.model.Volume;
import com.danteandroid.comicpush.net.API;
import com.danteandroid.comicpush.net.DataFetcher;
import com.danteandroid.comicpush.net.DownloadHelper;
import com.danteandroid.comicpush.net.HttpErrorAction;
import com.danteandroid.comicpush.net.NetService;
import com.danteandroid.comicpush.utils.AppUtil;
import com.danteandroid.comicpush.utils.Imager;
import com.danteandroid.comicpush.utils.SpUtil;
import com.danteandroid.comicpush.utils.TextUtil;
import com.danteandroid.comicpush.utils.UiUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.RealmList;
import rx.Observable;

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
    @BindView(R.id.commentRecyclerView)
    RecyclerView commentRecyclerView;
    @BindView(R.id.layout_detail)
    LinearLayout layoutDetail;
    @BindView(R.id.showAll)
    TextView showAll;
    @BindView(R.id.root)
    CoordinatorLayout root;
    @BindView(R.id.writeComment)
    TextView writeComment;
    private String title;
    private String coverUrl;
    private boolean expand;
    private String author;
    private String link;
    private int bookId;
    private BookVolumeAdapter adapter;
    private CommentAdapter commentAdapter;
    private String pushMessage;
    private boolean isExpand;
    private Book book;
    private int page = 1;
    private BottomDialogFragment commentFragment;
    private String commentTemp;
    private boolean isScrolling;

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
        adapter = new BookVolumeAdapter(this);
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

        recyclerView.addOnItemTouchListener(new OnItemLongClickListener() {
            @Override
            public void onSimpleItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (!SpUtil.getBoolean("download_hint")) {
                    UiUtils.showSnackLong(recyclerView, "下载epub漫画为试验性功能，由于网站有下载次数限制，所以请尽量一次下载一本（最多3本），否则可能无法下载");
                    SpUtil.save("download_hint", true);
                }
                Volume volume = (Volume) adapter.getItem(position);
                if (volume != null) {
                    DownloadHelper.getInstance(BookDetailActivity.this, bookId).downloadToSD(volume.downUrl, volume.title);
                }
            }
        });

        commentAdapter = new CommentAdapter();
        commentAdapter.disableLoadMoreIfNotFullPage(commentRecyclerView);
        commentAdapter.setOnLoadMoreListener(() -> {
            if (commentAdapter.getData().size() > 0) {
                page++;
            }
            moreComment();
        }, recyclerView);
        commentRecyclerView.setAdapter(commentAdapter);
    }

    private void writeComment() {
        commentFragment = BottomDialogFragment.create(R.layout.comment_layout).with(this)
                .bindView(v -> {
                    TextInputLayout textInputLayout = v.findViewById(R.id.commentTextInputLayout);
                    TextInputLayout titleInputLayout = v.findViewById(R.id.titleTextInputLayout);
                    ImageView commit = v.findViewById(R.id.commit);
                    RatingBar ratingBar = v.findViewById(R.id.ratingBar);
                    EditText commentEt = textInputLayout.getEditText();
                    EditText titleEt = titleInputLayout.getEditText();
                    assert commentEt != null;
                    assert titleEt != null;
                    cacheComment(commentEt);
                    commit.setOnClickListener(view -> {
                        if (commentEt.getText() == null || commentEt.getText().toString().trim().isEmpty()
                                || titleEt.getText() == null || titleEt.getText().toString().trim().isEmpty()
                                || commentEt.getText().toString().length() < 30
                                || titleEt.getText().toString().length() < 4) {
                            UiUtils.showSnack(view, R.string.no_text_entered);
                        } else {
                            String comment = commentEt.getText().toString();
                            String title = titleEt.getText().toString();
                            post(title, comment, Math.round(ratingBar.getRating()));
                        }
                    });
                }).listenDismiss(dialog -> {
                    if (!TextUtils.isEmpty(commentTemp)) {
                        UiUtils.showSnack(commentRecyclerView, getString(R.string.content_saved_as_draft));
                    }

                });
        commentFragment.show();
    }

    private void post(String title, String comment, int score) {
        Log.d(TAG, "post: " + title + " " + comment + "--" + score);
        NetService.request().comment(bookId, title, comment, score)
                .compose(applySchedulers())
                .subscribe(responseBody -> {
                    String data = "";
                    try {
                        data = responseBody.string();
                        if (data.contains("c100")) {
                            UiUtils.showSnack(commentRecyclerView, R.string.comment_success);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //1小時只能發表一次
                    Log.d(TAG, "call: " + data);
                    commentTemp = null;
                    commentFragment.dismiss();
                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        commentFragment.dismiss();
                        ToastUtils.showShortToast(errorMessage);
                    }
                });
    }

    private void cacheComment(EditText editText) {
        if (!TextUtils.isEmpty(commentTemp)) {
            editText.append(commentTemp);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                commentTemp = s.toString();
            }
        });
    }

    private void showPushButton(boolean show) {
        pushLayout.setVisibility(View.VISIBLE);
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
        StringBuilder volList = new StringBuilder();
        for (int i = 0; i < checked.size(); i++) {
            volList.append(checked.get(i).value)
                    .append(i == checked.size() - 1 ? "" : ",");
        }
        NetService.request().push(bookId, volList.toString())
                .compose(applySchedulers())
                .subscribe(responseBody -> {
                    String data;
                    try {
                        data = responseBody.string();
                        if (data.contains("c100")) {
                            UiUtils.showSnack(push, R.string.push_success);
                        } else if (data.contains("e403")) {
                            ToastUtils.showShortToast(R.string.push_failed);
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
        bookId = intent.getExtras().getInt(Constants.BOOK_ID);
        title = intent.getExtras().getString(Constants.TITLE);
        coverUrl = intent.getExtras().getString(Constants.COVER);
        initToolbar(title, "");

        if (coverUrl != null) {
            ViewCompat.setTransitionName(cover, coverUrl);
            loadCover(coverUrl);
        }
        book = database.findBook(bookId);
        if (book == null) {
            fetch();
        } else {
            updateUI();
        }
    }

    private void fetch() {
        swipeRefresh.setRefreshing(true);

        DataFetcher.getInstance(bookId, 0).fetchDetail()
                .compose(applySchedulers())
                .zipWith(fetchComment(), (book, comments) -> {
                    book.bookId = bookId;
                    BookDetailActivity.this.book = book;
                    book.commentList = comments;
                    return book;
                })
                .compose(applySchedulers())
                .subscribe(b -> {
                    database.save(b);
                    updateUI();
                    swipeRefresh.setRefreshing(false);

                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        ToastUtils.showShortToast(errorMessage);
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void updateUI() {
        populate(book);
        adapter.setNewData(book.volList);
        popComment();
    }

    private void popComment() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            TransitionManager.beginDelayedTransition(root);
//        }
        if (book.commentList == null || book.commentList.isEmpty()) {
            commentRecyclerView.setVisibility(View.GONE);
        } else {
            commentRecyclerView.setVisibility(View.VISIBLE);
            showAll.setVisibility(View.VISIBLE);
            showAll.setOnClickListener(v -> {
//                int firstPosition = ((LinearLayoutManager) commentRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
//                if (firstPosition > 20) {
//                    commentRecyclerView.scrollToPosition(0);
//                } else {
//                    commentRecyclerView.smoothScrollToPosition(0);
//                }
                collapseComment();
            });
            commentAdapter.setNewData(book.commentList);
        }
    }

    private void collapseComment() {
        commentRecyclerView.stopScroll();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(root);
        }
        ViewGroup.LayoutParams params = commentRecyclerView.getLayoutParams();
        int singleItemHeight = ConvertUtils.dp2px(60);
        params.height = isExpand ? singleItemHeight : ViewGroup.LayoutParams.WRAP_CONTENT;
        commentRecyclerView.setLayoutParams(params);
        isExpand = !isExpand;
        showAll.setText(isExpand ? R.string.collapse : R.string.showAllComment);

    }


    private Observable<RealmList<Comment>> fetchComment() {
        return DataFetcher.getInstance(bookId, page).fetchComment();
    }

    private void moreComment() {
        DataFetcher.getInstance(bookId, page).fetchComment()
                .compose(applySchedulers())
                .subscribe(comments -> {
                    Log.d(TAG, "call: comments" + comments.size());
                    if (book.commentList == comments) {
                        return;
                    }
                    if (comments.size() == 0) {
                        commentAdapter.loadMoreEnd(true);
                        return;
                    }
                    database.realm.beginTransaction();
                    if (book.commentList == null) {
                        book.commentList = new RealmList<>();
                    }
                    if (page == 1) {
                        book.commentList = comments;
                    } else {
                        book.commentList.addAll(comments);
                    }
                    database.realm.copyToRealmOrUpdate(book);
                    database.realm.commitTransaction();
                    popComment();

                }, throwable -> {
                    if (commentAdapter.getItemCount() > 0) {
                        commentAdapter.loadMoreComplete();
                    } else {
                        commentAdapter.loadMoreFail();
                    }
                });
    }


    private void populate(Book book) {
        loadCover(book.cover);
        initToolbar(book.title, book.author);
        tvDesc.setText(book.desc);
        tvDesc.setOnClickListener(v -> UiUtils.showDetailDialog(BookDetailActivity.this, book.desc));
        tvAuthor.setText(book.author);
        tvAuthor.setOnClickListener(v -> {
            Intent author = new Intent(getApplicationContext(), MainActivity.class);
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
        writeComment.setVisibility(View.VISIBLE);
        writeComment.setOnClickListener(v -> writeComment());

    }

    private void initToolbar(String title, String author) {
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

    private void loadCover(String url) {
        Imager.loadCover(this, url, new SimpleTarget<Bitmap>() {
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
        page = 1;
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


    private class MyLinearLayoutManager extends LinearLayoutManager {
        public MyLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void collectAdjacentPrefetchPositions(int dx, int dy, RecyclerView.State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
            try {
                super.collectAdjacentPrefetchPositions(dx, dy, state, layoutPrefetchRegistry);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "catch exception");
            }
        }
    }
}