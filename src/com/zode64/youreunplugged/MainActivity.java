package com.zode64.youreunplugged;

import java.io.File;

import com.zode64.saftie.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final int PICK_WARNING_SOUND = 0;

	private WarningPlayer wp;

	private Button selectWarning;
	private Button stopWarning;
	private Button warningVolume;

	private TextView songTitle;

	private TextView onText;
	private TextView offText;

	private SlideSwitch onOffButton;

	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		wp = new WarningPlayer(this);

		songTitle = (TextView) findViewById(R.id.warning_song_title);

		selectWarning = (Button) findViewById(R.id.select_warning);
		stopWarning = (Button) findViewById(R.id.stop_warning);
		warningVolume = (Button) findViewById(R.id.warning_volume);

		onText = (TextView) findViewById(R.id.on_text);
		offText = (TextView) findViewById(R.id.off_text);
		onOffButton = (SlideSwitch) findViewById(R.id.on_off_switch);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		wp = new WarningPlayer(this);

		boolean isOn = prefs.getBoolean("is_on", true);
		onOffButton.setChecked(isOn);
		if (!isOn) {
			onText.setTextColor(Color.BLACK);
		} else {
			offText.setTextColor(Color.BLACK);
		}

		String song = prefs.getString("warning", null);
		if (song != null) {
			File songFile = new File(song);
			if (songFile.exists()) {
				songTitle.setText(songFile.getName());
			} else {
				prefs.edit().remove("warning").commit();
			}
		}
		addListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.item_restore_default) {
			prefs.edit().remove("warning").commit();
			songTitle.setText("");
		} else {
			startActivity(new Intent(this, AboutUsActivity.class));
		}
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_WARNING_SOUND) {
			if (resultCode == RESULT_OK && data != null) {
				File file = new File(getPath(data.getData()));
				if (file.exists()) {
					prefs.edit().putString("warning", getPath(data.getData())).commit();
					songTitle.setText(file.getName());
				}
			}
		}
	}

	private void addListeners() {
		onOffButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.edit().putBoolean("is_on", isChecked).commit();
				if (!isChecked) {
					onText.setTextColor(Color.BLACK);
					offText.setTextColor(Color.GRAY);
					wp.stop();
				} else {
					onText.setTextColor(Color.GRAY);
					offText.setTextColor(Color.BLACK);
					wp.stop();
				}
			}
		});
		onText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOffButton.setChecked(true);
			}
		});
		offText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOffButton.setChecked(false);
			}
		});
		selectWarning.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK,
						android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(Intent.createChooser(intent, "Gallery"), PICK_WARNING_SOUND);
			}
		});
		stopWarning.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setAction(WarningService.STOP_WARNING_ACTION);
				sendBroadcast(i);
				if (wp != null) {
					wp.stop();
				}
			}
		});
		warningVolume.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME,
						AudioManager.FLAG_SHOW_UI);
			}
		});
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		startManagingCursor(cursor);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
}
