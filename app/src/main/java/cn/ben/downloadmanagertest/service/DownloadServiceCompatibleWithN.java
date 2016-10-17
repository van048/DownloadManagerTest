package cn.ben.downloadmanagertest.service;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;

import rx.functions.Action1;

public class DownloadServiceCompatibleWithN extends Service {

    private BroadcastReceiver receiver;
    @SuppressWarnings("FieldCanBeLocal")
    private DownloadManager dm;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private long enqueue;
    private final String downloadUrl = "https://dl.wandoujia.com/files/jupiter/latest/wandoujia-web_seo_baidu_homepage.apk";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                install(context);
                stopSelf();
            }
        };
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        RxPermissions.getInstance(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            startDownload(downloadUrl);
                        } else {
                            stopSelf();
                        }
                    }
                });
        return Service.START_STICKY; // TODO: 2016/10/17
    }

    private void startDownload(String downloadUrl) {
        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setMimeType("application/vnd.android.package-archive"); // TODO: 2016/10/17
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "1.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("下載新版本");
        enqueue = dm.enqueue(request);
    }

    private void install(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //沒有在activity環境下啟動
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "1.apk")), "application/vnd.android.package-archive");// TODO: 2016/10/17
        context.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
