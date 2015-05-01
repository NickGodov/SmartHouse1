package com.isosystem.smarthouse.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;
import com.isosystem.smarthouse.notifications.Notifications.MessageType;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Класс "Алармовые сообщения"
 */
public class AlarmMessages implements Serializable {
	// Список сообщений
	public ArrayList<AlarmMessage> mAlarmMessages;

	public AlarmMessages() {
		mAlarmMessages = new ArrayList<AlarmMessage>();
	}

	/**
	 * Добавление нового сообщения: 1) Добавление сообщения в список 2) Toast с
	 * сообщением 3) Сохранение файла с сообщениями
	 */

	public void addAlarmMessage(Context c, String msg, MessageType type) {
		// Проигрывание звукового сигнала при получении алармового сообщения
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(c);
		// Проверяем - нужно ли использовать буфер
		if (prefs.getBoolean("enable_alarm_messages_sound_alert", false)) {
			MyApplication mApplication = (MyApplication) c
					.getApplicationContext();
			mApplication.soundMessages.playAlarmSound();
		}

		// 1. Добавление в список
		AlarmMessage alarmMessage = new AlarmMessage(msg, type);
		this.mAlarmMessages.add(0, alarmMessage);

		if (type == MessageType.USBConnectionMessage) {
			Notifications.showUsbMessage(c, msg);
		} else if (type == MessageType.ControllerMessage) {
			Notifications.showControllerAlarmMessage(c, msg);
		} else if (type == MessageType.PowerSupplyMessage) {
			Notifications.showPowerSupplyMessage(c, msg);
		}

		// 3. Обновление файла с собщениями
		saveMessagesToFile(c);
	}

	/**
	 * Сохранение сообщений в файл
	 * 
	 * @param c
	 *            Контекст
	 */
	public void saveMessagesToFile(Context c) {
		MyApplication myApp = (MyApplication) c.getApplicationContext();

		// // Исправлено 04.02
		// try {
		// myApp.mProcessor.serializeAlarmMessages(myApp.mAlarmMessages);
		// } catch (Exception e) {
		// Logging.v("Исключение при попытке записать файл с сообщениями");
		// e.printStackTrace();
		// }

		try {
			myApp.mProcessor.saveMessagesToInternalStorage();
		} catch (Exception e) {
			Logging.v("Исключение при попытке записать файл с сообщениями");
			e.printStackTrace();
		}

	}

	/**
	 * Стирание сообщеня: 1) Стирание сообщений 2) Сохранение файла с
	 * сообщениями
	 * 
	 * @param index
	 *            Индекс сообщения в списке
	 * @param c
	 *            Контекст
	 */
	public void clearMessage(int index, Context c) {
		// 1. Стирание сообщения
		this.mAlarmMessages.remove(index);
		// 2. Запись на диск
		this.saveMessagesToFile(c);
	}

	/**
	 * Стирание всех сообщений: 1) Стирание сообщений 2) Сохранение файла с
	 * сообщениями
	 * 
	 * @param c
	 *            Контекст
	 */
	public void clearAllMessages(Context c) {
		// 1. Стирание всех сообщений
		this.mAlarmMessages = new ArrayList<AlarmMessage>();
		// 2. Запись на диск
		this.saveMessagesToFile(c);
	}

}