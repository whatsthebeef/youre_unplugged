package com.zode64.youreunplugged;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.zode64.saftie.R;

public class WarningPlayer {

	private static final int WARNING_DELAY = 1000;
	private static final String TAG = "WarningPlayer";

	private MediaPlayer mediaPlayer;
	private Context context;
	private OnCompletionListener listener;

	public WarningPlayer(Context context) {
		this.context = context;
	}

	public WarningPlayer(Context context, OnCompletionListener listener) {
		this.listener = listener;
		this.context = context;
	}

	public void setOnCompletionListener(OnCompletionListener listener) {
		this.listener = listener;
	}

	public void stop() {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	public boolean isPlaying() {
		return mediaPlayer != null ? mediaPlayer.isPlaying() : false;
	}

	public void playWithDelay() {
		play(WARNING_DELAY);
	}

	public void play(int delay) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		stop();
		mediaPlayer = MediaPlayer.create(context, R.raw.cow);
		String warningPath = prefs.getString("warning", null);
		if (warningPath != null) {
			Uri warning = Uri.fromFile(new File(warningPath));
			if (warning != null) {
				try {
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setDataSource(context, Uri.fromFile(new File(warningPath)));
					Log.i(TAG, "Uri data source : " + warning.toString());
					mediaPlayer.prepare();
				} catch (Exception e) {
					e.printStackTrace();
					prefs.edit().remove("warning").commit();
					Toast.makeText(context,
							context.getResources().getString(R.string.problem_warning_file),
							Toast.LENGTH_SHORT).show();
					mediaPlayer = MediaPlayer.create(context, R.raw.cow);
				}
			}
		}

		if (listener != null) {
			mediaPlayer.setOnCompletionListener(listener);
		} else {
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mediaPlayer.release();
					mediaPlayer = null;
				}
			});
		}

		// Start increasing volume in increments
		final Timer timer = new Timer(true);
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				mediaPlayer.start();
			}
		};
		timer.schedule(timerTask, delay);
	}
}
