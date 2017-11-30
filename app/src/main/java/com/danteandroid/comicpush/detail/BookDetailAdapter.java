package com.danteandroid.comicpush.detail;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.model.Volume;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yons on 17/11/23.
 */

public class BookDetailAdapter extends BaseQuickAdapter<Volume, BaseViewHolder> {

    private final CompoundButton.OnCheckedChangeListener listener;
    List<Volume> checked = new ArrayList<>();
    private boolean select;
    private boolean selectReverse;


    public BookDetailAdapter(CheckBox.OnCheckedChangeListener listener) {
        super(R.layout.book_volume_item, null);
        setHasStableIds(true);
        this.listener = listener;
    }

    public void selectAll(boolean select) {
        this.select = select;
        selectReverse = false;
        notifyDataSetChanged();
    }

    public List<Volume> getChecked() {
        return checked;
    }

    @Override
    protected void convert(BaseViewHolder helper, Volume item) {
        Context context = helper.itemView.getContext();
        CheckBox checkBox = helper.getView(R.id.checkbox);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "convert: " + isChecked);
            if (isChecked) {
                checked.add(item);
            } else {
                if (checked.contains(item)) {
                    checked.remove(item);
                }
            }
            listener.onCheckedChanged(buttonView, isChecked);
        });
        if (selectReverse) {
            select = !checkBox.isChecked();
        }
        checkBox.setChecked(select);
        checkBox.setText(item.title);
        String size = String.format("<font color='#808080'>推送%sM</font>", item.size);
        checkBox.setText(Html.fromHtml(item.title + "<br>" + size));
    }


    public void selectReverse() {
        selectReverse = true;
        notifyDataSetChanged();
    }
}
