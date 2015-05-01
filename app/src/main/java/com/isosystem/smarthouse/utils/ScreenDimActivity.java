package com.isosystem.smarthouse.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.DigitalClock;
import android.widget.RelativeLayout;

import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ScreenDimActivity extends Activity {
	MyApplication mApplication;
	
	// Использовать ли хранитель экрана
	Boolean mUseScreenSaver = false;
	
	// Время бездействия (секунды)
	int mScreenSaverIdleTime;
	
	// Для хранителя экрана, счетчик бездействия
	Handler mScreenSaverHandler;

	Activity mActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_screendim);
		
		mActivity = this;

		// Запрет на отключение экрана
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Включаем полноэкранный режим планшета
		// getActionBar().hide();
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		decorView.setSystemUiVisibility(8);
		// <<-----------------------------------
		
		mApplication = (MyApplication) getApplicationContext();

		int dimLevel;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);
		
		dimLevel = prefs.getInt("screen_dim_brightness", 95);
		Boolean useClock = prefs.getBoolean("enable_clock", false);
	
		int clockFontSize = 200;
		
		if (useClock) {
			try {
				clockFontSize = Integer.parseInt(prefs.getString("clock_font_size", "200"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
				
		DigitalClock dc = (DigitalClock) findViewById(R.id.digitalClock1);
		RelativeLayout fm = (RelativeLayout) findViewById(R.id.RelativeLayout2);
		
		if (useClock) {
			Typeface bottom_font = Typeface.createFromAsset(this.getAssets(), "digital.ttf");
			dc.setTypeface(bottom_font);
			dc.setTextSize(clockFontSize);
			dc.invalidate();
			fm.setAlpha(1.0f);
		} else {
			dc.setVisibility(View.INVISIBLE);
			fm.setAlpha(dimLevel / 100.0f);
		}
	}

	@Override
	public void onUserInteraction() {
		this.finish();
	}
	
	@Override
	protected void onResume() {			
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);
		// Проверка использования хранителя экрана
		mUseScreenSaver = prefs.getBoolean("enable_screen_saver", false);

		// Считывание времени бездействия и запуск Handler`а
		if (mUseScreenSaver) {
			mScreenSaverIdleTime = Integer.parseInt(prefs.getString(
					"screen_saver_idle_time", "25"));

			mScreenSaverHandler = new Handler();
			// Количество секунд * 1000
			mScreenSaverHandler.postDelayed(mScreenSaverRunnable,
					mScreenSaverIdleTime * 1000);
		}
		super.onResume();
	}
	
	private Runnable mScreenSaverRunnable = new Runnable() {
		public void run() {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(mApplication);

			// FALSE, если затемнение
			// TRUE, если слайд-шоу
			Boolean screenSaverMode = true;

			// Используется ли затемнение экрана
			Boolean useScreenDim = prefs.getBoolean("enable_screen_dim", false);

			// Если используется затемнение,
			// необходимо определить наступило ли время работы режима
			if (useScreenDim) {
				String time = prefs.getString("screen_dim_enable_time",
						"19:00-8:00");

				// Парсинг и конца работы
				// Например, 19:00-8:00 разбивается на [19:00] и [8:00]
				String[] time_period = time.split("-");

				// Парсинг значения начала работы
				// Например 19:00 разбивается на [19] и [00]
				String hour_start = time_period[0].split(":")[0];
				String minute_start = time_period[0].split(":")[1];
				
				// Парсинг значения начала работы
				// Например 8:00 разбивается на [8] и [00]
				String hour_end = time_period[1].split(":")[0];
				String minute_end = time_period[1].split(":")[1];

				// Получение временного промежутка в виде целых числа типа 1800
				// Например 19:45 преобразовывается в 1945
				int start_time = Integer.parseInt(hour_start) * 100
						+ Integer.parseInt(minute_start);
				int end_time = Integer.parseInt(hour_end) * 100
						+ Integer.parseInt(minute_end);

				// Получение текущего времени в виде целого числа типа 1800
				SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
				int current_time = Integer.parseInt(sdf.format(Calendar
						.getInstance().getTime()));

				// Необходимо сравнить текущее время и время для затемнения,
				// чтобы определить - какой вариант хранителя использовать.
				// Если временной промежуток задан в течение одного дня
				// "с 10 до 18", тогда current_time должно быть
				// больше start_time, но меньше end_time
				// Если промежуток "с 19 до 12", тогда current_time должно быть
				// или больше start_time, или меньше end_time
				// Если старт и конец работы совпадают - круглосуточно действует
				// затемнение
				if (start_time < end_time && current_time >= start_time
						&& current_time <= end_time)
					screenSaverMode = false;
				else if (start_time > end_time
						&& (current_time >= start_time || current_time <= end_time))
					screenSaverMode = false;
				else if (start_time == end_time)
					screenSaverMode = false;
			}

			if (screenSaverMode) {
				// Слайд-шоу
				Intent i = new Intent(mActivity, ScreenSaverActivity.class);
				startActivity(i);
			} else {
				mScreenSaverHandler.removeCallbacks(mScreenSaverRunnable);
				mScreenSaverHandler.postDelayed(mScreenSaverRunnable,
						mScreenSaverIdleTime * 1000);
			}
		}
	};
	
	@Override
	protected void onPause() {
		if (mScreenSaverHandler != null) {
			mScreenSaverHandler.removeCallbacks(mScreenSaverRunnable);
		}
		super.onPause();
	}
	
}