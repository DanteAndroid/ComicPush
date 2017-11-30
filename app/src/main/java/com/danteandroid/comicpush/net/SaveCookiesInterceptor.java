package com.danteandroid.comicpush.net;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.danteandroid.comicpush.base.App;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by yons on 17/11/21.
 */

public class SaveCookiesInterceptor implements Interceptor {
    private static final String TAG = "SaveCookiesInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();
            cookies.addAll(originalResponse.headers("Set-Cookie"));
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.context);
            if (sharedPreferences.getStringSet("cookies", null) == null) {
                Log.d(TAG, "intercept: " + cookies.toString());
                sharedPreferences.edit()
                        .putStringSet("cookies", cookies)
                        .apply();
            }
        }

        return originalResponse;
    }
}
