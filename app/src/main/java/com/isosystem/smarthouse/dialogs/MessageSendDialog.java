package com.isosystem.smarthouse.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.connection.MessageDispatcher;

public class MessageSendDialog extends Dialog implements
		android.view.View.OnClickListener {

	String header;
	String prefix;
	String description;
	String value;

	Activity mActivity;

	public MessageSendDialog(String hdr, String desc, String pref, String val, Activity a) {
		super(a);

		prefix = pref;
		value = val;
		description = desc;
		header = hdr;
		mActivity = a;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.message_send_dialog);

		// Включение полноэкранного режим планшета
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		decorView.setSystemUiVisibility(8);
		// <<-----------------------------------

		Typeface font = Typeface.createFromAsset(
				mActivity.getAssets(), "russo.ttf");

		TextView tv = (TextView) findViewById(R.id.header_label);

		tv.setTypeface(font);
		tv.setTextSize(30.0f);
		tv.setText(header);

		font = Typeface.createFromAsset(mActivity.getAssets(),
				"myfont.ttf");

		tv = (TextView) findViewById(R.id.description_label);
		tv.setTypeface(font);
		tv.setTextSize(20.0f);
		tv.setText(description);

		ImageButton button_ok = (ImageButton) findViewById(R.id.imageButton1);
		button_ok.setOnClickListener(this);
		ImageButton button_cancel = (ImageButton) findViewById(R.id.imageButton3);
		button_cancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageButton1:
			
			MessageDispatcher mDispatcher = new MessageDispatcher(mActivity);
			mDispatcher.SendValueMessage(prefix,value,true);

			break;
		case R.id.imageButton3:
			dismiss();
			break;
		default:
			break;
		}
		dismiss();
	}

}