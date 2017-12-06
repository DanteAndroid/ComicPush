package com.danteandroid.comicpush.collection;

import android.content.Context;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.model.CollectionItem;
import com.danteandroid.comicpush.utils.Imager;

/**
 * Created by yons on 17/12/1.
 */

public class CollectionItemAdapter extends BaseMultiItemQuickAdapter<CollectionItem, BaseViewHolder> {


    public CollectionItemAdapter() {
        super(null);
        addItemType(CollectionItem.ITEM_TITLE, R.layout.collection_item_category);
        addItemType(CollectionItem.ITEM_BOOK_CONTENT, R.layout.collection_item_content);
        addItemType(CollectionItem.ITEM_PUSH_CONTENT, R.layout.collection_push_content);
    }


    @Override
    protected void convert(BaseViewHolder helper, CollectionItem item) {
        Context context = helper.itemView.getContext();
        switch (helper.getItemViewType()) {
            case CollectionItem.ITEM_TITLE:
                helper.setText(R.id.category, item.title);
                break;
            case CollectionItem.ITEM_BOOK_CONTENT:
                helper.setText(R.id.author, item.author)
                        .setText(R.id.title, item.title)
                        .addOnClickListener(R.id.author)
                        .setText(R.id.update, item.lastUpdate);

                ImageView cover = helper.getView(R.id.cover);
                Imager.loadWithPlaceHolder(context, item.cover, cover);
                break;
            case CollectionItem.ITEM_PUSH_CONTENT:
                helper.setText(R.id.title, item.title)
//                        .setText(R.id.author, item.author)
                        .setText(R.id.state, item.info)
                        .setText(R.id.update, item.lastUpdate);
                break;


        }
    }
}
