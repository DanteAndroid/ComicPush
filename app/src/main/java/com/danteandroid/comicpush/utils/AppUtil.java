package com.danteandroid.comicpush.utils;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.blankj.utilcode.utils.ClipboardUtils;
import com.danteandroid.comicpush.BuildConfig;
import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.LoginActivity;

import java.util.List;

import moe.feng.alipay.zerosdk.AlipayZeroSdk;

import static android.content.Context.UI_MODE_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.danteandroid.comicpush.base.App.context;

/**
 * Created by Dante on 2016/2/19.
 */
public class AppUtil {

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static void openAppInfo(Context context) {
        //redirect user to app Settings
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
        context.startActivity(i);
    }

    public static boolean isIntentSafe(Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }

    public static void openBrowser(Activity activity, String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
    }

    public static void goMarket(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
            activity.startActivity(intent);
        } catch (android.content.ActivityNotFoundException anfe) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/apk/" + BuildConfig.APPLICATION_ID));
            activity.startActivity(intent);
        }
    }

    public static void donate(Activity activity) {
        if (AlipayZeroSdk.hasInstalledAlipayClient(activity.getApplicationContext())) {
            AlipayZeroSdk.startAlipayClient(activity, Constants.ALI_PAY);
        } else {
            UiUtils.showSnackLong(activity.getWindow().getDecorView(), "未安装支付宝，微信号已复制");
            ClipboardUtils.copyText("lightear");
            goWechat();
        }
    }

    public static void restartApp(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void toggleNightMode() {
        UiModeManager modeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        boolean enable = !(modeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES);
        modeManager.setNightMode(enable ? UiModeManager.MODE_NIGHT_YES : UiModeManager.MODE_NIGHT_NO);
    }

    public static void autoNightMode() {
        UiModeManager modeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        modeManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);
    }

    public static void goWechat() {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "微信未安装", Toast.LENGTH_SHORT).show();
        }
    }

//    public static void openExplorer(File file) {
//        Uri uri = Uri.fromFile(file.getParentFile());
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//        intent.setDataAndType(uri, "text/*");
//        if (intent.resolveActivityInfo(context.getPackageManager(), 0) != null) {
//            context.startActivity(intent);
//        } else {
//            Toast.makeText(context, R.string.no_explorer_installed, Toast.LENGTH_SHORT).show();
//        }
//    }
}
