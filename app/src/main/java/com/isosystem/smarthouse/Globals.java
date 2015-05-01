/*
 * Мобильное приложение для проекта "Умный дом"
 * 
 * author: Годовиченко Николай
 * email: nick.godov@gmail.com
 * last edit: 11.09.2014
 */

package com.isosystem.smarthouse;

/**
 * Данный класс содержит глобальные константы для приложения
 */
public final class Globals {

	/** Папка на sd-card с файлами для приложения */
	public static final String EXTERNAL_ROOT_DIRECTORY = "smarthouse";
	/** Папка в EXTERNAL_ROOT_DIRECTORY где хранятся изображения */
	public static final String EXTERNAL_IMAGES_DIRECTORY = "images";
    /** Папка в assets, откуда берутся стандартные картинки */
	public static final String ASSETS_IMAGES_DIRECTORY = "imgs";
    /** Папка в EXTERNAL_ROOT_DIRECTORY где хранятся логи */
	public static final String EXTERNAL_LOGS_DIRECTORY = "logs";
    /** Папка в EXTERNAL_ROOT_DIRECTORY где хранятся картинки для слайд-шоу */
	public static final String EXTERNAL_SCREENSAVER_IMAGES_DIRECTORY = "screensaver";

    /** Имя файла меню, который хранится во внутреннем хранилище */
	public static final String INTERNAL_MENU_FILE = "menu.obj";
    /** Имя файла окон вывода, который хранится во внутреннем хранилище */
	public static final String INTERNAL_FORMATTED_SCREENS_FILE = "fs.obj";
    /** Имя файла с сообщениями, который хранится во внутреннем хранилище */
    public static final String INTERNAL_MESSAGES_FILE = "messages.obj";

    /** Префикс для логов, которые хранятся в папке логов */
	public static final String LOG_TAG = "SMARTHOUSE";

    /** Action для Broadcast Receiver для прихода алармового сообщения */
	public static final String BROADCAST_INTENT_ALARM_MESSAGE = "SMARTHOUSE.ALARM_MESSAGE_RECEIVED";
    /** Action для Broadcast Receiver для прихода сообщения со значением */
	public static final String BROADCAST_INTENT_VALUE_MESSAGE = "SMARTHOUSE.VALUE_MESSAGE_RECEIVED";
    /** Action для Broadcast Receiver для прихода сообщения для окон форматированного вывода */
	public static final String BROADCAST_INTENT_FORMSCREEN_MESSAGE = "SMARTHOUSE.FORMSCREEN_MESSAGE_RECEIVED";

    /** Сервисный пароль для настроек */
    public static final String SERVICE_PASSWORD = "924";

    /** Режим подробного журналирования */
    public static final Boolean DEBUG_MODE = false;

    /** Ключ для настроечного XML файла, в котором хранится пароль */
	public static final String PREFERENCES_PASSWORD_STRING = "PREFERENCES_PASSWORD";
}