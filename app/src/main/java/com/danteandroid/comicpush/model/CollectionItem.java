package com.danteandroid.comicpush.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by yons on 17/12/1.
 */

public class CollectionItem implements MultiItemEntity{
    public static final int ITEM_TITLE = 1;
    public static final int ITEM_BOOK_CONTENT = 2;
    public static final int ITEM_PUSH_CONTENT = 3;

    public int bookId;

    public CollectionItem() {
    }

    public CollectionItem(int type) {
        this.type = type;
    }

    public String title = "";
    public String author = "";
    public String cover = "";
    public String lastUpdate = "";
    public String info = "";

    public int type;

    @Override
    public int getItemType() {
        return type;
    }
}
