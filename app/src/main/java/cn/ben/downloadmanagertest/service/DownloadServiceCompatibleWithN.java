package cn.ben.downloadmanagertest.service;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

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
//              The package installer only supports content schemes starting on Android 7.0.
//              Prior to that — and despite documentation to the contrary — the package installer only supports file schemes.
//              You will need to set the Uri on your Intent differently based on whether you are running on Android 7.0+ or not, such as by branching on Build.VERSION.SDK_INT.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) install(context);
                else installCompatible(context);
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

    @SuppressWarnings("unused")
    private void install(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //沒有在activity環境下啟動
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Android7.0执行了“StrictMode API 政策禁”
        //可以用FileProvider来解决这一问题
        //私有目录被限制访问
        //“私有目录被限制访问“ 是指在Android7.0中为了提高私有文件的安全性，面向 Android N 或更高版本的应用私有目录将被限制访问。这点类似iOS的沙盒机制。
        //" StrictMode API 政策" 是指禁止向你的应用外公开 file:// URI。 如果一项包含文件 file:// URI类型 的 Intent 离开你的应用，应用失败，并出现 FileUriExposedException 异常。
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "1.apk")), "application/vnd.android.package-archive");// TODO: 2016/10/17
        context.startActivity(intent);
    }

    //将之前Uri改成了有FileProvider创建一个content类型的Uri
    private void installCompatible(Context context) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "1.apk");
        Uri apkUri = FileProvider.getUriForFile(context, "cn.ben.downloadmanagertest.fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        //沒有在activity環境下啟動
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // TODO: 2016/10/17 對目標應用臨時授權該Uri所代表的文件
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");// TODO: 2016/10/17
        context.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
