package com.iris0513.device.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

public class DeviceUtils {

    /**
     * 获取手机 IMEI
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return checkDeviceId(tm.getDeviceId());
        } catch (Exception e) {
        }
        return null;
    }

    private static String checkDeviceId(String deviceId) {
        if (!TextUtils.isEmpty(deviceId))
            return deviceId;
        try {
            return "35" +
                    Build.BOARD.length() % 10 +
                    Build.BRAND.length() % 10 +
                    Build.CPU_ABI.length() % 10 +
                    Build.DEVICE.length() % 10 +
                    Build.DISPLAY.length() % 10 +
                    Build.HOST.length() % 10 +
                    Build.ID.length() % 10 +
                    Build.MANUFACTURER.length() % 10 +
                    Build.MODEL.length() % 10 +
                    Build.PRODUCT.length() % 10 +
                    Build.TAGS.length() % 10 +
                    Build.TYPE.length() % 10 +
                    Build.USER.length() % 10;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取手机 IMSI
     *
     * @param context
     * @return
     */
    public static String getSubscriberId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取设备名称
     *
     * @return
     */
    public static String getModel() {
        return Build.MODEL;
    }

    /**
     * 获取设备名称
     *
     * @return
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 获取制造商
     *
     * @return
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取安卓系统版本号
     *
     * @return
     */
    public static int getAndroidOSVersion() {
        return Build.VERSION.SDK_INT;
    }

    private static String defaultUserAgent;

    private static String getDefaultUserAgent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                return WebSettings.getDefaultUserAgent(context);
            } catch (Exception e) {
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            try {
                final Class<?> webSettingsClassicClass = Class.forName("android.webkit.WebSettingsClassic");
                final Class<?> webViewClassicClass = Class.forName("android.webkit.WebViewClassic");
                final Constructor<?> constructor = webSettingsClassicClass.getDeclaredConstructor(Context.class, webViewClassicClass);
                constructor.setAccessible(true);
                final Object object = constructor.newInstance(context, null);
                final Method method = webSettingsClassicClass.getMethod("getUserAgentString");
                return (String) method.invoke(object);
            } catch (Exception e) {
            }
        } else {
            try {
                Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(Context.class, WebView.class);
                constructor.setAccessible(true);
                WebSettings settings = constructor.newInstance(context, null);
                return settings.getUserAgentString();
            } catch (Exception e) {
            }
        }
        try {
            WebView webView = new WebView(context);
            WebSettings webSettings = webView.getSettings();
            return webSettings.getUserAgentString();
        } catch (Exception e) {
        }
        return System.getProperty("http.agent");
    }

    /**
     * 获取系统默认UA
     *
     * @param context
     * @return
     */
    public static String getUserAgent(Context context) {
        if (defaultUserAgent == null) {
            synchronized (DeviceUtils.class) {
                if (defaultUserAgent == null) {
                    defaultUserAgent = getDefaultUserAgent(context);
                }
            }
        }
        return defaultUserAgent;
    }

    /**
     * 判断是否为wifi
     *
     * @param context
     * @return
     */
    public static boolean isWifi(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (Exception e) {
        }
        return false;
    }

    public static String randomDeviceUUID(Context context) {
        UUID uuid;
        final String androidId = getAndroidId(context);
        if (androidId != null && !"9774d56d682e549c".equals(androidId)) {
            uuid = UUID.nameUUIDFromBytes(androidId.getBytes());
        } else {
            final String deviceId = getDeviceId(context);
            uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes()) : UUID.randomUUID();
        }
        return uuid.toString();
    }

    public static String getAndroidId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        return null;
    }

    public static String getWifiMacAddress(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm.isWifiEnabled()) {
                WifiInfo info = wm.getConnectionInfo();
                return info.getMacAddress();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static int getScreenWidth(Context context) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getSIMProvider(Context context) {
        int provider = 0;
        String IMSI = getSubscriberId(context);
        if (IMSI != null) {
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                provider = 3;
            } else if (IMSI.startsWith("46001")) {
                provider = 1;
            } else if (IMSI.startsWith("46003")) {
                provider = 2;
            }
        }
        return provider;
    }

    public static String getIpAddress(Context context) {
        try {
            // 获取WiFi服务
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            // 判断WiFi是否开启
            if (wifiManager.isWifiEnabled()) {
                // 已经开启了WiFi
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                String ip = intToIp(ipAddress);
                return ip;
            } else {
                // 未开启WiFi
                return getIpAddress();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static String getIpAddress() {
        try {
            NetworkInterface networkInterface;
            InetAddress inetAddress;
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                networkInterface = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
            return null;
        } catch (Exception e) {

        }
        return null;
    }

    private static String intToIp(int ipAddress) {
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    public static String getLocale(Context context) {
        return context.getResources().getConfiguration().locale.getCountry();
    }

    public static int getNetworkClass(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null || !info.isConnected()) {
                return 0; //not connected
            }
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return 100;
            }
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int networkType = info.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        return 2;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        return 3;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        return 4;
                    default:
                        return 1;
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public static boolean isScreenOn(Context context) {
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isScreenOn();
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean isUnlock(Context context) {
        try {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            return keyguardManager.inKeyguardRestrictedInputMode();
        } catch (Exception e) {
        }
        return false;
    }

    public static String getBoard() {
        return Build.BOARD;
    }

    public static String getCpuAbi() {
        return Build.CPU_ABI;
    }
}
