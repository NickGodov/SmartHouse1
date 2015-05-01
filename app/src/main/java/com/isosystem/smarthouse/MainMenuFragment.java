/*
 * Мобильное приложение для проекта "Умный дом"
 * 
 * author: Годовиченко Николай
 * email: nick.godov@gmail.com
 * last edit: 11.09.2014
 */

package com.isosystem.smarthouse;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;
import com.isosystem.smarthouse.settings.SettingsActivity;

/**
 * Этот класс фрагмента окна основного меню. Выводит и обновляет ListView с
 * пунктами меню. Для наполнения ListView используется кастомный адаптер
 * {@link MainMenuAdapterGrid}.
 * 
 * @author Годовиченко Николай (nick.godov@gmail.com)
 * @see MainMenuAdapterGrid
 */
public class MainMenuFragment extends Fragment {

	MyApplication mApplication;
	AlarmMessageReceiver mReceiver;

	Drawable mDefaultBackground;

	ImageView mUsbConnectedIcon;
	ImageView mPowerSupplyIcon;

	TextView mMessagesNumber;
	ImageView mMessagesIcon;

	Boolean mFragmentLoaded = false;

	Boolean mTileMode = true;

	public MainMenuFragment() {
	}

	/** ListView с пунктами меню */
	static GridView mMenuGridView;
	static ListView mMenuListView;
	static View rootView;
	TextView mMenuHeader;
	TextView mMessagesText;

