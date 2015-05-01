/*
 * ��������� ���������� ��� ������� "����� ���"
 * 
 * author: ����������� �������
 * email: nick.godov@gmail.com
 * last edit: 11.09.2014
 */

package com.isosystem.smarthouse;

/**
 * ������ ����� �������� ���������� ��������� ��� ����������
 */
public final class Globals {

	/** ����� �� sd-card � ������� ��� ���������� */
	public static final String EXTERNAL_ROOT_DIRECTORY = "smarthouse";
	/** ����� � EXTERNAL_ROOT_DIRECTORY ��� �������� ����������� */
	public static final String EXTERNAL_IMAGES_DIRECTORY = "images";
    /** ����� � assets, ������ ������� ����������� �������� */
	public static final String ASSETS_IMAGES_DIRECTORY = "imgs";
    /** ����� � EXTERNAL_ROOT_DIRECTORY ��� �������� ���� */
	public static final String EXTERNAL_LOGS_DIRECTORY = "logs";
    /** ����� � EXTERNAL_ROOT_DIRECTORY ��� �������� �������� ��� �����-��� */
	public static final String EXTERNAL_SCREENSAVER_IMAGES_DIRECTORY = "screensaver";

    /** ��� ����� ����, ������� �������� �� ���������� ��������� */
	public static final String INTERNAL_MENU_FILE = "menu.obj";
    /** ��� ����� ���� ������, ������� �������� �� ���������� ��������� */
	public static final String INTERNAL_FORMATTED_SCREENS_FILE = "fs.obj";
    /** ��� ����� � �����������, ������� �������� �� ���������� ��������� */
    public static final String INTERNAL_MESSAGES_FILE = "messages.obj";

    /** ������� ��� �����, ������� �������� � ����� ����� */
	public static final String LOG_TAG = "SMARTHOUSE";

    /** Action ��� Broadcast Receiver ��� ������� ���������� ��������� */
	public static final String BROADCAST_INTENT_ALARM_MESSAGE = "SMARTHOUSE.ALARM_MESSAGE_RECEIVED";
    /** Action ��� Broadcast Receiver ��� ������� ��������� �� ��������� */
	public static final String BROADCAST_INTENT_VALUE_MESSAGE = "SMARTHOUSE.VALUE_MESSAGE_RECEIVED";
    /** Action ��� Broadcast Receiver ��� ������� ��������� ��� ���� ���������������� ������ */
	public static final String BROADCAST_INTENT_FORMSCREEN_MESSAGE = "SMARTHOUSE.FORMSCREEN_MESSAGE_RECEIVED";

    /** ��������� ������ ��� �������� */
    public static final String SERVICE_PASSWORD = "924";

    /** ����� ���������� �������������� */
    public static final Boolean DEBUG_MODE = false;

    /** ���� ��� ������������ XML �����, � ������� �������� ������ */
	public static final String PREFERENCES_PASSWORD_STRING = "PREFERENCES_PASSWORD";
}