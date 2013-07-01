package com.zode64.youreunplugged;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class JackEventReceiver extends BroadcastReceiver {

	private static final String TAG = JackEventReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "Head phone event received");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(!prefs.getBoolean("is_on", true)) {
			context.startService(new Intent(context, WarningService.class));
		}
	}

}
