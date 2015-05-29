package com.isosystem.smarthouse.connection;

import android.content.Context;
import android.content.Intent;

import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

/**
 * ����� ���������� ���������.
 * <br>
 * ������ ����� ��������������� ������������ �������� ��������� �����������.
 * ������� ���������� ��������� �������: ����� ������������ �������� ������
 * "����������" �� ��������, ���������� ���� �� ������� ������������ ���������:
 * {@link #SendValueMessage(String, String, Boolean)} - ������� int ��������
 * {@link #sendBooleanMessage(String, int, Boolean)} - ������� �������� �������� (0|1)
 * {@link #SendRawMessage(String)} - ������� ���������, ������� ���� ����������
 * ������ �� �������, ����� ������������ ���������, �������� ����� {@link #Send(String)}, �������
 * ��������� {@link USBSendService} � �������� ��� �������������� ��������� ��� �������
 *
 */
public class MessageDispatcher {

	private Context mContext;

	public MessageDispatcher(Context c) {
		this.mContext = c;
	}

	/**
	 * ������������ �������� ����������� ��� ����������� ��������
	 * � �������� ������� ���������� �������� ������� ��������� (�������� ������������ ��� �������� ����
	 * � ���������� �������� � ������� int).
	 * ��������� ����������� ��� [�������],[��������]
	 * ��������, ���� �������: Z, � �� ������ 150, �� ��������� ����� Z,150
	 *
	 * @param prefix ������� ���������
	 * @param value ���������� ��������
	 * @param isSending �������� ������� ��� ����
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
	 * ������������ ��������� �������� �������� ����������� ��� ����������� ��������
	 * � �������� ������� ���������� �������� ������� ��������� (�������� ������������ ��� �������� ����
	 * � ���������� �������� � ������� int), � ����� ��� ��������
	 * ��������� ����������� ��� [�������],[��������_1]-[��������2]
	 * ��������, ���� �������: Z, � �� ������ 150, �� ��������� ����� Z,150
	 *
	 * @param prefix ������� ���������
	 * @param first_value ������ ���������� ��������
	 * @param second_value ������ ���������� ��������
	 * @param isSending �������� ������� ��� ����
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
	 * ������������ �������� �������� �����������
	 * ������ ���������: [�������],[0|1]
	 * 
	 * @param value ������� �������� (0 ��� 1)
	 * @return �������������� ���������
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
	 * ������������ ��������� as-is (���������� ����� ��, ��� ���� ����������)
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
	 * ������ {@link USBSendService} ��� �������� ���������.
	 *
	 * @param message ���������
	 */
	private void Send(String message) {
		Intent i = new Intent(mContext.getApplicationContext(),
				USBSendService.class);
		i.putExtra("message", message);
		mContext.startService(i);
	}
}
