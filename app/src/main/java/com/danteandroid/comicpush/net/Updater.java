package com.danteandroid.comicpush.net;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.TextUtils;

import com.danteandroid.comicpush.BuildConfig;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.model.AppInfo;
import com.danteandroid.comicpush.utils.AppUtil;
import com.danteandroid.comicpush.utils.SpUtil;
import com.danteandroid.comicpush.utils.UiUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Update app helper.
 */

public class Updater {
    public static final String SHARE_APP = "share_app";
    public static final String SHOULD_SHOW_UPDATE = "shouldShow";
    public static final String SHOULD_SHOW_ANNOUNCEMENT = "shouldShowAnnouncement";
    public static final String EGG_URL = "egg_url";
    private final Activity context;

    private Updater(Activity context) {
        this.context = context;
    }

    public static Updater getInstance(Activity context) {
        return new Updater(context);

    }

    public void check(boolean forceShow) {
        NetService.request().getAppInfo(API.GITHUB_APP_JSON)
                .filter(appInfo -> {
                    showAnnouncement(appInfo);
                    SpUtil.save(Updater.SHARE_APP, appInfo.getShareApp());
                    SpUtil.save(Updater.EGG_URL, appInfo.getEggUrl());
                    if (forceShow && appInfo.getVersionCode() == BuildConfig.VERSION_CODE) {
                        UiUtils.showSnack(context.getWindow().getDecorView(), R.string.is_latest_version);
                    }
                    return appInfo.getVersionCode() > BuildConfig.VERSION_CODE;//版本有更新
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appInfo -> {
                    boolean shouldShowUpdate = SpUtil.getBoolean(appInfo.getVersion() + SHOULD_SHOW_UPDATE, true);
                    if (shouldShowUpdate || forceShow) {
                        showDialog(appInfo);
                    }
                }, throwable -> throwable.printStackTrace());
    }

    private void showAnnouncement(AppInfo appInfo) {
        if (TextUtils.isEmpty(appInfo.getAnnouncement())
                || !SpUtil.getBoolean(appInfo.getAnnouncement() + SHOULD_SHOW_ANNOUNCEMENT, true)) {
            return;
        }

        context.runOnUiThread(() -> new AlertDialog.Builder(context)
                .setMessage(appInfo.getAnnouncement())
                .setPositiveButton(R.string.got_it,
                        (dialog, which) -> SpUtil.save(appInfo.getAnnouncement() + SHOULD_SHOW_ANNOUNCEMENT, false))
                .show());

    }

    private void showDialog(final AppInfo appInfo) {
        boolean needUpdate = appInfo.isForceUpdate();
        new AlertDialog.Builder(context).setTitle(R.string.detect_new_version)
                .setCancelable(!needUpdate)//需要更新就不可取消
                .setMessage(String.format(context.getString(R.string.update_message), appInfo.getMessage()))
                .setPositiveButton(R.string.go_market, (dialog, which) -> AppUtil.goMarket(context))
                .setNegativeButton(R.string.dont_hint_update,
                        (dialog, which) -> SpUtil.save(appInfo.getVersion() + SHOULD_SHOW_UPDATE, false))
                .show();
    }


}
