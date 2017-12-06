package com.danteandroid.comicpush.net;

import com.danteandroid.comicpush.BuildConfig;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

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
            interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
            ClearableCookieJar cookieJar =
                    new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            instance.client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .addHeader("Accept", "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                .addHeader("Accept-Encoding", "gzip, deflate")
                                .addHeader("Accept_Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                                .addHeader("Cache-Control", "max-age=0")
                                .addHeader("Connection", "keep-alive")
                                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                                .addHeader("Content-Length", "51")
                                .addHeader("Host", "vol.moe")
                                .addHeader("Origin", "http://vol.moe")
                                .addHeader("Upgrade-Insecure-Requests", "1")
                                .addHeader("Referer", "http://vol.moe/")
                                .addHeader("User-Agent", AGENT)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(interceptor).build();
        }
        return instance;
    }

    public static NetService getService(String baseUrl, String referer) {
        NetService service = new NetService(baseUrl);
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        service.client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .addHeader("Accept", "Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                            .addHeader("Accept-Encoding", "gzip, deflate")
                            .addHeader("Accept_Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                            .addHeader("Cache-Control", "max-age=0")
                            .addHeader("Connection", "keep-alive")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Content-Length", "51")
                            .addHeader("Host", "vol.moe")
                            .addHeader("Origin", "http://vol.moe")
                            .addHeader("Upgrade-Insecure-Requests", "1")
                            .addHeader("Referer", referer)
                            .addHeader("User-Agent", AGENT)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(interceptor).build();
        return service;
    }

    public static VolApi request() {
        return getInstance(API.BASE_URL).getApi();
    }

    public VolApi getApi() {
        if (api == null) {
            api = createService(VolApi.class);
        }
        return api;
    }

    private <T> T createService(Class<T> tClass) {
        retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(tClass);
    }

}
