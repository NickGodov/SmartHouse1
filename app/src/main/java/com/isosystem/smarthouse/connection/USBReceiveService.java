package com.isosystem.smarthouse.connection;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications.MessageType;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для приема сообщений из контроллера Запускается в момент
 */
public class USBReceiveService extends IntentService {
	MyApplication mApplication;

	UsbManager usbManager;
	HashMap<String, UsbDevice> deviceList;
	UsbDevice usbDevice;
	UsbInterface usbInterface;
	UsbEndpoint usbEndPointIn;
	UsbDeviceConnection usbConnection;

	// Буфер сообщений
	StringBuilder mMessageBuffer;
	// Обработчик буфера
	static Handler mMessageHandler;

	// Периодическая очистка буфера
	static Handler mBufferCleanHandler;
	// Время после прихода последнего сообщения
	// которое должно пройти для очистки буфера
	int mBufferClearTimeout = 2000;

	public USBReceiveService() {
		super("USBReceive");
		// Logging.v("Инициализация ReceiveService");
	}

	public void onCreate() {
		super.onCreate();
		mApplication = (MyApplication) getApplicationContext();

		mMessageBuffer = new StringBuilder();

		// Считывание тайм-аута для очистки буфера
		// из настроечного файла
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);

		try {
			mBufferClearTimeout = Integer.parseInt(prefs.getString(
					"buffer_clean_timeout_value", "2000"));
		} catch (Exception e) {
			// Logging.v("Ошибка при попытке считать значение периода обновления буфера из preferences");
			e.printStackTrace();
		}

		mBufferCleanHandler = new Handler();
		mBufferCleanHandler.postDelayed(mBufferClearRunnable,
				mBufferClearTimeout);

		mMessageHandler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				Bundle bundle = msg.getData();

				// Сообщение
				String message = bundle.getString("incoming_message");
				Logging.v("Пришло сообщение: [" + message + "]");

				// Добавляем сообщение в буфер
				mMessageBuffer.append(message);
				Logging.v("Значение буфера сообщений: ["
						+ mMessageBuffer.toString() + "]");

				// Поиск подстроки, которая начинается с @ или & или $
				// и заканчивается ¶
				Pattern p = Pattern.compile("[@&$](.*?)¶");
				Matcher m = p.matcher(mMessageBuffer);
				while (m.find()) {
					messageProcess(m.group());
					Logging.v("Найденная команда: [" + m.group() + "]");
				}

