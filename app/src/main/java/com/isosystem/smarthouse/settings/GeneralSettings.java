package com.isosystem.smarthouse.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;

public class GeneralSettings extends Fragment {

	public GeneralSettings() {
	}

	View rootView;
	MyApplication mApplication;

	Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_general_settings,
				container, false);

		mApplication = (MyApplication) rootView.getContext()
				.getApplicationContext();

		mContext = rootView.getContext();

		Button mImportExportFiles = (Button) rootView
				.findViewById(R.id.button_import_export);
		mImportExportFiles.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(mContext, ImportExportFilesActivity.class);
				mContext.startActivity(i);
			}
		});

		Button mActivityPreferences = (Button) rootView
				.findViewById(R.id.gs_activity_preferences);
		mActivityPreferences.setOnClickListener(mActivityPreferencesListener);

		Button mExitApplication = (Button) rootView
				.findViewById(R.id.gs_quit_application);
		mExitApplication.setOnClickListener(mExitApplicationListener);

		ImageButton mReturnButton = (ImageButton) rootView
				.findViewById(R.id.mms_return_button);
		mReturnButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});

		return rootView;
	}

	/**
	 * Слушатель для кнопки "Настройки"
	 */
	private OnClickListener mActivityPreferencesListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			Intent i = new Intent(mContext,
					ApplicationPreferencesActivity.class);
			mContext.startActivity(i);
		}
	};

	private OnClickListener mExitApplicationListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("Вы действительно хотите выйти из приложения?")
					.setPositiveButton("Выйти",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									
									// Закрыть приложение
									
									Activity a = (Activity)mContext;
									a.finishAffinity();

//									Intent intent = new Intent(mContext
//											.getApplicationContext(),
//											MainActivity.class);
//									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
//											| Intent.FLAG_ACTIVITY_NEW_TASK);
//									intent.putExtra("LOGOUT", true);
//									startActivity(intent);
								}
							})
					.setNegativeButton("Отмена",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							}).create().show();
		}
	};

}