package com.isosystem.smarthouse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.notifications.Notifications;

public class OutgoingMessagePasswordCheckDialog extends DialogFragment {

	String prefix = "";

	public OutgoingMessagePasswordCheckDialog(String pr) {
		super();

		this.prefix = pr;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);

		final EditText input = new EditText(this.getActivity());
		input.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Введите префикс сообщения и пароль:")
				.setView(input)
				.setPositiveButton("Проверить",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								MessageDispatcher dispatcher = new MessageDispatcher(
										getActivity());

								String msg = dispatcher.SendValueMessage(
										prefix, input.getText().toString(),
										false);
								Notifications.showTooltip(getActivity(),
										"Сообщение контроллеру: " + msg);
							}
						})
				.setNegativeButton("Отмена",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								OutgoingMessagePasswordCheckDialog.this
										.dismiss();
							}
						});
		return builder.create();
	}
}