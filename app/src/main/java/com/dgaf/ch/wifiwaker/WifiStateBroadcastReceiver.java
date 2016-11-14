package com.dgaf.ch.wifiwaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.Locale;

import timber.log.Timber;


public class WifiStateBroadcastReceiver extends BroadcastReceiver {
  private WifiManager       wifiManager;
  private SharedPreferences sharedPreferences;
  private DateTimeFormatter dateTimeFormatter;

  @Override
  public void onReceive(Context context, Intent intent) {
    initOnce(context);
    String action = intent.getAction();
    switch (action) {
      case WifiManager.WIFI_STATE_CHANGED_ACTION:
        Timber.d("Wifi state change action received");
        handleWiFiStateChange();
        break;

      default:
        Timber.d("Unsupported intent action");
        break;
    }
  }

  private void initOnce(Context context) {
    if (wifiManager == null) {
      wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    if (sharedPreferences == null) {
      sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCES,
                                                       Context.MODE_PRIVATE);
    }
  }

  private void handleWiFiStateChange() {
    if (wifiManager.isWifiEnabled()) {
      Timber.d("Wifi enabled; Ignoring state change");
      return;
    }

    if (dateTimeFormatter == null) {
      dateTimeFormatter = DateTimeFormatter.ofPattern("MM.dd.YY hh:mm");
    }

    String when = Instant.now().atZone(ZoneId.systemDefault()).format(dateTimeFormatter);
    if (wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled())) {
      long localCount;
      sharedPreferences.edit()
                       .putLong(Constants.KEY_COUNT, localCount = getCount() + 1)
                       .putString(Constants.KEY_WHEN, when)
                       .apply();
      Timber.d("Tagged count %d @ %s", localCount, when);
      Timber.d("Re-enabled wifi");
    }
  }

  private long getCount() {
    return sharedPreferences.getLong(Constants.KEY_COUNT, 0);
  }
}
