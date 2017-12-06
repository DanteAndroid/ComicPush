package com.danteandroid.comicpush.net;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.blankj.utilcode.utils.ToastUtils;
import com.bugtags.library.Bugtags;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.utils.NotifyUtils;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;


/**
 * Created by yons on 17/12/4.
 */

public class DownloadHelper {
    private static final String TAG = "DownloadHelper";
    private final int id;
    Context context;
    private DownloadManager manager;
    private File file;
    private long taskId;
    private String url;
    private String title;
    private String path;
    private boolean failed;


    private DownloadHelper(Context context, int id) {
        this.context = context;
        this.id = id;
    }

    public static DownloadHelper getInstance(Context context, int id) {
        DownloadHelper helper = new DownloadHelper(context, id);
        FileDownloader.setup(context);
        return helper;
    }

    public void downloadToSD(String url, String title) {
        this.url = url;
        this.title = title;

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        final RxPermissions permissions = new RxPermissions((Activity) context);
        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        downWithFileDownloader();
                    } else {
                        ToastUtils.showShortToast(R.string.permission_denied);
                    }
                }, throwable -> Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void startDownload() {
//        NetService.request().download(url)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(responseBody -> {
//                    try {
//                        Log.d(TAG, "call: " + responseBody.string());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }, new HttpErrorAction<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        super.call(throwable);
//                        failed = true;
//                        ToastUtils.showShortToast(context.getString(R.string.download_failed) + errorMessage);
//
//                    }
//                });
    }

    private void downWithManager(String url, String name) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri resource = Uri.parse(url);
        Log.d(TAG, "downWithDownloadManager: " + url);
        if (TextUtils.isEmpty(name)) {
            name = url.substring(url.lastIndexOf('/') + 1);
        }
        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);
        if (file.exists()) {
            boolean result = file.delete();
            Log.i(TAG, "downWithDownloadManager:  oldfile deleted " + result);
        }
        DownloadManager.Request request = new DownloadManager.Request(resource);
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//        request.setVisibleInDownloadsUi(true);
        request.setTitle(name);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        request.setDescription("Downloading...");
        taskId = manager.enqueue(request);
    }

    private void downWithFileDownloader() {
        Log.d(TAG, "downWithFileDownloader: " + url);
        FileDownloader.getImpl().create(url).setPath(path, true)
                .setListener(new FileDownloadSampleListener() {
                    NotifyUtils utils;

                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        ToastUtils.showShortToast(R.string.download_start);
                        utils = NotifyUtils.getInstance(id);
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                        utils.startDownload(task.getFilename());
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        int progress = Math.round(((float) soFarBytes / totalBytes) * 100);
                        utils.update(progress, task.getSpeed() + "KB/s");
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        utils.finished(task.getPath());
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        if (e.getMessage().contains("503")) {
                            ToastUtils.showShortToast(R.string.download_too_frequent);
                        } else {
                            ToastUtils.showShortToast(e.getMessage());
                        }
                        Bugtags.sendException(e);
                    }
                })
                .start();
    }

}
