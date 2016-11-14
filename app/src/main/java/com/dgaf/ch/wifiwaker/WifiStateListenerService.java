package com.dgaf.ch.wifiwaker;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;

import java.util.Locale;

import timber.log.Timber;

public class WifiStateListenerService extends Service {
  private static final int WIFIWAKER     = 999;
  private static final int REQUEST_RESET = 888;

  private NotificationManager                                notificationManager;
  private SharedPreferences                                  sharedPreferences;
  private WifiStateServiceBinder                             wifiStateServiceBinder;
  private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
  private WifiStateBroadcastReceiver                         wifiStateReceiver;

  public static Intent getIntent(Context context) {
    return new Intent(context, WifiStateListenerService.class);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    initReceiver();
    initCountListener();
    initNotification();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Timber.d("Starting sticky...");
    boolean haveIntent    = intent != null;
    String  action        = "";
    boolean haveAction    = haveIntent && (action = intent.getAction()) != null;
    boolean isResetAction = haveAction && action.equals(Constants.ACTION_RESET);
    if (isResetAction) {
      setCount(0);
    }

    Timber.d("%s", this);

    return START_STICKY;
  }

  private void setCount(int count) {
    sharedPreferences.edit().putLong(Constants.KEY_COUNT, count).apply();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    if (wifiStateServiceBinder == null) {
      wifiStateServiceBinder = new WifiStateServiceBinder();
    }

    return wifiStateServiceBinder;
  }

  private void initReceiver() {
    registerReceiver(wifiStateReceiver = new WifiStateBroadcastReceiver(),
                     new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Timber.d("Wifi listener service terminating...");
    performCleanup();
  }

  private void performCleanup() {
    unregisterReceiver(wifiStateReceiver);
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    preferenceChangeListener = null;
    notificationManager.cancel(WIFIWAKER);
  }

  public class WifiStateServiceBinder extends Binder {
    WifiStateListenerService getService() {
      return WifiStateListenerService.this;
    }
  }

  private void initCountListener() {
    if (sharedPreferences == null) {
      sharedPreferences = getSharedPreferences(Constants.KEY_PREFERENCES, MODE_PRIVATE);
    }

    if (preferenceChangeListener == null) {
      preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
          if (key.equals(Constants.KEY_COUNT)) {
            updateNotification();
          }
        }
      };
    }

    sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
  }

  private void updateNotification() {
    notificationManager.notify(WIFIWAKER,
                               recoverNotificationBuilder().setContentText(getContentText())
                                                           .build());
  }

  public Notification.Builder recoverNotificationBuilder() {
    StatusBarNotification[] active    = notificationManager.getActiveNotifications();
    StatusBarNotification   retrieved = null;
    for (StatusBarNotification notification : active) {
      if (notification.getId() == WIFIWAKER) {
        retrieved = notification;
        break;
      }
    }

    if (retrieved == null) {
      Timber.w("Failed to retrieve WiFi Waker notification for count update.");
      return new Notification.Builder(this);
    }

    return Notification.Builder.recoverBuilder(getApplicationContext(),
                                               retrieved.getNotification());
  }

  private void initNotification() {
    if (notificationManager == null) {
      notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    Intent resetIntent = WifiStateListenerService.getIntent(this).setAction(Constants.ACTION_RESET);
    PendingIntent pendingResetIntent = PendingIntent.getService(this,
                                                                REQUEST_RESET,
                                                                resetIntent,
                                                                0);
    Notification.Action resetAction = new Notification.Action.Builder(null,
                                                                      "RESET",
                                                                      pendingResetIntent).build();
    Notification notification = new Notification.Builder(getApplicationContext())
        .setSmallIcon(android.R.drawable.ic_menu_zoom)
        .setOngoing(true)
        .setContentTitle("WiFi Waker")
        .setContentText(getContentText())
        .addAction(resetAction)
        .setContentIntent(PendingIntent.getActivity(this,
                                                    Constants.REQUEST_CODE_FROM_NOTIFICATION,
                                                    MainActivity.newIntent(this),
                                                    0))
        .build();

    notificationManager.notify(WIFIWAKER, notification);
  }

  private long getCount() {
    return sharedPreferences.getLong(Constants.KEY_COUNT, 0);
  }

  private String getWhen() {
    return sharedPreferences.getString(Constants.KEY_WHEN, "");
  }

  private String getContentText() {
    return String.format(Locale.getDefault(),
                         "Re-enabled %d times.  Last occurrence was %s",
                         getCount(),
                         getWhen());
  }
}
