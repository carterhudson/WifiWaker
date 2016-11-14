package com.dgaf.ch.wifiwaker;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
  private ServiceConnection        serviceConnection;
  private WifiStateListenerService service;
  private boolean                  bound;

  @BindView(R.id.cb_listener_toggle) SwitchCompat    serviceSwitch;
  private                            ActivityManager activityManager;

  public static Intent newIntent(Context context) {
    return new Intent(context, MainActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    bindToService();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (bound) {
      unbindService(serviceConnection);
      setUnboundState();
    }
  }

  @OnCheckedChanged(R.id.cb_listener_toggle)
  public void toggle(SwitchCompat serviceSwitch) {
    if (serviceSwitch.isChecked()) {
      setOnState();
      return;
    }

    setOffState();
  }

  private boolean serviceIsRunning() {
    if (activityManager == null) {
      activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    }

    List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(
        Integer.MAX_VALUE);
    for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
      boolean foundService = serviceInfo.service.getClassName()
                                                .equals(WifiStateListenerService.class.getName());
      if (foundService) {
        return true;
      }
    }

    return false;
  }

  private void setBoundState(WifiStateListenerService.WifiStateServiceBinder iBinder) {
    service = iBinder.getService();
    bound = service != null;
    serviceSwitch.setChecked(bound);
  }

  private void setOffState() {
    if (!bound) {
      Timber.d("Not currently bound to a service");
      return;
    }

    unbindService(serviceConnection);
    service.stopSelf();
    serviceSwitch.setChecked(false);
    setUnboundState();
  }

  private void setUnboundState() {
    serviceConnection = null;
    service = null;
    bound = false;
  }

  private void setOnState() {
    startServiceIfNeeded();
    bindToService();
  }

  private void bindToService() {
    if (!serviceIsRunning()) {
      Timber.d("No service running to bind to");
      return;
    }

    Intent serviceIntent = WifiStateListenerService.getIntent(this);
    if (serviceConnection == null) {
      serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
          if (iBinder instanceof WifiStateListenerService.WifiStateServiceBinder) {
            setBoundState((WifiStateListenerService.WifiStateServiceBinder) iBinder);
          }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
          bound = false;
        }
      };
    }

    bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
  }

  private void startServiceIfNeeded() {
    Intent serviceIntent = WifiStateListenerService.getIntent(getApplicationContext());
    if (serviceIsRunning()) {
      Timber.d("Ignoring call to bind when already bound");
      return;
    }

    startService(serviceIntent);
  }
}
