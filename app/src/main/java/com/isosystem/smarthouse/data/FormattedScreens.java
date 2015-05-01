package com.isosystem.smarthouse.data;

import android.content.Context;

import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.logging.Logging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Класс "Окна форматированного вывода"
 */
public class FormattedScreens implements Serializable {

	// Список окон
	public ArrayList<FormattedScreen> mFormattedScreens;

	public FormattedScreens() {
		mFormattedScreens = new ArrayList<FormattedScreen>();
	}

	/**
	 * Добавление нового окна форматированного вывода
	 */
	public void addFormattedScreen(Context c, String name, String start, String end, HashMap<String, String> pmap) {
		mFormattedScreens.add(new FormattedScreen(name, start, end, pmap));
		this.saveScreensToFile(c);
	}
	
	public ArrayList<String> getFormattedScreens() {
		ArrayList<String> fsNames = new ArrayList<String>();
		for (int i=0;i<mFormattedScreens.size();i++) {
			fsNames.add(mFormattedScreens.get(i).mName);
		}
		return fsNames;
	}

	/**
	 * Сохранение окон в файл
	 * @param c Контекст
	 */
	public void saveScreensToFile(Context c) {
		MyApplication myApp = (MyApplication) c.getApplicationContext();

		try {
			myApp.mProcessor.saveFormattedScreensToInternalStorage();
		} catch (Exception e) {
			Logging.v("Исключение при попытке записать файл с сообщениями");
			e.printStackTrace();
		}
	}

	public void swapScreens(Context c, int index1, int index2) {
		Collections.swap(this.mFormattedScreens, index1, index2);
		this.saveScreensToFile(c);
	}
	
	/**
	 * Удаление окна по индексу
	 */
	public void deleteScreenByIndex(Context c, int index) {
		this.mFormattedScreens.remove(index);
		this.saveScreensToFile(c);
	}

	public void changeFormattedScreen (Context c, int position, String name, String start, String end, HashMap<String, String> pmap) {
		mFormattedScreens.get(position).mName = name;
		mFormattedScreens.get(position).mInputStart = start;
		mFormattedScreens.get(position).mInputEnd = end;
		mFormattedScreens.get(position).paramsMap = pmap;
		
		this.saveScreensToFile(c);
	}
	
	/**
	 * Стирание всех окон
	 */
	public void deleteAllScreens(Context c) {
		this.mFormattedScreens.clear();
		this.saveScreensToFile(c);
	}
}