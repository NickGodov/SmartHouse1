package com.isosystem.smarthouse.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogsFragment extends Fragment {

	public LogsFragment() {
	}

	View rootView;
	MyApplication mApplication;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_settings_logs, container,
				false);

		mApplication = (MyApplication) rootView.getContext()
				.getApplicationContext();

		TextView logView = (TextView) rootView.findViewById(R.id.logView);

		try {
			Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			StringBuilder log = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				log.append(line);
				log.append("\n");
			}
			logView.setText(log.toString());
		} catch (IOException e) {
		}

		Button writeButton = (Button) rootView
				.findViewById(R.id.write_to_file_button);
		writeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String fileName = "logcat_" + System.currentTimeMillis()
						+ ".txt";

				// Создание папки smarthouse/logs
				File log_directory = new File(Environment
						.getExternalStorageDirectory()
						+ File.separator
						+ Globals.EXTERNAL_ROOT_DIRECTORY
						+ File.separator
						+ Globals.EXTERNAL_LOGS_DIRECTORY);
				log_directory.mkdirs();
				
				File outputFile = new File(log_directory, fileName);
				try {
					Runtime.getRuntime().exec(
							"logcat -f " + outputFile.getAbsolutePath());
					Notifications.showOkMessage(rootView.getContext(),
							"Логи записаны в файл");
					Runtime.getRuntime().exec("logcat -c");
					TextView logView = (TextView) rootView
							.findViewById(R.id.logView);
					logView.setText("");
				} catch (IOException e) {
					Notifications.showError(rootView.getContext(),
							"Ошибка при записи логов в файл");
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		return rootView;
	}
}