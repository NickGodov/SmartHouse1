package com.isosystem.smarthouse.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.notifications.Notifications.MessageType;

public class PowerConnectionReceiver extends BroadcastReceiver {

	MyApplication mApplication;

	@Override
	public void onReceive(Context context, Intent intent) {

		mApplication = (MyApplication) context.getApplicationContext();

		String msg;
		if (isSupplyEnabled()) {
			msg = "Устройство подключено к электросети";
		} else {
			msg = "Устройство отключено от электросети";
		}

		mApplication.mAlarmMessages.addAlarmMessage(mApplication, msg,
				MessageType.PowerSupplyMessage);

		Intent i = new Intent();
		i.setAction("SMARTHOUSE.POWER_SUPPLY_CHANGED");
		context.sendBroadcast(i);
	}

	private Boolean isSupplyEnabled() {
		Intent intent = mApplication.registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
        int plugged = 0;
		plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean result = (plugged != 0 && plugged!=-1);
		return result;
	}
}