				// После отправки сообщения на обработку, оно удаляется из
				// буфера
				mMessageBuffer = new StringBuilder(m.replaceAll(""));
				Logging.v("Значение буфера после поиска команд: ["
						+ mMessageBuffer.toString() + "]");
			} // handle message
		}; // handler

		Logging.v("Создание ReceiveService");

		/** 1. Получаем список устройств */
		try {
			usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			deviceList = usbManager.getDeviceList();
		} catch (Exception e) {
			// Logging.v("Исключение при попытке получить список USB устройств");
			e.printStackTrace();
			this.stopSelf();
			return;
		}

		// Устройств нету
		if (deviceList.isEmpty()) {
			// Logging.v("Сервис остановлен, т.к. не обнаружены USB-устройства");
			this.stopSelf();
			return;
		}

		// Logging.v("Найдено " + String.valueOf(deviceList.size()) +
		// " устройств");

		/** 2. Ищем среди найденных устройств нужное устройство */
		for (UsbDevice device : deviceList.values()) {

			// Logging.v("Product ID " + String.valueOf(device.getProductId())
			// + " Vendoid ID " + String.valueOf(device.getVendorId())
			// + " Device ID " + String.valueOf(device.getDeviceId())
			// + " Class ID " + String.valueOf(device.getDeviceClass()));

			if ((device.getProductId() == 257)
					&& (device.getVendorId() == 65535)) {

				// Logging.v("Устройство обнаружено");

				try {
					usbDevice = device;
				} catch (Exception e) {
					// Logging.v("Исключение при попытке взять интерфейс USB-устройства");
					e.printStackTrace();
					this.stopSelf();
					return;
				}
			}// if
		}

		// Нужных устройств нет
		if (usbDevice == null) {
			// Logging.v("Подходящих устройств не обнаружено");
			this.stopSelf();
			return;
		}

		// Logging.v("Найдено " + String.valueOf(usbDevice.getInterfaceCount())
		// + " интерфейсов");

		/** 3. Берем интерфейс устройства */
		for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
			UsbInterface tempInterfce = usbDevice.getInterface(i);
			/*
			 * Logging.v("Endpoint count " +
			 * String.valueOf(tempInterfce.getEndpointCount()) +
			 * " Interface ID " + String.valueOf(tempInterfce.getId()) +
			 * " Protocol ID " +
			 * String.valueOf(tempInterfce.getInterfaceProtocol()) +
			 * " Class ID " + String.valueOf(tempInterfce.getInterfaceClass()));
			 */
			if (tempInterfce.getEndpointCount() > 1) {
				usbInterface = tempInterfce;
			}
		}

		// Нужных интерфейсов нет
		if (usbInterface == null) {
			// Logging.v("Подходящих интерфейсов не обнаружено");
			this.stopSelf();
			return;
		}

		// Logging.v("Найдено " +
		// String.valueOf(usbInterface.getEndpointCount())
		// + " конечных точек");

		/** 4. Берем EndPoint */
		for (int i = 0; i < usbInterface.getEndpointCount(); i++) {

			UsbEndpoint tempPoint = usbInterface.getEndpoint(i);
			/*
			 * Logging.v("Endpoint direction " +
			 * String.valueOf(tempPoint.getDirection()) + " Endpoint type" +
			 * String.valueOf(tempPoint.getType()) + " Protocol address " +
			 * String.valueOf(tempPoint.getAddress()));
			 */
			if (tempPoint.getDirection() == UsbConstants.USB_DIR_IN) {
				usbEndPointIn = tempPoint;
			}
		}// for

		// Нужных конечных точек нет
		if (usbEndPointIn == null) {
			// Logging.v("Подходящих конечных точек не обнаружено");
			this.stopSelf();
			return;
		}

		// Открываем соединение для USB-устройства
		try {
			usbConnection = usbManager.openDevice(usbDevice);
		} catch (Exception e) {
			// Logging.v("Исключение при попытке открыть USB-устройство");
			e.printStackTrace();
			this.stopSelf();
			return;
		}

		// Добавляем сообщение в список сообщений
		String msg = "USB-устройство присоединено";

