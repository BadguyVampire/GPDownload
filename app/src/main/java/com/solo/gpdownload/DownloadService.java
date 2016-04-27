package com.solo.gpdownload;

import android.accessibilityservice.AccessibilityService;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.solo.gpdownload.utils.DownloadUtils;
import com.solo.gpdownload.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created on 16-3-31.
 */
public class DownloadService extends AccessibilityService {

    private static final String SRC_FILE = "/data/app/";
    private static final String DES_FILE = "/sdcard/apkfiles/";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                AccessibilityNodeInfo install = event.getSource();
                if (install != null) {

                    List<AccessibilityNodeInfo> list = install.findAccessibilityNodeInfosByText("安装");
                    if (null != list) {
                        for (AccessibilityNodeInfo info : list) {
                            if (TextUtils.equals(info.getText(), "安装")) {
                                //找到你的节点以后 就直接点击他就行了
                                Log.d("1234", "find install");
                                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }
                }

                AccessibilityNodeInfo accept = event.getSource();

                if (accept != null) {

                    List<AccessibilityNodeInfo> accessibilityNodeInfos = accept.findAccessibilityNodeInfosByText("接受");
                    if (accessibilityNodeInfos != null) {
                        for (AccessibilityNodeInfo info : accessibilityNodeInfos) {
                            if (TextUtils.equals(info.getText(), "接受")) {
                                Log.d("1234", "find accept");
                                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }
                }

                AccessibilityNodeInfo finish = event.getSource();
                if (finish != null) {
                    List<AccessibilityNodeInfo> finishInfo = finish.findAccessibilityNodeInfosByText("打开");
                    if (finishInfo != null) {
                        for (AccessibilityNodeInfo info : finishInfo) {
                            if (TextUtils.equals(info.getText(), "打开")) {
                                //TODO:下载下一个应用，复制apk
                                Log.d("1234", "find finish");
                                copyApkFile("home.solo.launcher.free");
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void copyApkFile(String pkg) {
        File src = new File(SRC_FILE + pkg + "-1", "base.apk");
        File des = new File(DES_FILE, pkg.replaceAll("\\.", "_") + ".apk");

        try {
            FileUtils.copyFile(src, des);
        } catch (IOException e) {
            Log.d("1234", "copy apk error:" + e.getMessage());
        }
    }

    private void downloadApk(String pkg) {
        if (!DownloadUtils.isApkInstalled(getApplicationContext(), pkg)) {
            DownloadUtils.startGooglePlayOnApp(getApplicationContext(), pkg);
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

    }
}
