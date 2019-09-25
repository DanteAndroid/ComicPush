package com.danteandroid.comicpush.net;

import com.danteandroid.comicpush.model.AppInfo;

import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by yons on 17/11/21.
 */

public interface VolApi {

    @FormUrlEncoded
    @POST("login_do.php")
    Observable<ResponseBody> login(@Field("email") String email, @Field("passwd") String password, @Field("keepalive") String on);

    @GET("login.php")
    @Headers("Content-Type:application/x-www-form-urlencoded")
    Observable<ResponseBody> login();


    /**
     * follow：1，1    cancel follow: 1,0
        favorite: 0,1    cancel favorite: 0,0
     */
    @FormUrlEncoded
    @POST("book_follow.php")
    Observable<ResponseBody> followOrFavorite(@Field("follow_bookid") int bookId,
                                              @Field("autopush_yes") int autopush, @Field("follow_yes") int follow);

    /**
      "m100":"推送已經登記成功，將會陸續推送到達",
     "m101":"部分推送登記失敗，可能由於額度不足",
     "m102":"正在進行推送登記......",
     "m103":"操作成功",
     "like1":"點贊成功",
     "like0":"已取消贊",
     "c100":"do_callback",
     "c101":"do_callback1",
     "c102":"do_callback2",
     "e400":"文檔暫不支持下載",
     "e401":"請先登錄",
     "e402":"權限不足",
     "e403":"額度不足",
     "lv02":"用戶等級不足，需達到Lv2，驗證Kindle郵箱可升級到Lv2",
     "lv03":"用戶等級不足，需達到Lv3，達到前請使用推送服務",
     "lv05":"用戶等級不足，需達到Lv5"
     */
    @FormUrlEncoded
    @POST("book_push.php")
    Observable<ResponseBody> push(@Field("push_bookid")int bookId, @Field("push_vol_list") String vol_list);
    @FormUrlEncoded
    @POST("book_comm_do.php")
    Observable<ResponseBody> comment(@Field("bookid")int bookId, @Field("comm_title") String title,
                                     @Field("comm_content") String content, @Field("book_score")int score);

    @GET("my.php")
    Observable<ResponseBody> myProfile();

    @GET("register.php")
    Observable<ResponseBody> register();

    @GET("myfollow.php")
    Observable<ResponseBody> myFollowings();

    @GET("list/{query}/{page}")
    Observable<ResponseBody> books(@Path("query") String query, @Path("page") int page);

    @GET("list/{author}")
    Observable<ResponseBody> booksOfAuthor(@Path("author") String author);

    @GET("comic/{bookId}.htm")
    Observable<ResponseBody> bookDetail(@Path("bookId") int bookId);

    //http://vol.moe/list/{keyword},all,all,sortpoint,all,all/2.htm
    @GET("list.php")
    Observable<ResponseBody> queryBooks(@Query("s") String keyword);

    //http://manhua.vol.moe/comic/10232.htm
    @GET("comic/{bookId}.htm")
    Observable<ResponseBody> bookCovers(@Path("bookId")int bookId);

    @GET()
    Observable<ResponseBody> download(@Url String url);

    ///book_comm_list.php?b=10338
    @GET("book_comm_list.php")
    Observable<ResponseBody> commentList(@Query("b") int bookId, @Query("p") int page);



    @GET()
    Observable<AppInfo> getAppInfo(@Url String url);

}
