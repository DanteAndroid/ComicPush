package com.danteandroid.comicpush.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.target.SimpleTarget;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.net.NetService;

/**
 * Created by yons on 17/12/5.
 */

public class Imager {

    public static void loadWithHeader(Context context, String url, ImageView target) {
        if (TextUtils.isEmpty(url)) return;
        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Referer", "http://vol.moe/")
                .addHeader("User-Agent", NetService.AGENT).build());
        Glide.with(context).load(glideUrl).into(target);
    }
    public static void loadWithPlaceHolder(Context context, String url, ImageView target) {
        if (TextUtils.isEmpty(url)) return;
        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Referer", "http://vol.moe/")
                .addHeader("User-Agent", NetService.AGENT).build());
        Glide.with(context).load(glideUrl).
                placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher)
                .into(target);
    }

    public static void loadCover(Context context, String url, SimpleTarget<Bitmap> simpleTarget) {
        if (TextUtils.isEmpty(url)) return;
        GlideUrl glideUrl = new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("Referer", "http://vol.moe/")
                .addHeader("User-Agent", NetService.AGENT).build());
        Glide.with(context).load(glideUrl)
                .asBitmap()
                .into(simpleTarget);
    }
}