	/**
	 * При старте фрагмента:<br>
	 * 1) Получаем и обновляем ListView; <br>
	 * 2) Получаем кнопку свайпа вправо; <br>
	 * 3) Пишем слушатель на клик (переход на фрагмент 1).
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mApplication = (MyApplication) container.getContext()
				.getApplicationContext();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);

		mTileMode = prefs.getBoolean("use_main_menu_tile", true);
		if (mTileMode) {
			rootView = inflater.inflate(R.layout.fragment_main_mainmenu,
					container, false);
		} else {
			rootView = inflater.inflate(R.layout.fragment_main_mainmenu_list,
					container, false);
		}

		mDefaultBackground = rootView.getBackground();

		mUsbConnectedIcon = (ImageView) rootView
				.findViewById(R.id.image_usb_connection);
		checkUsbConnectionIcon();

		mPowerSupplyIcon = (ImageView) rootView
				.findViewById(R.id.image_power_connection);
		checkPowerSupplyIcon();

		// Иконка для количества сообщений
		mMessagesIcon = (ImageView) rootView.findViewById(R.id.imageView3);

		// Текстовая надпись для количества сообщений
		mMessagesNumber = (TextView) rootView.findViewById(R.id.textView1);
		mMessagesNumber.setTypeface(Typeface.createFromAsset(rootView
				.getContext().getAssets(), "fonto.ttf"));
		mMessagesNumber.setTextColor(Color.BLACK);

		// Настройка иконки и надписи
		setMessageNumberIcon();

		// Настройка заголовка меню
		Typeface font = Typeface.createFromAsset(rootView.getContext()
				.getAssets(), "russo.ttf");
		mMenuHeader = (TextView) rootView.findViewById(R.id.menuheader_text);
		mMenuHeader.setTypeface(font);
		// mMenuHeader.setBackgroundColor(Color.parseColor("#e74c3c"));
		mMenuHeader.setTextColor(Color.parseColor("white"));
		mMenuHeader.setTextSize(35.0f);
		// <-----------------------

		if (mTileMode) {
			mMenuGridView = (GridView) rootView.findViewById(R.id.mainlist1);
		} else {
			mMenuListView = (ListView) rootView.findViewById(R.id.mainlist1);
		}

		// Надпись внизу, URL
		TextView textView = (TextView) rootView.findViewById(R.id.mfl_url);
		font = Typeface.createFromAsset(rootView.getContext().getAssets(),
				"code.otf");
		textView.setTypeface(font);
		textView.setText(this.getString(R.string.company_url));
		textView.setTextSize(15.0f);
		textView.setTextColor(Color.parseColor("white"));
		textView.invalidate();

		// Надпись внизу, телефон
		textView = (TextView) rootView.findViewById(R.id.mfl_phone);
		font = Typeface.createFromAsset(rootView.getContext().getAssets(),
				"code.otf");
		textView.setTypeface(font);
		textView.setText(this.getString(R.string.company_phone));
		textView.setTextSize(15.0f);
		textView.setTextColor(Color.parseColor("white"));
		textView.invalidate();

		mFragmentLoaded = true;

		return rootView;
	}

	@Override
	public void onStart() {
		try {
			mReceiver = new AlarmMessageReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction("SMARTHOUSE.ALARM_MESSAGE_RECEIVED");
			filter.addAction("SMARTHOUSE.POWER_SUPPLY_CHANGED");
			rootView.getContext().registerReceiver(mReceiver, filter);
			Logging.v("Регистрируем ресивер MainActivity");
		} catch (Exception e) {
			Logging.v("Исключение при попытке зарегистрировать ресивер");
			e.printStackTrace();
		}

		if (mTileMode) {
			configureGridView();
		} else {
			configureListView();
		}

		checkPowerSupplyIcon();
		checkUsbConnectionIcon();
		setMessageNumberIcon();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);

		// Если в настройках выставлено использование своего фона
		if (!prefs.getBoolean("use_default_main_menu_background", true)) {
			String filepath = prefs.getString("choose_main_menu_background",
					"no-image");
			// Если установлен путь к изображению
			if (!filepath.equals("no-image")) {
				BitmapDrawable navigationBackground = new BitmapDrawable(
						filepath);
				// Если фон плиткой
				if (prefs.getBoolean("main_menu_background_tile", true)) {
					navigationBackground.setTileModeXY(Shader.TileMode.REPEAT,
							Shader.TileMode.REPEAT);
				}
				rootView.setBackgroundDrawable(navigationBackground);
			}
		} else {
			rootView.setBackgroundDrawable(mDefaultBackground);
		}

		super.onStart();
	}

	// Настройка вывода GridView для плиток
	private void configureGridView() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);

		// Количество колонок в ряду
		int num_columns = Integer.parseInt(prefs.getString(
				"main_menu_tiles_in_row", "5"));
		mMenuGridView.setNumColumns(num_columns);

		// Режим растягивания
		int stretch_mode = Integer.parseInt(prefs.getString(
				"main_menu_tile_stretch_mode", "2"));
		mMenuGridView.setStretchMode(stretch_mode);

		// Горизонтальный зазор между плитками
		int horizontal_spacing = Integer.parseInt(prefs.getString(
				"main_menu_tile_horizontal_spacing", "1"));
		mMenuGridView.setHorizontalSpacing(horizontal_spacing);

		// Если указан квадратный зазор, то выставляем одинаковый горизонтальный
		// и вертикальный зазоры
		// иначе, берем значение из настроек
		boolean square_spacing = prefs.getBoolean(
				"main_menu_tile_align_vertical_spacing", true);
		if (square_spacing) {
			mMenuGridView.setVerticalSpacing(horizontal_spacing);
		} else {
			int vertical_spacing = Integer.parseInt(prefs.getString(
					"main_menu_tile_vertical_spacing", "1"));
			mMenuGridView.setVerticalSpacing(vertical_spacing);
		}
	}

	// Настройка вывода ListView для списка
	private void configureListView() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);

		// Количество колонок в ряду
		int divider_height = Integer.parseInt(prefs.getString(
				"main_menu_list_divider_height", "5"));
		boolean transparent_divider = prefs.getBoolean(
				"main_menu_list_divider_transparent", true);

		if (transparent_divider) {
			ColorDrawable transparent = new ColorDrawable(
					android.R.color.transparent);
			mMenuListView.setDivider(transparent);
		} else {
			ColorDrawable div_color = new ColorDrawable(Color.LTGRAY);
			mMenuListView.setDivider(div_color);
		}

		mMenuListView.setDividerHeight(divider_height);

		int margins = Integer.parseInt(prefs.getString(
				"main_menu_list_margins", "10"));

		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mMenuListView
				.getLayoutParams();

		mlp.setMargins(margins, 5, margins, 5);

		mMenuListView.setLayoutParams(mlp);
	}

	@Override
	public void onStop() {
		try {
			rootView.getContext().unregisterReceiver(mReceiver);
			Logging.v("Освобождаем ресивер MainActivity");
		} catch (Exception e) {
			Logging.v("Исключение при попытке освободить ресивер");
			e.printStackTrace();
		}
		super.onStop();
	}

	/**
	 * Данный метод обновляет ListView
	 *
	 *            заглавныйузел поддерева. Дочерние узлы будут показаны как
	 *            пункты меню
	 */
	public void reloadListViewMenu() {

		checkPowerSupplyIcon();
		checkUsbConnectionIcon();
		setMessageNumberIcon();

		if (mApplication.mTree.mPageReturnNode == null)
			mApplication.mTree.mPageReturnNode = mApplication.mTree.rootNode;

		if (mTileMode) {
			mMenuGridView.setAdapter(new MainMenuAdapterGrid(rootView
					.getContext(), mApplication.mTree.mPageReturnNode, this));
		} else {
			mMenuListView.setAdapter(new MainMenuAdapterList(rootView
					.getContext(), mApplication.mTree.mPageReturnNode, this));
		}
	}

