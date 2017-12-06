package com.danteandroid.comicpush.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.danteandroid.comicpush.R;


public class SettingActivity extends BaseActivity {

    @Override
    protected int initLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initViews(@Nullable Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
