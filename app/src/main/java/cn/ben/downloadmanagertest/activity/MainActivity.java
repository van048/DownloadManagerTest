package cn.ben.downloadmanagertest.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cn.ben.downloadmanagertest.R;
import cn.ben.downloadmanagertest.service.DownloadService;
import cn.ben.downloadmanagertest.service.DownloadServiceCompatibleWithN;

public class MainActivity extends AppCompatActivity {

    private DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
//        downloadManagerSimpleUse("https://dl.wandoujia.com/files/jupiter/latest/wandoujia-web_seo_baidu_homepage.apk", "1.apk");
    }

    @SuppressWarnings("unused")
    private void downloadManagerSimpleUse(@NonNull String downloadUrl, @NonNull String fileName) {
        //创建下载任务,downloadUrl就是下载链接
        // encoded URI string, an RFC-2396 compliant
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        //指定下载路径和下载文件名
        // the local destination, not scanned by MediaScanner
        // subPath
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        //将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);
    }

    public void download(@SuppressWarnings("UnusedParameters") View view) {
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
    }

    public void downloadCompatible(@SuppressWarnings("UnusedParameters") View view) {
        Intent intent = new Intent(this, DownloadServiceCompatibleWithN.class);
        startService(intent);
    }
}
