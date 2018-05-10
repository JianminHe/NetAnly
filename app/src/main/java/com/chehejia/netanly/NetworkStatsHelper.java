package com.chehejia.netanly;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

import static android.content.Context.NETWORK_STATS_SERVICE;

/**
 * Created by chj1090 on 2018/5/9.
 */
@TargetApi(Build.VERSION_CODES.M)
public class NetworkStatsHelper {

    NetworkStatsManager networkStatsManager;

    public long getAllRxBytesMobile(Context context) {
        return getAllRxBytesMobile(context, 0, System.currentTimeMillis());
    }

    public long getAllRxBytesMobile(Context context, long startTime, long endTime) {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    startTime,
                    endTime);
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getRxBytes();
    }

    public long getAllTxBytesMobile(Context context) {
        return getAllTxBytesMobile(context, 0, System.currentTimeMillis());
    }

    public long getAllTxBytesMobile(Context context, long startTime, long endTime) {
        NetworkStats.Bucket bucket;
        try {
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    startTime,
                    endTime);
        } catch (RemoteException e) {
            return -1;
        }
        return bucket.getTxBytes();
    }

    public NetworkStatsHelper(NetworkStatsManager networkStatsManager) {
        this.networkStatsManager = networkStatsManager;
    }

    public long getPackageRxBytesMobile(Context context) {
        return getPackageRxBytesMobile(context, 0, System.currentTimeMillis());
    }

    public long getPackageRxBytesMobile(Context context, long startTime, long endTime) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetails(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    startTime,
                    endTime);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        networkStats.getNextBucket(bucket);
        return bucket.getRxBytes();
    }

    public long getPackageTxBytesMobile(Context context) {
        return getPackageTxBytesMobile(context, 0,  System.currentTimeMillis());

    }

    public long getPackageTxBytesMobile(Context context, long startTime, long endTime) {
        NetworkStats networkStats = null;
        try {
            networkStats = networkStatsManager.queryDetails(
                    ConnectivityManager.TYPE_MOBILE,
                    getSubscriberId(context, ConnectivityManager.TYPE_MOBILE),
                    startTime,
                    endTime);
        } catch (RemoteException e) {
            return -1;
        }
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        networkStats.getNextBucket(bucket);
        return bucket.getTxBytes();
    }

    private String getSubscriberId(Context context, int networkType) throws SecurityException {
        if (ConnectivityManager.TYPE_MOBILE == networkType) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getSubscriberId();
        }
        return "";
    }
}