	public void setMenuHeaderText(String headerText) {
		mMenuHeader.setText(headerText);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser) {
			if (mFragmentLoaded)
				setMessageNumberIcon();
		}
	}

	@Override
	public void onResume() {

		checkPowerSupplyIcon();
		checkUsbConnectionIcon();
		setMessageNumberIcon();

		reloadListViewMenu();
		super.onResume();
	}

	class AlarmMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("SMARTHOUSE.POWER_SUPPLY_CHANGED"))
				checkPowerSupplyIcon();
			checkUsbConnectionIcon();
			setMessageNumberIcon();
		}
	}

	private Boolean isSupplyEnabled() {
		Intent intent = mApplication.registerReceiver(null, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
        int plugged = 0;
		plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
	
		boolean result = (plugged != 0 && plugged!=-1);
		return result;
	}

	private void setMessageNumberIcon() {

		mApplication = (MyApplication) rootView.getContext()
				.getApplicationContext();

		try {
			// Если новых сообщений нет - прячем иконку
			if (mApplication.mAlarmMessages.mAlarmMessages.size() == 0) {
				mMessagesIcon.setVisibility(View.INVISIBLE);
				mMessagesNumber.setVisibility(View.INVISIBLE);
			} else {
				mMessagesIcon.setVisibility(View.VISIBLE);
				mMessagesNumber.setVisibility(View.VISIBLE);
				mMessagesNumber.setText(String
						.valueOf(mApplication.mAlarmMessages.mAlarmMessages
								.size()));
			}
		} catch (Exception e) {
			Logging.v("Исключение при попытке выполнить onReceive в MainActivity");
			e.printStackTrace();
		}

		if (mMessagesNumber.getText().toString().length() == 1) {
			mMessagesNumber.setTextSize(25.0f);
		} else if (mMessagesNumber.getText().toString().length() == 2) {
			mMessagesNumber.setTextSize(23.0f);
		} else if (mMessagesNumber.getText().toString().length() == 3) {
			mMessagesNumber.setTextSize(21.0f);
		} else if (mMessagesNumber.getText().toString().length() == 4) {
			mMessagesNumber.setTextSize(19.0f);
		}

		mMessagesIcon.invalidate();
		mMessagesNumber.invalidate();
	}

	private void checkPowerSupplyIcon() {
		if (isSupplyEnabled()) {
			mPowerSupplyIcon.setImageResource(R.drawable.tablet_power_on);
		} else {
			mPowerSupplyIcon.setImageResource(R.drawable.tablet_power_off);
		}
	}

	private void checkUsbConnectionIcon() {
		if (mApplication.isUsbConnected) {
			mUsbConnectedIcon.setImageResource(R.drawable.tablet_connection_on);
		} else {
			mUsbConnectedIcon
					.setImageResource(R.drawable.tablet_connection_off);
		}
	}

	View.OnClickListener mSettingsButtonListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutInflater inflater = LayoutInflater.from(mApplication);
				final View dialog_view = inflater.inflate(
						R.layout.fragment_dialog_check_password, null);

				// Включение полноэкранного режим планшета
				int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

				dialog_view.setSystemUiVisibility(uiOptions);
				dialog_view.setSystemUiVisibility(8);

				final String mOldPassword = PreferenceManager
						.getDefaultSharedPreferences(mApplication).getString(
								Globals.PREFERENCES_PASSWORD_STRING, "12345");

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setMessage("Введите пароль для входа в настройки:")
						.setView(dialog_view)
						.setPositiveButton("Войти",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {

										String password = ((EditText) dialog_view
												.findViewById(R.id.checkpassword_dialog_password))
												.getText().toString();

										Boolean correct_password = false;
										if (password.equals(mOldPassword)
												|| (password
														.equals(Globals.SERVICE_PASSWORD)))
											correct_password = true;

										if (correct_password) {
											// Пароль правильный

											Intent intent = new Intent(
													getActivity(),
													SettingsActivity.class);
											getActivity().startActivity(intent);
										} else {
											// Пароль неправильный
											Notifications.showError(
													mApplication,
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
			}
		};
	}

}