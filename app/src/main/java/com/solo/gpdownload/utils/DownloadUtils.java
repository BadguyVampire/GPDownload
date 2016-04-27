package com.solo.gpdownload.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DownloadUtils {

    private static final String TAG = DownloadUtils.class.getName();
    public static final String GP_PKG_NAME = "com.android.vending";

    /**
     * 判断程序是否安装
     */
    public static boolean isApkInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            @SuppressWarnings("unused")
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }


    public static float getScreenWidth(Context context) {
        float width = 0;
        if (context instanceof Activity) {
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            width = dm.widthPixels;
        }
        return width;
    }

    public static float getScreenDensity(Context context) {
        float density = 0;
        if (context instanceof Activity) {
            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
            density = dm.density;
        }
        return density;
    }

    public static int getOrientation(Context context) {
        Configuration mConfiguration = context.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        return ori;
    }

    public static String trimSpace(String text) {
        return text.replaceAll("\\s", "").trim();
    }


    /**
     * 获取当前手机使用的语言
     *
     * @param context
     * @return 语言的代码 如中文是CN
     */
    public static String getLocaleLanguage(Context context) {
        String language = context.getResources().getConfiguration().locale.getLanguage();
        if (language != null) {
            language.toLowerCase(Locale.US);
        }
        return trimSpace(language);
    }

    /**
     * 获取当前国家ISO码<br>
     * 策略:<br>
     * 1.返回SIM卡提供商的国家代码。<br>
     * 2.返回网络所在的国家代码（ISO标准形式)<br>
     * http://countrycode.org/
     *
     * @param context
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String getCountryISOCode(Context context) {
        String isoStr = "us";
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) {
                isoStr = simCountry.toLowerCase(Locale.US);
            } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) {
                    isoStr = networkCountry.toLowerCase(Locale.US);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(isoStr)) {
            try {
                isoStr = context.getResources().getConfiguration().locale.getCountry();
            } catch (Exception e) {
                isoStr = "us";
            }
        }
        return trimSpace(isoStr.toLowerCase());
    }

    /**
     * 获取uuid
     */
    @SuppressLint("DefaultLocale")
    public static String getDeviceUUID(Context mContext) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String uuid = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = uuid.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            for (final byte b : bytes) {
                stringBuilder.append(String.format("%02X", b));
            }

            return trimSpace(stringBuilder.toString().toLowerCase());
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressLint("DefaultLocale")
    public static String getAndroidId(Context mContext) {
        try {
            String androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            return trimSpace(androidId.toLowerCase());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * @return 手机上已安装app列表，系统app除外
     */
    public static ArrayList<String> getInstalledAppList(Context context) {
        ArrayList<String> appList = new ArrayList<String>();
        try {
            List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                //Only get the non-system app info
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appList.add(packageInfo.packageName);
                }

            }
            return appList;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取apk versioncode
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_UNINSTALLED_PACKAGES);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 100;
        }
    }

    public static boolean isRoot() {
        boolean root = false;
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                root = false;
            } else {
                root = true;
            }
        } catch (Exception e) {
        }
        return root;
    }

    public static boolean isAccessibilityEnable(Context context) {

        boolean flag = false;

        boolean accessibilityEnabled = false;
        try {

            String temp = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (TextUtils.isEmpty(temp)) {
                return flag;
            }

            String[] notifications = temp.split(":");

            for (int i = 0; i < notifications.length; i++) {
                ComponentName componentName = ComponentName.unflattenFromString(notifications[i]);

                if ((componentName != null) && (componentName.getPackageName().equals("com.solo.gpdownload"))) {
                    flag = true;
                    break;
                }
            }

            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED,
                    0) == 1;

        } catch (Exception e) {

        }

        return flag && accessibilityEnabled;
    }

    public static void startNotifyAccessibility(Activity context) {
        try {

            Intent intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
            context.startActivity(intent);

        } catch (Exception e) {

        }
    }

    public static boolean isNetConnected(Context context) {
        try {
            ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

    }

    public static boolean isWifiConnectTimeOut(Context context) {
        WifiManager wifi_service = (WifiManager) context
                .getSystemService(Service.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
        if (wifiInfo != null) {
            int rssi = wifiInfo.getRssi();
            if (rssi < -70) {
                // 这个时候网络状况很不好，可认为无网络
                return false;
            }
        }
        return true;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
    }

    public static boolean installPkg(Context ct, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
            i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ct.startActivity(i);
            return true;
        }
        return false;
    }

    public static boolean startGooglePlayOnApp(Context context,String pkg) {
        try {
            context.startActivity(getGpOnAppIntent(pkg, null));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean startGooglePlayOnApp(Context context,String pkg, String source) {
        try {
            context.startActivity(getGpOnAppIntent(pkg, source));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Intent getGpOnAppIntent(String pkg, String source) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(GP_PKG_NAME);
//      intent.setClassName(GP_PKG_NAME, GP_TARGET_CLASS);
        if(TextUtils.isEmpty(source)) {
            intent.setData(Uri.parse("market://details?id=" + pkg));
        } else {
            intent.setData(Uri.parse("market://details?id=" + pkg + "&referrer=utm_source%3D" + source));
        }
        return intent;
    }
}
