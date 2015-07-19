package me.flyingrub.mibandnotify.notification;

/**
 * Created by fly on 19/07/15.
 */

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import me.flyingrub.mibandnotify.bluetooth.BLECommunicationManager;
import me.flyingrub.mibandnotify.data.WhitelistedApp;
import me.flyingrub.mibandnotify.miband.MiBandCommunicationService;

public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    private final String TAG = this.getClass().getSimpleName();

    private static boolean isNotificationAccessEnabled = false;

    private static MiBandCommunicationService miBandCommunicationService;

    @Override
    public IBinder onBind(Intent mIntent) {
        IBinder mIBinder = super.onBind(mIntent);
        Log.i(TAG, "onBind");
        isNotificationAccessEnabled = true;
        miBandCommunicationService = new MiBandCommunicationService(new BLECommunicationManager(this));
        return mIBinder;
    }

    @Override
    public boolean onUnbind(Intent mIntent) {
        boolean mOnUnbind = super.onUnbind(mIntent);
        Log.i(TAG, "onUnbind");
        isNotificationAccessEnabled = false;
        try {
        } catch (Exception e) {
            Log.e(TAG, "Error during unbind", e);
        }
        return mOnUnbind;
    }

    public static boolean isNotificationAccessEnabled() {
        return isNotificationAccessEnabled;
    }

    private boolean filter(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> blacklistedApp = defaultSharedPreferences.getStringSet("BlackList", new HashSet<String>());
        return
                notification != null
                        // Filter low priority notifications
                        && notification.priority >= 0
                        // Notification flags
                        && !isOngoing(notification)
                        && !isLocalOnly(notification)
                        && blacklistedApp.contains(sbn.getPackageName());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private boolean isLocalOnly(Notification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            return false;
        }
        boolean local = (notification.flags & Notification.FLAG_LOCAL_ONLY) != 0;
        Log.d(TAG, String.format("Notification is local: %1s", local));
        return local;

    }

    private boolean isOngoing(Notification notification) {
        boolean ongoing = (notification.flags & Notification.FLAG_ONGOING_EVENT) != 0;
        Log.d(TAG, String.format("Notification is ongoing: %1s", ongoing));
        return ongoing;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Notification from " + sbn.getPackageName() + ", prio=" + sbn.getNotification().priority);
        if (!miBandCommunicationService.isBlinking() && filter(sbn)) {
            miBandCommunicationService.onNotificationPosted();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification from " + sbn.getPackageName() + " removed");
        miBandCommunicationService.onNotificationRemoved();
    }
}
