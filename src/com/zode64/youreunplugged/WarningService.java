package com.zode64.youreunplugged;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class WarningService extends Service {

	public static final String STOP_WARNING_ACTION = "com.zode64.youreunplugged.intent.action.STOP_WARNING";
	
	private static final int VIBRATION_TIME = 1000;
	private static final int HEADSET_PLUGGED_IN = 1;
	private static final String TAG = "WarningService";

	private WarningPlayer wp;
	private BroadcastReceiver stopWarningReceiver;
	private BroadcastReceiver jackPluggedInReceiver;
	private BroadcastReceiver phoneUnlockedReceiver;

	@Override
	public void onCreate() {
		super.onCreate();
		final IntentFilter stopWarningFilter = new IntentFilter();
		stopWarningFilter.addAction(STOP_WARNING_ACTION);
		stopWarningReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (wp != null && wp.isPlaying()) {
					stopWarning();
				}
			}
		};
		registerReceiver(stopWarningReceiver, stopWarningFilter);

		final IntentFilter jackPluggedInFilter = new IntentFilter();
		jackPluggedInFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		jackPluggedInReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getIntExtra("state", -1) == HEADSET_PLUGGED_IN) {
					if (wp != null && wp.isPlaying()) {
						stopWarning();
					}
				}
			}
		};
		registerReceiver(jackPluggedInReceiver, jackPluggedInFilter);

		final IntentFilter phoneUnpluggedFilter = new IntentFilter();
		phoneUnpluggedFilter.addAction(Intent.ACTION_USER_PRESENT);
		phoneUnlockedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (wp != null && wp.isPlaying()) {
					stopWarning();
				}
			}
		};
		registerReceiver(phoneUnlockedReceiver, phoneUnpluggedFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "Warning service started");
		wp = new WarningPlayer(this, new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopWarning();
			}
		});
		wp.playWithDelay();
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(VIBRATION_TIME);
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private void stopWarning() {
		if (wp != null) {
			wp.stop();
		}
		stopSelf();
		unregisterReceiver(jackPluggedInReceiver);
		unregisterReceiver(stopWarningReceiver);
		unregisterReceiver(phoneUnlockedReceiver);
		stopWarningReceiver = null;
		jackPluggedInReceiver = null;
		phoneUnlockedReceiver = null;
	}

}