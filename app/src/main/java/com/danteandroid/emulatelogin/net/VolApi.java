package com.danteandroid.emulatelogin.net;

import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yons on 17/11/21.
 */

public interface VolApi {

    @FormUrlEncoded
    @POST("login_do.php")
    Observable<ResponseBody> login(@Field("email") String email, @Field("passwd") String password, @Field("keepalive")String on);

    @GET("my.php")
    Observable<ResponseBody> getMyProfile();

    @GET("list/{query}/{page}")
    Observable<ResponseBody> books(@Path("query")String query, @Path("page")int page);
    @GET("{bookLink}")
    Observable<ResponseBody> bookDetail(@Path("bookLink")String bookLink);

    //http://vol.moe/list/{keyword},all,all,sortpoint,all,all/2.htm
    @GET("list.php")
    Observable<ResponseBody> queryBooks(@Query("s")String keyword);
}
