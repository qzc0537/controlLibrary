package com.rhizo.libcontrol.util;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;

import com.rhizo.libcontrol.bean.WifiBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConnectWifiUtils {

    private static final String TAG = ConnectWifiUtils.class.getSimpleName();

    private final ConnectivityManager mConnectivityManager;//连接管理者

    private final WifiManager mWifiManager;//Wifi管理者

    private WifiConnectCallback mWifiConnectCallback;

    @SuppressLint("StaticFieldLeak")
    private static volatile ConnectWifiUtils mInstance;

    private final Context mContext;

    public ConnectWifiUtils(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            IntentFilter  intentFilter = new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
            mContext.registerReceiver(mWifiScanReceiver, intentFilter);
            mIsRegistered = true;
        }
    }

//    public static ConnectWifiUtils initialize(Context context) {
//        if (mInstance == null) {
//            synchronized (ConnectWifiUtils.class) {
//                if (mInstance == null) {
//                    mInstance = new ConnectWifiUtils(context);
//                }
//            }
//        }
//        return mInstance;
//    }

    public void setWifiConnectCallback(WifiConnectCallback wifiConnectCallback) {
        this.mWifiConnectCallback = wifiConnectCallback;
    }

    /**
     * 连接Wifi
     *
     * @param wifiBean 扫描结果
     */
    public void connectWifi(WifiBean wifiBean) {
        if (mConnecting) {
            Log.e(TAG, "正在连接中，请勿重复连接");
            return;
        }
        mConnecting = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectBySuggestion(wifiBean);
        } else {
            connectByOld(wifiBean);
        }
    }

    /**
     * Android 10 以下使用
     *
     * @param wifiBean 扫描结果
     */
    private void connectByOld(WifiBean wifiBean) {
        if (mWifiConnectCallback != null) {
            mWifiConnectCallback.onWifiConnectLog("开始配置WiFi-connectByOld");
        }
        String ssid = wifiBean.getSsid();
        boolean isSuccess;
        WifiConfiguration configured = isExist(ssid);
        if (configured != null) {
            //在配置表中找到了，直接连接
            isSuccess = mWifiManager.enableNetwork(configured.networkId, true);
        } else {
            WifiConfiguration wifiConfig = createWifiConfig(ssid, wifiBean.getPassword(), getCipherType(wifiBean.getCapabilities()));
            int netId = mWifiManager.addNetwork(wifiConfig);
            isSuccess = mWifiManager.enableNetwork(netId, true);
        }
        Log.d(TAG, "connectWifi: " + (isSuccess ? "成功" : "失败"));
        mConnecting = false;
        if (mWifiConnectCallback != null) {
            if (isSuccess) {
                mWifiConnectCallback.onWifiConnectSuccess();
            } else {
                mWifiConnectCallback.onWifiConnectFailure();
            }
        }
    }

    /**
     * Android 10及以上版本使用此方式连接Wifi
     *
     * @param ssid     名称
     * @param password 密码
     */
    @SuppressLint("NewApi")
    private void connectByNew(String ssid, String password) {
        WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();
        //网络请求
        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build();
        //网络回调处理
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                if (mWifiConnectCallback != null) {
                    mWifiConnectCallback.onWifiConnectSuccess();
                    Log.d(TAG, "======onAvailable: ====连接成功======");
                }
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Log.d(TAG, "======onAvailable: ====连接失败======");
                if (mWifiConnectCallback != null) {
                    mWifiConnectCallback.onWifiConnectFailure();
                }
            }
        };

        //请求连接网络
        mConnectivityManager.requestNetwork(request, networkCallback);
    }

    @SuppressLint("NewApi")
    private void connectBySuggestion(WifiBean wifiBean) {
        if (mWifiConnectCallback != null) {
            mWifiConnectCallback.onWifiConnectLog("开始配置WiFi-connectBySuggestion");
        }
        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(wifiBean.getSsid())
                .setWpa2Passphrase(wifiBean.getPassword())
                .setIsAppInteractionRequired(true)
                .build();
        List<WifiNetworkSuggestion> suggestionList = new ArrayList<>();
        suggestionList.add(suggestion);
        int status = mWifiManager.addNetworkSuggestions(suggestionList);
        if (mWifiConnectCallback != null) {
            mWifiConnectCallback.onWifiConnectLog("配置WiFi state:" + status);
        }
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            Log.e(TAG, "status=" + status);
            mConnecting = false;
            if (mWifiConnectCallback != null) {
                mWifiConnectCallback.onWifiConnectFailure();
            }
            return;
        }
        Log.d(TAG, "STATUS_NETWORK_SUGGESTIONS_SUCCESS");
    }

    private boolean mConnecting = false;
    private boolean mIsRegistered = false;
    BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "======onReceive:" + intent.getAction());
            String action = intent.getAction();
            if (mWifiConnectCallback != null) {
                mWifiConnectCallback.onWifiConnectLog("onReceive:" + action);
            }
            if (Objects.equals(action, WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                mConnecting = false;
                if (mWifiConnectCallback != null) {
                    mWifiConnectCallback.onWifiConnectSuccess();
                }
            }
        }
    };

    /**
     * 取消注册广播
     */
    public void unregisterReceiver() {
        if (mIsRegistered) {
            mContext.unregisterReceiver(mWifiScanReceiver);
        }
    }

    /**
     * 创建Wifi配置
     *
     * @param ssid     名称
     * @param password 密码
     * @param type     类型
     */
    private WifiConfiguration createWifiConfig(String ssid, String password, WifiCapability type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        WifiConfiguration configured = isExist(ssid);
        if (configured != null) {
            mWifiManager.removeNetwork(configured.networkId);
            mWifiManager.saveConfiguration();
        }

        //不需要密码的场景
        if (type == WifiCapability.WIFI_CIPHER_NO_PASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //以WEP加密的场景
        } else if (type == WifiCapability.WIFI_CIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
        } else if (type == WifiCapability.WIFI_CIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 网络是否连接
     */
    @SuppressLint("NewApi")
    public static boolean isNetConnected(ConnectivityManager connectivityManager) {
        return connectivityManager.getActiveNetwork() != null;
    }

    /**
     * 连接网络类型是否为Wifi
     */
    @SuppressLint("NewApi")
    public static boolean isWifi(ConnectivityManager connectivityManager) {
        if (connectivityManager.getActiveNetwork() == null) {
            return false;
        }
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (networkCapabilities == null) {
            return false;
        }
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * 配置表是否存在对应的Wifi配置
     *
     * @param SSID
     * @return
     */
    @SuppressLint("MissingPermission")
    private WifiConfiguration isExist(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiCapability getCipherType(String capabilities) {
        if (capabilities.contains("WEB")) {
            return WifiCapability.WIFI_CIPHER_WEP;
        } else if (capabilities.contains("PSK")) {
            return WifiCapability.WIFI_CIPHER_WPA;
        } else if (capabilities.contains("WPS")) {
            return WifiCapability.WIFI_CIPHER_NO_PASS;
        } else {
            return WifiCapability.WIFI_CIPHER_NO_PASS;
        }
    }

    /**
     * wifi连接回调接口
     */
    public interface WifiConnectCallback {

        void onWifiConnectSuccess();

        void onWifiConnectFailure();

        void onWifiConnectLog(String msg);
    }

    public enum WifiCapability {
        WIFI_CIPHER_WEP, WIFI_CIPHER_WPA, WIFI_CIPHER_NO_PASS
    }

}
