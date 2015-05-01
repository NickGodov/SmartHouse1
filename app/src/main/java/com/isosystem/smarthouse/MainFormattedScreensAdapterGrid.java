package com.isosystem.smarthouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.isosystem.smarthouse.logging.Logging;

import java.io.File;
import java.util.HashMap;

/**
 * Адаптер для вывода плиток для окон форматированного вывода
 * При нажатии на плитку запускается {@link com.isosystem.smarthouse.FormattedScreensActivity}
 * которому передается номер окна форматированного вывода, который соотносится с определенной плиткой
 */
public class MainFormattedScreensAdapterGrid extends BaseAdapter {

	private Context mContext;
	private MyApplication mApplication;

	public MainFormattedScreensAdapterGrid(Context c) {
		mContext = c;
		mApplication = (MyApplication) c.getApplicationContext();
	}

	public int getCount() {
		return mApplication.mFormattedScreens.mFormattedScreens.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

    /**
     * При нажатии на плитку, открывается {@link com.isosystem.smarthouse.FormattedScreensActivity},
     * которому передается номер окна вывода
     * @param cnt номер окна вывода
     * @return void
     */
	View.OnClickListener mFormattedScreenListener(final int cnt) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent(mContext,
							FormattedScreensActivity.class);

                    // Передаем номер нажатого окна в FormatterScreenActivity
					intent.putExtra("formScreenIndex", cnt);
					mContext.startActivity(intent);
					((Activity)mContext).overridePendingTransition(R.anim.flipin,R.anim.flipout);

				} catch (Exception e) {
					Logging.v("Исключение при попытке вызвать активити для окна вывода");
					e.printStackTrace();
				}
			}
		};
	}

    /**
     * При выводе View:
     * 1. Из настроек считываются параметры плиток: квадрватная ли она, ширина, высота,
     * размер картинки на плитке)
     * 2. Устанавливаются параметры плитки из настроек
     * 3. Считываются параметры i-ой плитки из MyApplication.mFormattedScreens
     * 4. Устанавливается картинка плитки и текст плитки
     * 5. Устанавливается OnClickListener
     *
     * @param position позиция плиткии соответствует окну вывода
     * @param convertView
     * @param parent
     * @return
     */
	public View getView(int position, View convertView, ViewGroup parent) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);

        //Считывание параметров из настроек

        // Является ли плитка квадратной
		boolean square_tile = prefs.getBoolean(
				"formatted_screens_tile_align_height", true);
        // Ширина плитки
		int formatted_screens_tile_width = Integer.parseInt(prefs.getString(
				"formatted_screens_tile_width", "250"));
        // Высота плитки
		int formatted_screens_tile_height = Integer.parseInt(prefs.getString(
				"formatted_screens_tile_height", "250"));
        // Размер картинки на плитке
		int formatted_screens_tile_image_size = Integer.parseInt(prefs
				.getString("formatted_screens_tile_image_size", "96"));

		View v = convertView;
		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(this.mContext);
			v = vi.inflate(R.layout.formscreen_adapter_grid_item, null);

			// Установка высоты и ширины плитки
			if (square_tile) {
				v.setLayoutParams(new GridView.LayoutParams(
						formatted_screens_tile_width,
						formatted_screens_tile_width));
			} else {
				v.setLayoutParams(new GridView.LayoutParams(
						formatted_screens_tile_width,
						formatted_screens_tile_height));
			}

			v.invalidate();
		}

		ImageView mImage = (ImageView) v
				.findViewById(R.id.formscreen_adapter_left_image);

		// Берем хеш-таблицу параметров узла
		HashMap<String, String> pMap = mApplication.mFormattedScreens.mFormattedScreens
				.get(position).paramsMap;

        // Установка картинки для плитки
		if (pMap.containsKey("GridImage")) {
			File imageFile = new File(pMap.get("GridImage"));

			if (imageFile.exists()) {
				mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
						.getAbsolutePath()));
			} else {
				FrameLayout frm = (FrameLayout) v
						.findViewById(R.id.FrameLayout1);
				frm.setVisibility(View.GONE);
			}
		} else {
			FrameLayout frm = (FrameLayout) v.findViewById(R.id.FrameLayout1);
			frm.setVisibility(View.GONE);
		}

        // Параметры для картинки плитки
		mImage.getLayoutParams().width = formatted_screens_tile_image_size;
		mImage.getLayoutParams().height = formatted_screens_tile_image_size;

		Typeface font = Typeface.createFromAsset(mContext.getAssets(),
				"russo.ttf");

        // Размер шрифта для надписи на плитке
		int formatted_screens_tile_label_size = Integer.parseInt(prefs
				.getString("formatted_screens_tile_label_size", "21"));

        // Установка надписи для плитки
		TextView mTitle = (TextView) v
				.findViewById(R.id.formscreen_adapter_title);
		mTitle.setTypeface(font);
		mTitle.setTextSize(formatted_screens_tile_label_size);
		mTitle.setTextColor(Color.parseColor("#ffffff"));
		mTitle.setText(mApplication.mFormattedScreens.mFormattedScreens
				.get(position).mName);

		v.setOnClickListener(mFormattedScreenListener(position));
		return v;
	}
}