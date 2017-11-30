package com.danteandroid.comicpush.net;

import com.danteandroid.comicpush.BuildConfig;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

import static com.danteandroid.comicpush.base.App.context;

/**
 * Created by yons on 17/11/21.
 */

public class NetService {
    public static final String AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36";

    private static NetService instance;
    private final Gson gson = new Gson();
    private Retrofit retrofit;
    private String baseUrl;
    private OkHttpClient client;
    private VolApi api;

    private NetService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static NetService getInstance(String baseUrl) {
        if (instance == null || !instance.baseUrl.equals(baseUrl)) {
            instance = new NetService(baseUrl);
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);
            ClearableCookieJar cookieJar =
                    new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            instance.client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .addInterceptor(interceptor).build();
        }
        return instance;
    }

    public static VolApi request() {
        return getInstance(API.BASE_URL).getApi();
    }

    private VolApi getApi() {
        if (api == null) {
            api = createService(VolApi.class);
        }
        return api;
    }

    private <T> T createService(Class<T> tClass) {
        retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(tClass);
    }

}
