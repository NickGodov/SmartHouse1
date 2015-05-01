package com.isosystem.smarthouse.notifications;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import com.isosystem.smarthouse.R;

public class SoundMessages {

	Handler handler;
	MediaPlayer player;
	
	public SoundMessages (Context c) {
		player = MediaPlayer.create(c.getApplicationContext(), R.raw.ding);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				player.start();
			}
		};
	}
	
	public void playAlarmSound() {
		handler.sendEmptyMessage(0);
	}
}