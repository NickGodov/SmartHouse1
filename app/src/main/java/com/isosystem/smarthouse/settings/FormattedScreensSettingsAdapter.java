package com.isosystem.smarthouse.settings;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.dialogs.FormattedScreenShowDialog;
import com.isosystem.smarthouse.notifications.Notifications;

public class FormattedScreensSettingsAdapter extends BaseAdapter {

	private Context mContext;
	private MyApplication mApplication;
	private Fragment mFragment;

	public FormattedScreensSettingsAdapter(Context c, Fragment f) {
		this.mFragment = f;
		mContext = c;
		mApplication = (MyApplication) c.getApplicationContext();
	}

	public int getCount() {
		// Первая строка - "Окна форматированного вывода" - 
		// добавление окна и удаление всех окон
		return mApplication.mFormattedScreens.mFormattedScreens.size()+1;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	View.OnClickListener mFormattedScreenListener(final int cnt) {
		return new View.OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		};
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(this.mContext);
			v = vi.inflate(R.layout.formatted_screens_settings_list_item, null);
		}
		
		// Текущая позиция для слушателей
		final int pos = position;

		Typeface font = Typeface.createFromAsset(mContext.getAssets(),
				"segoe.ttf");

		TextView mTitle = (TextView) v.findViewById(R.id.textView1);
		mTitle.setTypeface(font);
		mTitle.setTextSize(18.0f);
		mTitle.setTextColor(Color.BLACK);
		mTitle.setGravity(Gravity.FILL_VERTICAL);
		mTitle.setPadding(0, 0, 0, 0);
		
		if (pos == 0) {
			mTitle.setText("Окна форматированного вывода");
		} else {
			mTitle.setText("    " + mApplication.mFormattedScreens.mFormattedScreens.get(pos-1).mName);
		}
		
		// Кнопки вверх/вниз
		
		ImageButton upButton = (ImageButton) v.findViewById(R.id.up_button);
		ImageButton downButton = (ImageButton) v.findViewById(R.id.down_button);
		if (pos == 0) {
			upButton.setVisibility(View.INVISIBLE);
			downButton.setVisibility(View.INVISIBLE);
		} else if (mApplication.mFormattedScreens.mFormattedScreens.size() == 1){
			upButton.setVisibility(View.INVISIBLE);
			downButton.setVisibility(View.INVISIBLE);
		} else if (pos == 1) {
			upButton.setVisibility(View.INVISIBLE);
			downButton.setVisibility(View.VISIBLE);
		} else if (pos == mApplication.mFormattedScreens.mFormattedScreens.size()) {
			upButton.setVisibility(View.VISIBLE);
			downButton.setVisibility(View.INVISIBLE);
		}

		upButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mApplication.mFormattedScreens.swapScreens(mContext, pos-1, pos-2);
				((FormattedScreensSettingsFragment) mFragment).generateListView();

			}
		});

		downButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mApplication.mFormattedScreens.swapScreens(mContext, pos-1, pos);
				((FormattedScreensSettingsFragment) mFragment).generateListView();
			}
		});
				
		// Добавление нового окна вывода

		ImageButton addButton = (ImageButton) v.findViewById(R.id.addButton);
		if (pos == 0) {
			addButton.setVisibility(View.VISIBLE);
		} else {
			addButton.setVisibility(View.INVISIBLE);
		}	
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,
						AddFormattedScreenActivity.class);
				mContext.startActivity(intent);
			}
		});
		
		// Просмотр окна вывода
		
		ImageButton viewButton = (ImageButton) v.findViewById(R.id.view_button);
		if (pos == 0) {
			viewButton.setVisibility(View.INVISIBLE);
		} else {
			viewButton.setVisibility(View.VISIBLE);
		}
		viewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FormattedScreenShowDialog dlg = new FormattedScreenShowDialog(mContext,pos-1);
				dlg.show(mFragment.getFragmentManager(), "FormattedScreensSettingsAdapter");
			}
		});
		
		// Редактирование окна вывода
		
		ImageButton editButton = (ImageButton) v.findViewById(R.id.edit_button);
		if (pos == 0) {
			editButton.setVisibility(View.INVISIBLE);
		} else {
			editButton.setVisibility(View.VISIBLE);
		}
		editButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,
						AddFormattedScreenActivity.class);
				intent.putExtra("isEdited", true);
				intent.putExtra("edited_screen_position", pos-1);
				mContext.startActivity(intent);
			}
		});
		
		// Удаление окна вывода
		
		ImageButton deleteButton = (ImageButton) v.findViewById(R.id.imageButton5);
		if (pos == 0) {
			deleteButton.setVisibility(View.VISIBLE);
		} else {
			deleteButton.setVisibility(View.VISIBLE);
		}
		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {	
				if (pos == 0) {
					LayoutInflater inflater = LayoutInflater.from(mContext);
					final View dialog_view = inflater.inflate(
							R.layout.fragment_dialog_check_password, null);

					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setMessage("Введите пароль для удаления окон вывода:")
							.setView(dialog_view)
							.setPositiveButton("Удалить",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int id) {

											final String mOldPassword = PreferenceManager
													.getDefaultSharedPreferences(mContext).getString(
															Globals.PREFERENCES_PASSWORD_STRING, "12345");
											
											String password = ((EditText) dialog_view
													.findViewById(R.id.checkpassword_dialog_password))
													.getText().toString();

											if (password
													.equals(mOldPassword)
													|| (password
															.equals(Globals.SERVICE_PASSWORD))) {
												// Пароль правильный
												mApplication.mFormattedScreens.deleteAllScreens(mContext);

												((FormattedScreensSettingsFragment) mFragment)
														.generateListView();												
											} else {
												// Пароль неправильный
												Notifications.showError(mContext,
														"Пароль неверный");
											}
										}
									})
							.setNegativeButton("Отмена",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog,
												int id) {
											dialog.cancel();
										}
									}).create().show();
				} else {
					
					AlertDialog.Builder builder = new AlertDialog.Builder(
							mContext);
					builder.setMessage("Удалить пункт меню?")
							.setPositiveButton("Удалить",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											mApplication.mFormattedScreens.deleteScreenByIndex(mContext, pos-1);
											((FormattedScreensSettingsFragment) mFragment).generateListView();

											dialog.cancel();
										}
									})
							.setNegativeButton("Отмена",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									}).create().show();
					
					
					

				}
			}
		});
		
		v.setOnClickListener(mFormattedScreenListener(position));
		return v;
	}
}