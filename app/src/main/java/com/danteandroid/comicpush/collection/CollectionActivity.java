package com.danteandroid.comicpush.collection;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.utils.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.MainActivity;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.base.BaseActivity;
import com.danteandroid.comicpush.detail.BookDetailActivity;
import com.danteandroid.comicpush.model.CollectionItem;
import com.danteandroid.comicpush.net.DataFetcher;
import com.danteandroid.comicpush.net.HttpErrorAction;
import com.danteandroid.comicpush.net.NetService;
import com.danteandroid.comicpush.utils.UiUtils;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;

public class CollectionActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    private CollectionItemAdapter adapter;
    private int page = 1;

    @Override
    protected void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        initMain();
        fetch();
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_collection;
    }


    private void initMain() {
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setColorSchemeColors(color(R.color.colorPrimary),
                color(R.color.colorPrimaryDark), color(R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(this);
//        layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
        adapter = new CollectionItemAdapter();
        recyclerView.setAdapter(adapter);
        adapter.bindToRecyclerView(recyclerView);
        adapter.disableLoadMoreIfNotFullPage();
//        adapter.setOnLoadMoreListener(() -> {
//            if (adapter.getData().size() > 0) {
//                page++;
//            }
//            fetch();
//        }, recyclerView);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {

            @Override
            public void onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                CollectionItem item = CollectionActivity.this.adapter.getItem(position);
                if (item != null) {
                    if (item.getItemType() == CollectionItem.ITEM_BOOK_CONTENT) {
                        showCancelFollowDialog(item);
                    }
                }
            }

            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                CollectionItem item = CollectionActivity.this.adapter.getItem(position);
                if (item != null) {
                    if (item.getItemType() != CollectionItem.ITEM_TITLE) {
                        goBookDetail(view, position);

                    }
                }
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                int id = view.getId();
                CollectionItem item = CollectionActivity.this.adapter.getItem(position);
                if (id == R.id.author) {
                    goAuthor(item);
                }
            }
        });

        if (toolbar != null) {
            toolbar.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
        }
    }

    private void showCancelFollowDialog(CollectionItem item) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.cancel_follow_message)
                .setNegativeButton(R.string.cancel_favorite, (dialog, which) -> cancelFollowOrFavorite(item, false))
                .setPositiveButton(R.string.cancel_follow, (dialog, which) -> cancelFollowOrFavorite(item, true)).show();
    }

    private void cancelFollowOrFavorite(CollectionItem item, boolean cancelFollow) {
        NetService.request().followOrFavorite(item.bookId, cancelFollow ? 1 : 0, 0)
                .compose(applySchedulers())
                .subscribe(responseBody -> {
                    String data;
                    try {
                        data = responseBody.string();
                        if (data.contains("c102")) {
                            data = getString(R.string.success);
                            new Handler().postDelayed(() -> onRefresh(), 1000);
                        } else {
                            data = getString(R.string.failed);
                        }
                        UiUtils.showSnack(recyclerView, cancelFollow ? getString(R.string.cancel_follow) + data
                                : getString(R.string.cancel_favorite) + data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void goAuthor(CollectionItem item) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(Constants.QUERY, item.author);
        startActivity(intent);
    }

    private void goBookDetail(View view, int position) {
        View cover = view.findViewById(R.id.cover);
        CollectionItem book = adapter.getItem(position);
        if (book != null) {
            Intent intent = new Intent(CollectionActivity.this, BookDetailActivity.class);
            if (book.cover != null && cover != null) {
                ViewCompat.setTransitionName(cover, book.cover);
                intent.putExtra(Constants.COVER, book.cover);
            }
            intent.putExtra(Constants.BOOK_ID, book.bookId);
            if (cover != null) {
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(CollectionActivity.this, cover, book.cover);
                ActivityCompat.startActivity(CollectionActivity.this, intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        }
    }

    private void fetch() {
        swipeRefresh.setRefreshing(true);

        DataFetcher.fetchMy()
                .compose(applySchedulers())
                .subscribe(collectionItems -> {
                    Log.d(TAG, "call: " + collectionItems.size());
                    updateUI(collectionItems);
                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        swipeRefresh.setRefreshing(false);
                        ToastUtils.showShortToast(errorMessage);
                    }
                });

    }

    private void updateUI(List<CollectionItem> items) {
        swipeRefresh.setRefreshing(false);
        adapter.setNewData(items);
    }

    @Override
    public void onRefresh() {
        page = 1;
        fetch();
    }
}
