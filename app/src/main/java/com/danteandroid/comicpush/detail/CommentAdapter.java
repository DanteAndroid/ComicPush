package com.danteandroid.comicpush.detail;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.model.Comment;

/**
 * Created by yons on 17/12/1.
 */

public class CommentAdapter extends BaseQuickAdapter<Comment, BaseViewHolder> {


    public CommentAdapter() {
        super(R.layout.comment_item, null);
        setHasStableIds(true);
    }


    @Override
    protected void convert(BaseViewHolder helper, Comment item) {
        Context context = helper.itemView.getContext();
        helper.setText(R.id.content, item.content)
                .setText(R.id.title, item.title);


    }
}
