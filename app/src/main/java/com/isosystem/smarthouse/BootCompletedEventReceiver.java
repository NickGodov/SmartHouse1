package com.isosystem.smarthouse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * ���� ������� ������� action android.intent.action.BOOT_COMPLETED
 * � ��������� {@link MainActivity} ��� ���������
 * ������������������ ���������
 *
 * @see com.isosystem.smarthouse.MainActivity
 */
public class BootCompletedEventReceiver extends BroadcastReceiver {
    /**
     * ��� ��������� ������������������ ���������, ������ MainActivity
     *
     * @param context ��������
     * @param intent ���������� ������
     */
    @Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent(context, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
}