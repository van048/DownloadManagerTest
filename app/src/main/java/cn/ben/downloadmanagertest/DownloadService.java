package cn.ben.downloadmanagertest;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import java.io.File;

public class DownloadService extends Service {

    private Context mContext;
    private long mTaskId;
    private String versionName;
    private DownloadManager downloadManager;
    //广播接受者，接收下载状态
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkDownloadStatus();//检查下载状态
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        downloadAPK("https://dl.wandoujia.com/files/jupiter/latest/wandoujia-web_seo_baidu_homepage.apk", "wandoujia-web_seo_baidu_homepage.apk");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //使用系统下载器下载
    @SuppressWarnings("SameParameterValue")
    private void downloadAPK(@NonNull String versionUrl, @NonNull String versionName) {
        this.versionName = versionName;

        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
        //漫游网络是否可以下载
        request.setAllowedOverRoaming(false);

        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));

        if (mimeString == null) {
            MLog.i("mimeString null");
            return;
        }

        request.setMimeType(mimeString);
        //在通知栏中显示，默认就是显示的
        // or when it is completed
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true); // TODO: 2016/8/11
        //sdcard的目录下的download文件夹，必须设置
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, versionName);
        //request.setDestinationInExternalFilesDir(),也可以自己制定下载路径 // TODO: 2016/8/11

        //将下载请求加入下载队列
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        mTaskId = downloadManager.enqueue(request);
        //注册广播接收者，监听下载状态
        mContext.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //检查下载状态
    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数

        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            // TODO: 2016/8/11 getInt
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    MLog.i(">>>下载暂停");
                    break;
                case DownloadManager.STATUS_PENDING:
                    MLog.i(">>>下载延迟");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    MLog.i(">>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    MLog.i(">>>下载完成");
                    //下载完成安装APK
                    String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + versionName;
                    installAPK(new File(downloadPath));
                    break;
                case DownloadManager.STATUS_FAILED:
                    MLog.i(">>>下载失败");
                    break;
            }
        }
    }

    //下载到本地后执行安装
    private void installAPK(File file) {
        if (!file.exists()) return;
        // TODO: 2016/8/11
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + file.toString());
        // TODO: 2016/8/11
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        //在服务中开启activity必须设置flag,后面解释
        // TODO: 2016/8/11
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

        stopSelf();
    }

    @Override
    public void onDestroy() {
        mContext.unregisterReceiver(receiver);
        super.onDestroy();
    }
}
