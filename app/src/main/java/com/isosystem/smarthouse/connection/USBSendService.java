package com.isosystem.smarthouse.connection;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.util.HashMap;

public class USBSendService extends IntentService {

	UsbManager usbManager;
	HashMap<String, UsbDevice> deviceList;
	UsbDevice usbDevice;
	UsbInterface usbInterface;
	UsbEndpoint usbEndpointOut;
	UsbDeviceConnection usbConnection;

	Boolean wasSend;

	public USBSendService() {
		super("USBSEND");
		Logging.v("Инициализация сервиса");
	}

	public void onCreate() {
		super.onCreate();

		wasSend = false;
		
		Logging.v("Создаем сервис");

		/**1. Получаем список устройств */
		
		try {
			usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			deviceList = usbManager.getDeviceList();
		} catch (Exception e) {
			Logging.v("Исключение при попытке получить список USB устройств");
			e.printStackTrace();
			this.stopSelf();
			return;
		}

		// Устройств нету
		if (deviceList.isEmpty()) {
			Logging.v("Сервис остановлен, т.к. не обнаружены USB-устройства");
			this.stopSelf();
			return;
		}
		
		Logging.v("Найдено " + String.valueOf(deviceList.size()) + " устройств");
		
		/**2. Ищем среди найденных устройств нужное устройство */
		
		// Ищем в списке USB-устройств наше устройство
        for (UsbDevice device : deviceList.values()) {
        	
        	Logging.v("Product ID " + String.valueOf(device.getProductId()) +
        			 		 " Vendor ID " + String.valueOf(device.getVendorId()) +
        					 " Device ID " + String.valueOf(device.getDeviceId()) + 
        					 " Class ID " + String.valueOf(device.getDeviceClass()));
        	
        	if ((device.getProductId() == 257) && (device.getVendorId() == 65535)) {
        		
        		Logging.v("Устройство обнаружено");
        		
        		try {
        			usbDevice = device;
        		} catch (Exception e) {
        			Logging.v("Исключение при попытке взять интерфейс USB-устройства");
        			e.printStackTrace();
        			this.stopSelf();
        			return;
        		}
        	}//if
        }
        
		// Нужных устройств нет
        if (usbDevice == null) {
			Logging.v("Подходящих устройств не обнаружено");
			this.stopSelf();
			return;
        }
		
		Logging.v("Найдено " + String.valueOf(usbDevice.getInterfaceCount()) + " интерфейсов");
        
		/**3. Берем интерфейс устройства */
		
        for (int i = 0; i < usbDevice.getInterfaceCount();i++) {
        	UsbInterface tempInterfce = usbDevice.getInterface(i);
        	
        	Logging.v("Endpoint count " + String.valueOf(tempInterfce.getEndpointCount()) +
			 		 " Interface ID " + String.valueOf(tempInterfce.getId()) +
					 " Protocol ID " + String.valueOf(tempInterfce.getInterfaceProtocol()) + 
					 " Class ID " + String.valueOf(tempInterfce.getInterfaceClass()));
  
        	if (tempInterfce.getEndpointCount() > 1) {
        		usbInterface = tempInterfce;
        	}
        }
        
		// Нужных интерфейсов нет
        if (usbInterface == null) {
			Logging.v("Подходящих интерфейсов не обнаружено");
			this.stopSelf();
			return;
        }

		Logging.v("Найдено " + String.valueOf(usbInterface.getEndpointCount()) + " конечных точек");
        
		/**4. Берем EndPoint */

		for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
			
			UsbEndpoint tempPoint = usbInterface.getEndpoint(i);
			
        	Logging.v("Endpoint direction " + String.valueOf(tempPoint.getDirection()) +
			 		 " Endpoint type" + String.valueOf(tempPoint.getType()) +
					 " Protocol address " + String.valueOf(tempPoint.getAddress()));
			
			if (tempPoint.getDirection() == UsbConstants.USB_DIR_OUT) {
				usbEndpointOut = tempPoint;
			}
		}//for

		
		// Нужных конечных точек нет
        if (usbEndpointOut == null) {
			Logging.v("Подходящих конечных точек не обнаружено");
			this.stopSelf();
			return;
        }
		
		// Открываем соединение для USB-устройства
		try {
			usbConnection = usbManager.openDevice(usbDevice);
		} catch (Exception e) {
			Logging.v("Исключение при попытке открыть USB-устройство");
			e.printStackTrace();
			this.stopSelf();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		/** 1. Считываем передаваемое сообщение из extras */
		String msg;
		try {
			msg = intent.getStringExtra("message");
		} catch (Exception e) {
			Logging.v("Исключение при попытке получить из extras сообщение");
			e.printStackTrace();
			this.stopSelf();
			return;
		}

		/** 2. Проверяем наличие устройства и подключения к нему */
		if ((usbConnection !=null) && (usbDevice!=null)) {

			// Берем интерфейс
			try {
				usbConnection.claimInterface(usbInterface, true);
			} catch (Exception e) {
				Logging.v("Исключение при попытке взять интерфейс");
				e.printStackTrace();
				this.stopSelf();
				return;
			}

			/** 
			 * ПЕРЕДАЕМ СООБЩЕНИЕ 
			 */
			
			// Передаем сообщение и получаем количество байт для сообщения
			int result = -1;
			try {
				result = usbConnection.bulkTransfer(usbEndpointOut,
						msg.getBytes(), msg.getBytes().length, 0);
			} catch (Exception e) {
				Logging.v("Исключение при попытке выслать сообщение");
				e.printStackTrace();
				this.stopSelf();
				return;
			}

			// Освобождаем интерфейс
			try {
				usbConnection.releaseInterface(usbInterface);
			} catch (Exception e) {
				Logging.v("Исключение при попытке освободить интерфейс");
				e.printStackTrace();
				this.stopSelf();
				return;
			}
			
			if (result >= 0) {
				wasSend = true;
				Logging.v("Выслано сообщение: " + msg);
			} else {
				wasSend = false;
				Logging.v("Не удалось выслать сообщение: " + msg);
			}
		} else {
			wasSend = false;
			Logging.v("Не удалось выслать сообщение: " + msg + ". Скорее всего, USB-устройство не подключено");
		}
	}

	public void onDestroy() {
		try {
			Logging.v("Уничтожаем сервис");
			if (wasSend) {
				// Сообщение успешно передано		
				Logging.v("Сообщение успешно передано");
				
				if (Globals.DEBUG_MODE) {
					Notifications.showOkMessage(getApplicationContext(),
							"Сообщение успешно передано");
				}
			} else {
				Notifications
						.showError(getApplicationContext(),
								"Ошибка при передаче сообщения. Возможно, USB-соединение не установлено");
			}
			super.onDestroy();
		} catch (Exception e) {
			Logging.v("Исключение при попытке уничтожить сервис");
			e.printStackTrace();
			super.onDestroy();
		}
	}
}