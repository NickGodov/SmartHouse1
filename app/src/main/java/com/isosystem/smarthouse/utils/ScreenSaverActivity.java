package com.isosystem.smarthouse.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ScreenSaverActivity extends Activity {

	public ScreenSaverActivity() {
	}
	
	// Использовать ли хранитель экрана
	Boolean mUseScreenSaver = false;
	
	// Время бездействия (секунды)
	int mScreenSaverIdleTime;
	
	// Для хранителя экрана, счетчик бездействия
	Handler mScreenSaverHandler;

	Activity mActivity;
	
	ImageSwitcher mImageSwitcher;
	Context mContext;	
	MyApplication mApplication;
	Handler mHandler; 
	ArrayList<String> mImages;
	
	int mImageChangeTimeout;
	
	int mImageIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_screensaver);
		
		mActivity = this;
		
		// Запрет на отключение экрана
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Включаем полноэкранный режим планшета
        if (getActionBar() != null) {
            getActionBar().hide();
        }
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		decorView.setSystemUiVisibility(8);
		// <<-----------------------------------
			
		mHandler = new Handler();
		
		mContext = this;
		mApplication =(MyApplication) getApplicationContext();
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);
		
		// Считывается значение тайм-аута
		try {
			mImageChangeTimeout = Integer.parseInt(prefs.getString("slide_show_image_change_time", "5"));
		} catch (Exception e) {
			// Logging.v("Ошибка при попытке считать значение тайм-аута");
			e.printStackTrace();
		}
				
		mImages = getImages();
		
		mImageSwitcher = (ImageSwitcher) findViewById(R.id.screen_saver_image_switcher);
		
		mImageSwitcher.setInAnimation(this, R.anim.flipin);
		mImageSwitcher.setOutAnimation(this, R.anim.flipout);
		
		mImageSwitcher.setFactory(new ViewFactory() {
			@Override
			public View makeView() {

				ImageView imageView = new ImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

				LayoutParams params = new ImageSwitcher.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

				imageView.setLayoutParams(params);
				return imageView;
			}
		});
		
		if (mImages.size() > 0){
//			mImageIndex = mImages.size()-1;
//			Drawable d = new BitmapDrawable(decodeSampledBitmapFromResource(getResources(), mImages.get(mImageIndex), 1000, 1000));
//			mImageSwitcher.setImageDrawable(d);
		} else {
			mImageSwitcher.setImageResource(R.drawable.screensaver_default);
		}
	}
	
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
		
	public static Bitmap decodeSampledBitmapFromResource(Resources res, String path,
	        int reqWidth, int reqHeight) {
		
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (mImages.size() > 0){
			mStatusChecker.run();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(mStatusChecker);
	}

	// Непосредственная обработка буфера
	Runnable mStatusChecker = new Runnable() {
		@SuppressWarnings("deprecation")
		@Override
		public void run() {			
			Drawable d;
			Bitmap b;
			
			if (mImageIndex == 0) {
				mImageIndex = mImages.size()-1;				
				b = decodeSampledBitmapFromResource(getResources(), mImages.get(mImageIndex), 1000, 1000);
			} else {
				b = decodeSampledBitmapFromResource(getResources(), mImages.get(--mImageIndex), 1000, 1000);
			}
			d = new BitmapDrawable(b);
			mImageSwitcher.setImageDrawable(d);
			System.gc();
			mHandler.postDelayed(mStatusChecker, mImageChangeTimeout*1000);
		}
	};
	
	private ArrayList<String> getImages() {
		ArrayList<String> images = new ArrayList<String>();
		File file = new File(Environment.getExternalStorageDirectory() + File.separator + Globals.EXTERNAL_ROOT_DIRECTORY + File.separator + Globals.EXTERNAL_SCREENSAVER_IMAGES_DIRECTORY);

		if (file.isDirectory()) {
			File[] listFile = file.listFiles();
            for (File f : listFile) {
                images.add(f.getAbsolutePath());
            }
		}
		return images;
	}
	
	@Override
	public void onUserInteraction()
	{
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
				mScreenSaverHandler.removeCallbacks(mScreenSaverRunnable);
				mScreenSaverHandler.postDelayed(mScreenSaverRunnable,
						mScreenSaverIdleTime * 1000);
			} else {
				// Затемнение экрана
				Intent i = new Intent(mActivity, ScreenDimActivity.class);
				startActivity(i);
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