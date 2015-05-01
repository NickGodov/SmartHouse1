package com.isosystem.smarthouse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Этот ресивер слушает action android.intent.action.BOOT_COMPLETED
 * и запускает {@link MainActivity} при получении
 * широковещательного сообщения
 *
 * @see com.isosystem.smarthouse.MainActivity
 */
public class BootCompletedEventReceiver extends BroadcastReceiver {
    /**
     * При получении широковещательного сообщения, запуск MainActivity
     *
     * @param context контекст
     * @param intent полученный интент
     */
    @Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
}