/*
 * Мобильное приложение для проекта "Умный дом"
 * 
 * author: Годовиченко Николай
 * email: nick.godov@gmail.com
 * last edit: 11.09.2014
 */

package com.isosystem.smarthouse;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.isosystem.smarthouse.data.AlarmMessages;
import com.isosystem.smarthouse.data.FormattedScreens;
import com.isosystem.smarthouse.data.MenuTree;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.SoundMessages;
import com.isosystem.smarthouse.processor.XMLProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Этот класс содержит объект дерева меню {@link MenuTree}, а также
 * объект-обработчик дерева меню {@link XMLProcessor}, который позволяет
 * считывать и записывать деревю меню в файл на планшете.
 * 
 * @author Годовиченко Николай (nick.godov@gmail.com)
 * @see MenuTree
 * @see XMLProcessor
 */
public class MyApplication extends Application {

	/** Дерево меню */
	public MenuTree mTree;

	/** Список алармовых сообщений */
	public AlarmMessages mAlarmMessages;

	public Boolean isUsbConnected = false;

	/** Список окон форматированного вывода */
	public FormattedScreens mFormattedScreens;

	/** Обработчик дерева меню (загрузка\сохранение) */
	public XMLProcessor mProcessor;

	public SoundMessages soundMessages;

	/**
	 * При старте приложения:<br>
	 * 1) Проверяем наличие внешних папок; <br>
	 * 2) Получаем объект-обработчик; <br>
	 * 3) Загружаем дерево меню с помощью объекта-обработчика.
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		PreferenceManager.setDefaultValues(this, R.xml.application_preferences,
				false);

		soundMessages = new SoundMessages(getApplicationContext());
		mProcessor = new XMLProcessor(getApplicationContext());

		if (!mProcessor.loadMessagesFromInternalStorage()) {
			Logging.v("Загрузка сообщений из внутреннего хранилища прошла с ошибкой");
		}
	}
}