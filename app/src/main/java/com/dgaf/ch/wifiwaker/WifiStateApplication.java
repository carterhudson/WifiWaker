package com.dgaf.ch.wifiwaker;


import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

import timber.log.Timber;

public class WifiStateApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Timber.plant(new Timber.DebugTree());
    AndroidThreeTen.init(this);
  }
}