//		mApplication = (MyApplication) getApplicationContext();
//
//		if (!mApplication.equals(null)) {
//
//			if (mApplication.mAlarmMessages != null) {
//				try {
//					mApplication.mAlarmMessages.addAlarmMessage(mApplication,
//							msg, MessageType.USBConnectionMessage);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}

		mApplication.mAlarmMessages.addAlarmMessage(mApplication,
				msg, MessageType.USBConnectionMessage);
		
		// Отсылаем броадкаст о том, что пришло алармовое сообщение
		Intent i = new Intent();
		i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
		getApplicationContext().sendBroadcast(i);

		mApplication.isUsbConnected = true;
	}

	// Runnable для очистки буфера после N мсек.
	private Runnable mBufferClearRunnable = new Runnable() {
		public void run() {
			// Очистка буфера
			mMessageBuffer = new StringBuilder();

			Logging.v("Очистка буфера!");

			mBufferCleanHandler.removeCallbacks(mBufferClearRunnable);
			mBufferCleanHandler.postDelayed(mBufferClearRunnable,
					mBufferClearTimeout);
		}
	};

	private Boolean checkUsbDevice() {
		UsbManager manager;
		HashMap<String, UsbDevice> dList;

		manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		dList = manager.getDeviceList();

		if (dList.isEmpty())
			return false;

		for (UsbDevice device : dList.values()) {
			if ((device.getProductId() == 257)
					&& (device.getVendorId() == 65535)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		while (checkUsbDevice()) {
			// Буфер для приема сообщений
			byte[] mReadBuffer = new byte[128];
			// Количество принятых байт
			int transferred = -1;

			// Берем интерфейс
			try {
				usbConnection.claimInterface(usbInterface, true);
			} catch (Exception e) {
				// Logging.v("Исключение при попытке взять интерфейс");
				e.printStackTrace();
				continue;
			}

			// Забираем сообщение из endpoint
			try {
				transferred = usbConnection.bulkTransfer(usbEndPointIn,
						mReadBuffer, mReadBuffer.length, 0);
			} catch (Exception e) {
				// Logging.v("Исключение при попытке считывания пришедшего сообщения");
				e.printStackTrace();
				continue;
			}

			// Освобождаем интерфейс
			try {
				usbConnection.releaseInterface(usbInterface);
			} catch (Exception e) {
				// Logging.v("Исключение при попытке освободить интерфейс");
				e.printStackTrace();
				continue;
			}

			/** Обрабатываем пришедшие данные */
			if (transferred >= 0) {

				// Если пришло сообщение - сброс тайм-аута очистки буфера
				mBufferCleanHandler.removeCallbacks(mBufferClearRunnable);
				mBufferCleanHandler.postDelayed(mBufferClearRunnable,
						mBufferClearTimeout);

				String mReceivedMessage = new String(mReadBuffer, 0,
						transferred, Charset.forName("windows-1251"));

				// Logging.v("Обрезано:[" + mReceivedMessage + "]");

				Bundle b = new Bundle();
				b.putString("incoming_message", mReceivedMessage);

				Message msg = new Message();
				msg.setData(b);

				mMessageHandler.sendMessage(msg);

			} // end if transferred
		} // end while
	} // end onHandleIntent

	/**
	 * Обработка пришедшего сообщения: 1) Считываем первый символ, чтобы понять
	 * тип сообщения
	 * 
	 * @param message
	 *            Пришедшее сообщение
	 */
	private void messageProcess(String message) {
		Intent i = new Intent();

		// Стирание последнего символа = ¶
		message = message.substring(0, message.length() - 1);

		if (message.charAt(0) == '$') {
			// Пришло алармовое сообщение
			if (message.length() > 2) {
				// Убираем $ и пробел
				String alarmMessage = message.substring(2);

				// Logging.v("Получено алармовое сообщение: " + alarmMessage);

				mApplication.mAlarmMessages.addAlarmMessage(
						getApplicationContext(), alarmMessage,
						MessageType.ControllerMessage);

				// Кидаем броадкаст
				i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
				getApplicationContext().sendBroadcast(i);
			} else {
				// Пришло сообщение длиной 1 или 2 символа, где первый был $
				// Logging.v("Пришло некорректное алармовое сообщение: " +
				// message);
			} // end if length
		} else if (message.charAt(0) == '&') {
			// Пришло интовое значение
			i.setAction(Globals.BROADCAST_INTENT_VALUE_MESSAGE);
			i.putExtra("message", message);
			// Кидаем броадкаст
			getApplicationContext().sendBroadcast(i);
		} else if (message.charAt(0) == '@') {
			// Пришло сообщение форматированного вывода
			i.setAction(Globals.BROADCAST_INTENT_FORMSCREEN_MESSAGE);
			i.putExtra("message", message);
			// Кидаем броадкаст
			getApplicationContext().sendBroadcast(i);
		} else {
			// Logging.v("Пришло сообщение неизвестного формата: " + message);
		} // end char[0]
	} // end method

	public void onDestroy() {
		try {
			// Logging.v("Уничтожение ReceiveService");

			try {
				usbConnection.releaseInterface(usbInterface);
			} catch (Exception e) {
				// Logging.v("Исключение при попытке освободить интерфейс");
				e.printStackTrace();
			}

			if (mBufferCleanHandler != null) {
				mBufferCleanHandler.removeCallbacks(mBufferClearRunnable);
			}

			// Алармовое сообщение + броадкаст
			String msg = "USB-устройство отсоединено";
			mApplication.mAlarmMessages.addAlarmMessage(
					getApplicationContext(), msg,
					MessageType.USBConnectionMessage);

			Intent i = new Intent();
			i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
			getApplicationContext().sendBroadcast(i);

			mApplication.isUsbConnected = false;

			super.onDestroy();
		} catch (Exception e) {
			// Logging.v("Исключение при попытке уничтожить ReceiveService");
			e.printStackTrace();
			super.onDestroy();
		}
	}
}