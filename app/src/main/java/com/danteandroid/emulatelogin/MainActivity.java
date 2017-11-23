package com.danteandroid.emulatelogin;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.utils.KeyboardUtils;
import com.blankj.utilcode.utils.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.danteandroid.emulatelogin.base.BaseActivity;
import com.danteandroid.emulatelogin.detail.BookDetailActivity;
import com.danteandroid.emulatelogin.main.Book;
import com.danteandroid.emulatelogin.main.BookListAdapter;
import com.danteandroid.emulatelogin.net.DataFetcher;
import com.google.android.flexbox.FlexboxLayout;
import com.robertlevonyan.views.chip.Chip;
import com.robertlevonyan.views.chip.ChipUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;

import static com.blankj.utilcode.utils.Utils.getContext;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final int SEARCH_DEBOUNCE_TIME = 5000;
    private static final String TAG = "MainActivity";
    @BindView(R.id.categoryConstraintLayout)
    ConstraintLayout constraintLayout;
    @BindView(R.id.categories)
    FlexboxLayout categories;
    @BindView(R.id.lengths)
    FlexboxLayout lengths;
    boolean isExpand;
    @BindView(R.id.root)
    LinearLayout root;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    BookListAdapter adapter;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.tagsLayout)
    LinearLayout tagsLayout;
    @BindView(R.id.cat)
    TextView cat;
    @BindView(R.id.states)
    FlexboxLayout states;
    @BindView(R.id.orders)
    FlexboxLayout orders;
    private SearchView searchView;
    private MenuItem searchItem;
    private int page = 1;
    private StaggeredGridLayoutManager layoutManager;
    private String category = "all";
    private String length = "all";
    private String state = "all";
    private String order = "all";
    private long nowTime;
    private long lastTime;

    @OnClick(R.id.showAll)
    void onClick(View v) {
        collapseTags(isExpand);
        isExpand = !isExpand;
        ((TextView) v).setText(isExpand ? "收起" : "全部");
    }

    @Override
    protected boolean needNavigation() {
        return false;
    }

    @Override
    protected int initLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        initTags();
        initMain();
        fetch();
    }

    private void initMain() {
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setColorSchemeColors(getColor(R.color.colorPrimary),
                getColor(R.color.colorPrimaryDark), getColor(R.color.colorAccent));
        swipeRefresh.setOnRefreshListener(this);
//        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);
        adapter = new BookListAdapter();
        recyclerView.setAdapter(adapter);
        adapter.bindToRecyclerView(recyclerView);
        adapter.disableLoadMoreIfNotFullPage();
        adapter.setOnLoadMoreListener(() -> {
            if (adapter.getData().size() > 0) {
                page++;
            }
            fetch();
        }, recyclerView);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                onBookClicked(view, i);
            }
        });
    }

    private void onBookClicked(View view, int position) {
        View cover = view.findViewById(R.id.cover);
        Book book = adapter.getItem(position);
        if (book !=null){
            ViewCompat.setTransitionName(cover, book.title);
            Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
            intent.putExtra(Constants.TITLE, book.title);
            intent.putExtra(Constants.AUTHOR, book.author);
            intent.putExtra(Constants.COVER, book.cover);
            intent.putExtra(Constants.LINK, book.link);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(MainActivity.this, cover, book.title);
            ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
//            startActivity(intent);
        }


    }

    private void updateUI(Observable<List<Book>> data) {
        data.compose(applySchedulers())
                .subscribe(books -> {
                    swipeRefresh.setRefreshing(false);
                    Log.d(TAG, "call: " + books.size());
                    if (books.size()==0){
                        adapter.loadMoreEnd();
                        return;
                    }
                    if (page <= 1) {
                        adapter.setNewData(books);
                        recyclerView.smoothScrollToPosition(0);

                    } else {
                        adapter.addData(books);
                        adapter.loadMoreComplete();
                    }

                }, throwable -> {
                    ToastUtils.showShortToast(throwable.getMessage());
                    adapter.loadMoreFail();
                    swipeRefresh.setRefreshing(false);

                });
    }



    private void initTags() {
        for (int i = 0; i < categories.getChildCount(); i++) {
            Chip chip = (Chip) categories.getChildAt(i);
            chip.setOnSelectClickListener((v, selected) -> {
                if (selected) {
                    category = chip.getChipText();
                    if (chip.getId() == R.id.allCat) {
                        category = "all";
                    }
                    refresh();
                }
                ChipUtils.resetAllAndSelectChip(v, selected);
            });
            if (i == 0) {
                chip.setSelected(true);
            }
        }
        for (int i = 0; i < orders.getChildCount(); i++) {
            Chip chip = (Chip) orders.getChildAt(i);
            chip.setOnSelectClickListener((v, selected) -> {
                if (selected) {
                    switch (v.getId()) {
                        case R.id.sortpoint:
                            order = "sortpoint";
                            break;
                        case R.id.score:
                            order = "score";
                            break;
                        case R.id.count_push:
                            order = "count_push";
                            break;
                        case R.id.lastupdate:
                            order = "lastupdate";
                            break;
                        default:
                            order = "sortpoint";
                            break;
                    }
                    refresh();
                }
                ChipUtils.resetAllAndSelectChip(v, selected);
            });
            if (i == 0) {
                chip.setSelected(true);
            }
        }
        for (int i = 0; i < states.getChildCount(); i++) {
            Chip chip = (Chip) states.getChildAt(i);
            chip.setOnSelectClickListener((v, selected) -> {
                if (selected) {
                    state = chip.getChipText();
                    if (chip.getId() == R.id.allState) {
                        category = "all";
                    }
                    refresh();

                }
                ChipUtils.resetAllAndSelectChip(v, selected);
            });
            if (i == 0) {
                chip.setSelected(true);
            }
        }
        for (int i = 0; i < lengths.getChildCount(); i++) {
            Chip chip = (Chip) lengths.getChildAt(i);
            chip.setOnSelectClickListener((v, selected) -> {
                if (selected) {
                    switch (v.getId()) {
                        case R.id.shortLength:
                            length = "s";
                            break;
                        case R.id.mediumLength:
                            length = "m";
                            break;
                        case R.id.longLength:
                            length = "l";
                            break;
                        default:
                            length = "all";
                            break;
                    }
                    refresh();

                }
                ChipUtils.resetAllAndSelectChip(v, selected);
            });
            if (i == 0) {
                chip.setSelected(true);
            }
        }
    }

    private void refresh() {
        page = 1;
        fetch();
    }


    public void collapseTags(boolean isExpand) {
        Chip chip = findViewById(R.id.allCat);
        LinearLayout layout = findViewById(R.id.tagsLayout);
        LinearLayout root = findViewById(R.id.root);
        TextView cat = findViewById(R.id.cat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(root);
        }
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        int verticalPadding = layout.getPaddingTop() + layout.getPaddingBottom();
        FlexboxLayout.LayoutParams chipParams = ((FlexboxLayout.LayoutParams) chip.getLayoutParams());
        int verticalMargin = chipParams.topMargin + chipParams.bottomMargin;
        Log.d(TAG, "onCreate: " + params.height + " " + isExpand);
        params.height = isExpand ? chip.getHeight() + cat.getHeight() + verticalPadding + verticalMargin : ViewGroup.LayoutParams.WRAP_CONTENT;
        layout.setLayoutParams(params);
//        ValueAnimator animator = ValueAnimator、.ofInt(params.height, collapseTags ? chip1.getHeight()+26 : 310);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                flexboxLayout1.getLayoutParams().height = (int) animation.getAnimatedValue();
//                flexboxLayout1.requestLayout();
//            }
//        });
//        animator.start();
    }


    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // Retrieve the SearchView and plug it into SearchManager
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchItem = menu.findItem(R.id.action_search);
        initSearchView();
        return true;
    }

    private void initSearchView() {
        searchView.setQueryHint("请输入关键词");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.trim();
                if (TextUtils.isEmpty(query)){
                    query = "all";
                }
                fetchResult(query);
                KeyboardUtils.hideSoftInput(MainActivity.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                nowTime = System.currentTimeMillis();
                Observable.just(newText).filter(s -> !s.isEmpty()
                        && nowTime - lastTime > SEARCH_DEBOUNCE_TIME)
                        .subscribe(s -> fetchResult(s));
                return false;
            }
        });

    }

    private void fetch() {
//        http://vol.moe/list/少女,all,完結,sortpoint,fetch,s/
        String info = String.format(Locale.getDefault(), "%s,all,%s,%s,all,%s", category, state, order, length);
        fetch(info);
    }

    private void fetch(String info) {
        swipeRefresh.setRefreshing(true);
        Observable<List<Book>> books = DataFetcher.getInstance(info, page).fetch();
        updateUI(books);

    }

    private void fetchResult(String keyword) {
        page = 1;
        category = keyword;
        fetch();
    }

}
