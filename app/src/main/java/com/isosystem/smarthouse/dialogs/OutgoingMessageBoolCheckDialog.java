package com.isosystem.smarthouse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.notifications.Notifications;

public class OutgoingMessageBoolCheckDialog extends DialogFragment {

	String prefix = "";

	public OutgoingMessageBoolCheckDialog(String pr) {
		super();

		this.prefix = pr;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Введите префикс сообщения и нажмите на нужную кнопку:")
				.setNeutralButton("0",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								MessageDispatcher dispatcher = new MessageDispatcher(
										getActivity());

								String msg = dispatcher.sendBooleanMessage(
										prefix, 0,
										false);
								Notifications.showTooltip(getActivity(),
										"Сообщение контроллеру: " + msg);
							}
						})
				.setPositiveButton("1",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								MessageDispatcher dispatcher = new MessageDispatcher(
										getActivity());

								String msg = dispatcher.sendBooleanMessage(prefix, 1, false);
								Notifications.showTooltip(getActivity(),
										"Сообщение контроллеру: " + msg);
							}
						})
				.setNegativeButton("Отмена",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								OutgoingMessageBoolCheckDialog.this.dismiss();
							}
						});
		return builder.create();
	}
}