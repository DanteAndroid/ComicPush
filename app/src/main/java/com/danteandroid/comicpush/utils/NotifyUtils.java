package com.danteandroid.comicpush.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.danteandroid.comicpush.Constants;
import com.danteandroid.comicpush.R;
import com.danteandroid.comicpush.base.App;

import java.io.File;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by yons on 17/12/4.
 */

public class NotifyUtils {
    private static NotificationManager mNotifyManager =
            (NotificationManager) App.context.getSystemService(Context.NOTIFICATION_SERVICE);
    private static NotificationCompat.Builder mBuilder;
    private Context context = App.context;
    private int id;

    public NotifyUtils(int id) {
        this.id = id;
    }

    public static NotifyUtils getInstance(int id) {
        return new NotifyUtils(id);
    }

    public static Intent getExplorerIntent(String path) {
        Uri uri = Uri.fromFile(new File(path));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "text/*");
        if (intent.resolveActivityInfo(App.context.getPackageManager(), 0) != null) {
            return intent;
        } else {
            Toast.makeText(App.context, "未找到文件管理器", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public void startDownload(String title) {
        mBuilder = new NotificationCompat.Builder(App.context, "channel_id");
        mBuilder.setContentTitle(title.replace(Constants.VOL_PREFIX, ""))
                .setContentText(context.getString(R.string.download_hard))
                .setSmallIcon(R.mipmap.ic_launcher);
    }

    public void update(int progress, String speed) {
        mBuilder.setProgress(100, progress, false);
        mBuilder.setContentText(String.format(context.getString(R.string.download_progress_content), progress, speed));
        mNotifyManager.notify(id, mBuilder.build());
    }

    public void finished(String path) {
        PendingIntent pendingIntent = PendingIntent.getActivity(App.context, 0, getExplorerIntent(path), 0);
        mBuilder.setContentText(context.getString(R.string.download_finished))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setProgress(0, 0, false);
        mNotifyManager.notify(id, mBuilder.build());
    }
}
