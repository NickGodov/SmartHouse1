package com.isosystem.smarthouse.connection;

import android.content.Context;
import android.content.Intent;

import com.isosystem.smarthouse.logging.Logging;

public class MessageDispatcher {

	private Context mContext;

	public MessageDispatcher(Context c) {
		this.mContext = c;
	}

	/**
	 * Отправка значения контроллеру
	 * В качестве входных аргументов получаем префикс сообщения (вводится настройщиком при создании окна
	 * и отсылаемое значение в формате int
	 * Сообщение формируется как [префикс][количество символов][значение]
	 * Например, если префикс: Z, а на выходе 150, то сообщение будет Z3150
	 */
	public String SendValueMessage(String prefix, String value, Boolean isSending) {
		StringBuilder sendingMessage = new StringBuilder();
		
		sendingMessage.append(prefix);
		sendingMessage.append(value.length());
		sendingMessage.append(value);
		
		Logging.v("Отсылаемое значение:" + sendingMessage.toString());
		
		if (isSending) {
			Send(sendingMessage.toString());
		}
		
		return sendingMessage.toString();
	}
	
	/**
	 * Отправка булевого значения контроллеру
	 * Формат сообщения: [префикс][0|1]
	 * 
	 * @param value интовое значение (0 или 1)
	 * @return отосланное сообщение
	 */
	public String sendBooleanMessage (String prefix, int value, Boolean isSending) {
		StringBuilder sendingMessage = new StringBuilder();
		
		sendingMessage.append(prefix);
		sendingMessage.append(value);
		
		/** Если нужно отправить в ascii-кодировке
		sendingMessage.append(EncodingUtils.getAsciiBytes(Globals.SEND_MESSAGE_BOOLEAN_PREFIX));
		sendingMessage.append(EncodingUtils.getAsciiBytes(String.valueOf(value)));
		*/
		
		if (isSending) {
			Send(sendingMessage.toString());
		}
		
		return sendingMessage.toString();
	}
	
	/**
	 * Отправка значения без изменений
	 */
	public void SendRawMessage (String message) {
		Send(message);
	}

	private void Send(String message) {
		Intent i = new Intent(mContext.getApplicationContext(),
				USBSendService.class);
		i.putExtra("message", message);
		mContext.startService(i);
	}

}
