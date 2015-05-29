package com.isosystem.smarthouse.connection;

import android.content.Context;
import android.content.Intent;

import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

/**
 * Класс диспетчера сообщений.
 * <br>
 * Данный класс непосредственно осуществляет отправку сообщений контроллеру.
 * Отсылка происходит следующим образом: когда пользователь нажимает кнопку
 * "Установить" на планшете, вызывается один из методов формирования сообщений:
 * {@link #SendValueMessage(String, String, Boolean)} - отсылка int значения
 * {@link #sendBooleanMessage(String, int, Boolean)} - отсылка булевого значения (0|1)
 * {@link #SendRawMessage(String)} - отсылка сообщения, которое ввел настройщик
 * Каждый из методов, после формирования сообщения, вызывает метод {@link #Send(String)}, который
 * запускает {@link USBSendService} и передает ему сформированное сообщение для отсылки
 *
 */
public class MessageDispatcher {

	private Context mContext;

	public MessageDispatcher(Context c) {
		this.mContext = c;
	}

	/**
	 * Формирование значения контроллеру для последующей отправки
	 * В качестве входных аргументов получаем префикс сообщения (вводится настройщиком при создании окна
	 * и отсылаемое значение в формате int).
	 * Сообщение формируется как [префикс],[значение]
	 * Например, если префикс: Z, а на выходе 150, то сообщение будет Z,150
	 *
	 * @param prefix префикс сообщения
	 * @param value отсылаемое значение
	 * @param isSending реальная отсылка или тест
	 */
	public String SendValueMessage(String prefix, String value, Boolean isSending) {
		StringBuilder sendingMessage = new StringBuilder();
		
		sendingMessage.append(prefix);
		sendingMessage.append(",");
		sendingMessage.append(value);

		if (isSending) {
			Send(sendingMessage.toString());
		}

		return sendingMessage.toString();
	}

	/**
	 * Формирование диапазона числовых значений контроллеру для последующей отправки
	 * В качестве входных аргументов получаем префикс сообщения (вводится настройщиком при создании окна
	 * и отсылаемое значение в формате int), а также два значения
	 * Сообщение формируется как [префикс],[значение_1]-[значение2]
	 * Например, если префикс: Z, а на выходе 150, то сообщение будет Z,150
	 *
	 * @param prefix префикс сообщения
	 * @param first_value первое отсылаемое значение
	 * @param second_value второе отсылаемое значение
	 * @param isSending реальная отсылка или тест
	 */
	public String SendRangeValueMessage(String prefix, String first_value, String second_value, Boolean isSending) {
		StringBuilder sendingMessage = new StringBuilder();

		sendingMessage.append(prefix);
		sendingMessage.append(",");
		sendingMessage.append(first_value);
		sendingMessage.append("-");
		sendingMessage.append(second_value);

		if (isSending) {
			Send(sendingMessage.toString());
		}

		return sendingMessage.toString();
	}


	/**
	 * Формирование булевого значения контроллеру
	 * Формат сообщения: [префикс],[0|1]
	 * 
	 * @param value интовое значение (0 или 1)
	 * @return Сформированное сообщение
	 */
	public String sendBooleanMessage (String prefix, int value, Boolean isSending) {
		StringBuilder sendingMessage = new StringBuilder();
		
		sendingMessage.append(prefix);
		sendingMessage.append(",");
		sendingMessage.append(value);

		if (isSending) {
			Send(sendingMessage.toString());
		}
		
		return sendingMessage.toString();
	}

	public String sendTimeRangeMessage (String prefix, int first_hour, int first_minute, int
										second_hour, int second_minute,Boolean isSending) {

		StringBuilder sendingMessage = new StringBuilder();

		sendingMessage.append(prefix);
		sendingMessage.append(",");

		//sendingMessage.append("(");
		sendingMessage.append(first_hour);
		sendingMessage.append(":");
		sendingMessage.append(first_minute);
		//sendingMessage.append(")");

		sendingMessage.append("-");

		//sendingMessage.append("(");
		sendingMessage.append(second_hour);
		sendingMessage.append(":");
		sendingMessage.append(second_minute);
		//sendingMessage.append(")");

		if (isSending) {
			Send(sendingMessage.toString());
		}

		return sendingMessage.toString();
	}

	public String sendDateRangeMessage (String prefix, int first_day, int first_month, int first_year, int
			second_day, int second_month, int second_year, Boolean isSending) {

		StringBuilder sendingMessage = new StringBuilder();

		sendingMessage.append(prefix);
		sendingMessage.append(",");

		//sendingMessage.append("(");
		sendingMessage.append(first_day);
		sendingMessage.append("/");
		sendingMessage.append(first_month);
		sendingMessage.append("/");
		sendingMessage.append(first_year);
		//sendingMessage.append(")");

		sendingMessage.append("-");

		//sendingMessage.append("(");
		sendingMessage.append(second_day);
		sendingMessage.append("/");
		sendingMessage.append(second_month);
		sendingMessage.append("/");
		sendingMessage.append(second_year);
		//sendingMessage.append(")");

		if (isSending) {
			Send(sendingMessage.toString());
		}

		return sendingMessage.toString();
	}

	public String sendDateTimeRangeMessage (String prefix, int first_hour, int first_minute,
											int first_day, int first_month, int first_year, int second_hour, int second_minute,
											int	second_day, int second_month, int second_year, Boolean isSending) {

		StringBuilder sendingMessage = new StringBuilder();

		sendingMessage.append(prefix);
		sendingMessage.append(",");

		//sendingMessage.append("(");
		sendingMessage.append(first_day);
		sendingMessage.append("/");
		sendingMessage.append(first_month);
		sendingMessage.append("/");
		sendingMessage.append(first_year);
		sendingMessage.append(" ");
		sendingMessage.append(first_hour);
		sendingMessage.append(":");
		sendingMessage.append(first_minute);
		//sendingMessage.append(")");

		sendingMessage.append("-");

		//sendingMessage.append("(");
		sendingMessage.append(second_day);
		sendingMessage.append("/");
		sendingMessage.append(second_month);
		sendingMessage.append("/");
		sendingMessage.append(second_year);
		sendingMessage.append(" ");
		sendingMessage.append(second_hour);
		sendingMessage.append(":");
		sendingMessage.append(second_minute);
		//sendingMessage.append(")");

		if (isSending) {
			Send(sendingMessage.toString());
		}

		return sendingMessage.toString();
	}

	/**
	 * Формирование сообщения as-is (отсылается точно то, что ввел настройщик)
	 */
	public void SendRawMessage (String message) {
		Send(message);
	}

	public String sendGiveMeValueMessage(String message, boolean isSending) {
		StringBuilder sendingMessage = new StringBuilder();

		sendingMessage.append(message);
		sendingMessage.append(",");

		if (isSending) {
			Send(sendingMessage.toString());
		}

		return sendingMessage.toString();
	}

	/**
	 * Запуск {@link USBSendService} для отправки сообщения.
	 *
	 * @param message сообщение
	 */
	private void Send(String message) {
		Intent i = new Intent(mContext.getApplicationContext(),
				USBSendService.class);
		i.putExtra("message", message);
		mContext.startService(i);
	}
}
