package com.solo.gpdownload;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.solo.gpdownload.utils.DownloadUtils;

public class MainActivity extends Activity implements View.OnClickListener, ServiceConnection {

    private DownloadService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent service = new Intent(this, DownloadService.class);
        bindService(service, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        findViewById(R.id.btn_download).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!DownloadUtils.isAccessibilityEnable(this)) {
            DownloadUtils.startNotifyAccessibility(this);
        }
        Log.d("1234", "path:" + Environment.getDataDirectory());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_download:
                if (DownloadUtils.isNetConnected(this) && DownloadUtils.isRoot() && DownloadUtils.isApkInstalled(this, DownloadUtils.GP_PKG_NAME)) {
                    if (!DownloadUtils.isApkInstalled(this, "home.solo.launcher.free")) {
                        DownloadUtils.startGooglePlayOnApp(this, "home.solo.launcher.free");
                    }
                }
                break;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service != null) {
           // mService = service.;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Intent service = new Intent(this, DownloadService.class);
        startService(service);
    }
}
