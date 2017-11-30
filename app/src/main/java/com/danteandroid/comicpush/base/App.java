package com.danteandroid.comicpush.base;

import android.app.Application;
import android.content.Context;

import com.blankj.utilcode.utils.Utils;
import com.bugtags.library.Bugtags;

import io.realm.Realm;


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
        Realm.init(this);
        //在这里初始化
        Bugtags.start("b6bda52df50bbb941b2656fee4c2e97d", this,  Bugtags.BTGInvocationEventNone);
    }
}
