package com.danteandroid.comicpush.net;

import android.preference.PreferenceManager;
import android.util.Log;

import com.danteandroid.comicpush.base.App;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yons on 17/11/21.
 */

public class ReadCookiesInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        Set<String> preferences =
                PreferenceManager.getDefaultSharedPreferences(App.context).getStringSet("cookies", new HashSet<String>());
        for (String cookie : preferences) {
            builder.addHeader("Cookie", cookie);
            Log.d("OkHttp", "intercept read Header: " + cookie); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
        }

        return chain.proceed(builder.build());
    }
}


