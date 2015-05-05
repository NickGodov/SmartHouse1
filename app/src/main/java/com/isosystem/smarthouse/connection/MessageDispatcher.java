package com.isosystem.smarthouse.connection;

import android.content.Context;
import android.content.Intent;

import com.isosystem.smarthouse.logging.Logging;

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
	
	/**
	 * ������������ ��������� ��� ���������
	 */
	public void SendRawMessage (String message) {
		Send(message);
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
