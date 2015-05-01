package com.isosystem.smarthouse.connection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class UsbAttachEventReceiver extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getApplicationContext() == null) finish();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		if (intent!=null){
			Intent i = new Intent(getApplicationContext(),USBReceiveService.class);
			startService(i);
		}
		finish();
	}
}
