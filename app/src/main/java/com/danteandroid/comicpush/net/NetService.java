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
//                    .cookieJar(cookieJar)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .addHeader("Cookie", "VLIBSID=q49sepjshuetqrqdi3hubiffd4")
                                .addHeader("Referer", "https://vol.moe/")
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
//                .cookieJar(cookieJar)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .addHeader("Cookie", "VLIBSID=q49sepjshuetqrqdi3hubiffd4; VOLUIN=10004759; VOLKEY=18827b515f9dc358cf3318cc216fc511; VOLSESS=1569421999")
                            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                            .addHeader("Accept-Encoding", "gzip, deflate, br")
                            .addHeader("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7,ja;q=0.6")
                            .addHeader("Sec-Fetch-Mode", "nested-navigate")
                            .addHeader("Sec-Fetch-User", "same-origin")
                            .addHeader("Sec-Fetch-Site", "?1")
                            .addHeader("Upgrade-Insecure-Requests", "1")
                            .addHeader("Referer", referer)
                            .addHeader("Origin", "https://volmoe.com")
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
