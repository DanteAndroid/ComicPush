package com.danteandroid.comicpush.base;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.blankj.utilcode.utils.Utils;
import com.bugtags.library.Bugtags;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.concurrent.TimeUnit;

import cn.dreamtobe.filedownloader.OkHttp3Connection;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;


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
        //在这里初始化
        Bugtags.start("b6bda52df50bbb941b2656fee4c2e97d", this, Bugtags.BTGInvocationEventNone);
        initSDK();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

    }

    private void initSDK() {
        // Enable the okHttp3 connection with the customized okHttp client builder.
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10_000, TimeUnit.SECONDS); // customize the value of the connect timeout.
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        builder.cookieJar(cookieJar);
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new OkHttp3Connection.Creator(builder));
    }
}
