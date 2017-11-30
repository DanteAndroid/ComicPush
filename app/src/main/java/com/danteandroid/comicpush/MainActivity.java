package com.danteandroid.comicpush;

import android.app.AlertDialog;
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
import android.text.method.LinkMovementMethod;
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
import com.danteandroid.comicpush.base.BaseActivity;
import com.danteandroid.comicpush.detail.BookDetailActivity;
import com.danteandroid.comicpush.main.BookListAdapter;
import com.danteandroid.comicpush.model.Book;
import com.danteandroid.comicpush.net.DataFetcher;
import com.danteandroid.comicpush.net.HttpErrorAction;
import com.danteandroid.comicpush.utils.AppUtil;
import com.danteandroid.comicpush.utils.DateUtil;
import com.danteandroid.comicpush.utils.SimpleChineseConvert;
import com.danteandroid.comicpush.utils.SpUtil;
import com.google.android.flexbox.FlexboxLayout;
import com.robertlevonyan.views.chip.Chip;
import com.robertlevonyan.views.chip.ChipUtils;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import rx.Observable;

public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final int SEARCH_DEBOUNCE_TIME = 5000;
    private static final String TAG = "MainActivity";
    private static final long COLLAPSE_TAGS_DELAY = 3000;
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
    @BindView(R.id.showAll)
    TextView showAll;
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
    private String query = "all";
    private boolean isFromOther;
    private boolean isTraditional;

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
        initInfo();
        initTags();
        initMain();
        fetchInfo();
    }

    private void initInfo() {
        if (SpUtil.getLong(Constants.SHOULD_UPDATE_TIME) < (new Date().getTime())) {
            database.clear();
            SpUtil.save(Constants.SHOULD_UPDATE_TIME, DateUtil.nextWeekDateOfToday().getTime());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fetchInfo();
    }

    private void fetchInfo() {
        if (getIntent().getExtras() == null) {
            query = String.format(Locale.getDefault(), "%s,all,%s,%s,all,%s", category, state, order, length);
        } else {
            query = getIntent().getStringExtra(Constants.QUERY);
        }
        page = 1;
        fetch();
    }

    private void initMain() {
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setColorSchemeColors(color(R.color.colorPrimary),
                color(R.color.colorPrimaryDark), color(R.color.colorAccent));
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
        showAll.setOnClickListener(v -> {
            collapseTags();

        });
        if (toolbar != null) {
            toolbar.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
        }

    }

    private void onBookClicked(View view, int position) {
        View cover = view.findViewById(R.id.cover);
        Book book = adapter.getItem(position);
        if (book != null) {
            ViewCompat.setTransitionName(cover, book.title);
            Intent intent = new Intent(MainActivity.this, BookDetailActivity.class);
            intent.putExtra(Constants.TITLE, book.title);
            intent.putExtra(Constants.AUTHOR, book.author);
            intent.putExtra(Constants.COVER, book.cover);
            intent.putExtra(Constants.BOOK_ID, book.bookId);
            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(MainActivity.this, cover, book.title);
            ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
        }
    }


    private void initTagsText() {
        for (int i = 0; i < categories.getChildCount(); i++) {
            Chip chip = (Chip) categories.getChildAt(i);
            String text = chip.getChipText();
            if (isTraditional) {
                text = SimpleChineseConvert.simpleToTraditional(text);
            } else {
                text = SimpleChineseConvert.traditionalToSimple(text);
            }
            chip.setChipText(text);
        }
        for (int i = 0; i < lengths.getChildCount(); i++) {
            Chip chip = (Chip) lengths.getChildAt(i);
            String text = chip.getChipText();
            if (isTraditional) {
                text = SimpleChineseConvert.simpleToTraditional(text);
            } else {
                text = SimpleChineseConvert.traditionalToSimple(text);
            }
            chip.setChipText(text);
        }
        for (int i = 0; i < states.getChildCount(); i++) {
            Chip chip = (Chip) states.getChildAt(i);
            String text = chip.getChipText();
            if (isTraditional) {
                text = SimpleChineseConvert.simpleToTraditional(text);
            } else {
                text = SimpleChineseConvert.traditionalToSimple(text);
            }
            chip.setChipText(text);
        }
        for (int i = 0; i < orders.getChildCount(); i++) {
            Chip chip = (Chip) orders.getChildAt(i);
            String text = chip.getChipText();
            if (isTraditional) {
                text = SimpleChineseConvert.simpleToTraditional(text);
            } else {
                text = SimpleChineseConvert.traditionalToSimple(text);
            }
            chip.setChipText(text);
        }
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
        query = String.format(Locale.getDefault(), "%s,all,%s,%s,all,%s", category, state, order, length);
        fetch();

    }


    public void collapseTags() {
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
        params.height = isExpand ? chip.getHeight() + cat.getHeight() + verticalPadding + verticalMargin : ViewGroup.LayoutParams.WRAP_CONTENT;
        layout.setLayoutParams(params);
        this.isExpand = !isExpand;
        showAll.setText(isExpand ? R.string.collapse : R.string.expand);
    }


    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        KeyboardUtils.hideSoftInput(MainActivity.this);
        if (id == R.id.action_about) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.about)
                    .setMessage(R.string.about_message)
                    .setNegativeButton(R.string.logoff, (dialog13, which) -> new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.attention)
                            .setMessage(R.string.logoff_message)
                            .setPositiveButton(R.string.logoff, (dialog12, w) -> {
                                database.clear();
                                SpUtil.clear();
                            })
                            .show())
                    .setPositiveButton(R.string.update, (dialog1, which) -> AppUtil.goMarket(MainActivity.this))
                    .show();
            ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

        } else if (id == R.id.donate) {
            AppUtil.donate(MainActivity.this);
        } else if (id == R.id.action_simple_traditional) {


        }
        return super.onOptionsItemSelected(item);
    }

    private void initSimpleOrTraditional(MenuItem item) {
        isTraditional = SpUtil.getBoolean(Constants.IS_TRADITIONAL, Locale.getDefault().equals(Locale.TRADITIONAL_CHINESE));
//        initTagsText();
        item.setTitle(isTraditional ? "简体" : "繁體");
        item.setOnMenuItemClickListener(item12 -> {
            isTraditional = !isTraditional;
            Log.d(TAG, "isTraditional: " + isTraditional);
            item.setTitle(isTraditional ? "简体" : "繁體");
            SpUtil.save(Constants.IS_TRADITIONAL, isTraditional);
//            initTagsText();
            fetch();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        // Retrieve the SearchView and plug it into SearchManager
        searchItem = menu.findItem(R.id.action_search);
        initSimpleOrTraditional(menu.findItem(R.id.action_simple_traditional));
        searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchItem = menu.findItem(R.id.action_search);
        initSearchView();

        return true;
    }

    private void initSearchView() {
        searchView.setQueryHint(getString(R.string.query_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.trim();
                if (TextUtils.isEmpty(query)) {
                    query = "all";
                }
                fetchSearch(query);
                KeyboardUtils.hideSoftInput(MainActivity.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                nowTime = System.currentTimeMillis();
                Observable.just(newText).filter(s -> !TextUtils.isEmpty(s)
                        && nowTime - lastTime > SEARCH_DEBOUNCE_TIME)
                        .subscribe(s -> {
                            fetchSearch(s);
                            lastTime = nowTime;
                        });
                return false;
            }
        });

    }

    private void fetch() {
//        http://vol.moe/list/少女,all,完結,sortpoint,fetch,s/
//            query = String.format(Locale.getDefault(), "%s,all,%s,%s,all,%s", category, state, order, length);
        fetch(query);
    }

    private void fetch(String info) {
        swipeRefresh.setRefreshing(true);

        DataFetcher.getInstance(info, page).fetch()
                .compose(applySchedulers())
                .subscribe(books -> {
                    swipeRefresh.setRefreshing(false);
                    updateUI(books);

                }, new HttpErrorAction<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        super.call(throwable);
                        throwable.printStackTrace();
                        ToastUtils.showShortToast(errorMessage);
                        adapter.loadMoreFail();
                        swipeRefresh.setRefreshing(false);
                    }
                });

    }

    private void updateUI(List<Book> books) {
        if (books.size() == 0) {
            adapter.loadMoreEnd();
            return;
        }
        if (page <= 1) {
            adapter.setNewData(books);
            recyclerView.scrollToPosition(0);

        } else {
            adapter.addData(books);
            adapter.loadMoreComplete();
        }
    }


    private void fetchSearch(String keyword) {
        keyword = SimpleChineseConvert.simpleToTraditional(keyword);
        page = 1;
        query = keyword;
        fetch();
    }


}
