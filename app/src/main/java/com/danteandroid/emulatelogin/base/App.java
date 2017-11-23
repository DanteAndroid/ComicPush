package com.danteandroid.emulatelogin.base;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;


/**
 * Created by yons on 17/11/21.
 */

public class App extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Utils.init(this);
    }
}
