package com.danteandroid.comicpush.main;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.model.Book;

/**
 * Created by yons on 17/11/23.
 */

public class BookListAdapter extends BaseQuickAdapter<Book, BaseViewHolder> {

    public BookListAdapter() {
        super(R.layout.book_item, null);
        setHasStableIds(true);
    }

    @Override
    protected void convert(BaseViewHolder helper, Book item) {
        Context context = helper.itemView.getContext();
        helper.setText(R.id.title, item.title);
        helper.setText(R.id.vol, item.vol);
        helper.setText(R.id.author, item.author);
        ImageView cover = helper.getView(R.id.cover);
//        TextView title=helper.getView(R.id.title);
//        TextView vol=helper.getView(R.id.vol);
//        TextView author=helper.getView(R.id.author);
        Glide.with(context).load(item.cover).into(cover);
    }
}
