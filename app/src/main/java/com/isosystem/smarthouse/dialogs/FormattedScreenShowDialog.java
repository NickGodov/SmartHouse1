package com.isosystem.smarthouse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;

import java.util.ArrayList;
import java.util.HashMap;

public class FormattedScreenShowDialog extends DialogFragment {
	int position;
	Context mContext;
	ListView list;
	MyApplication mApplication;

	public FormattedScreenShowDialog(Context context, int pos) {
		this.position = pos;
		this.mContext = context;
		mApplication = (MyApplication) mContext.getApplicationContext();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		
		list = new ListView(mContext);
		list.setPadding(10, 20, 10, 20);
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();

		/** Добавляем данные */

		// Имя
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("name", "Имя узла");
		map.put("value", mApplication.mFormattedScreens.mFormattedScreens
				.get(position).mName);
		mylist.add(map);

		// Имя родительского узла
		map = new HashMap<String, String>();
		map.put("name", "Сообщение о начале передачи данных");
		map.put("value", mApplication.mFormattedScreens.mFormattedScreens
				.get(position).mInputStart);
		mylist.add(map);

		// Тип узла
		map = new HashMap<String, String>();
		map.put("name", "Сообщение о конце передачи данных");
		map.put("value", mApplication.mFormattedScreens.mFormattedScreens
				.get(position).mInputEnd);
		mylist.add(map);

		SimpleAdapter mSchedule = new SimpleAdapter(mContext, mylist,
				R.layout.menu_item_show_layout, new String[] { "name",
						"value" }, new int[] { R.id.menu_item_name,
						R.id.menu_item_value });

		list.setAdapter(mSchedule);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(list).setPositiveButton("OK",
				positiveButtonListener);
		return builder.create();
	} // onCreate

	private DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			FormattedScreenShowDialog.this.dismiss();
		}
	}; // end listener
} // end dialog class