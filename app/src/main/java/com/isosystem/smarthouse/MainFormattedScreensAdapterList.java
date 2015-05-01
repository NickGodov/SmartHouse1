package com.isosystem.smarthouse;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.isosystem.smarthouse.logging.Logging;

/**
 * Адаптер для вывода списка для окон форматированного вывода
 * При нажатии на пункт списка запускается {@link com.isosystem.smarthouse.FormattedScreensActivity}
 * которому передается номер окна форматированного вывода, который соотносится с определенным пунктом
 * списка
 */
public class MainFormattedScreensAdapterList extends BaseAdapter {

	private Context mContext;
	private MyApplication mApplication;

	public MainFormattedScreensAdapterList(Context c) {
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
     * При нажатии на пункт списка, открывается {@link com.isosystem.smarthouse.FormattedScreensActivity},
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
     * 1. Из настроек считываются параметры списка: ширина, высота и тд)
     * 2. Устанавливаются параметры пункта списка из настроек
     * 3. Считываются параметры i-ого пункта списка из MyApplication.mFormattedScreens
     * 4. Устанавливается текст пункта списка
     * 5. Устанавливается OnClickListener
     *
     * @param position позиция пункта списка соответствует окну вывода
     * @param convertView
     * @param parent
     * @return
     */
	public View getView(int position, View convertView, ViewGroup parent) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);

        //Считывание параметров из настроек

        // Высота строки списка
		int formatted_screens_list_height = Integer.parseInt(prefs.getString(
				"formatted_screens_list_height", "150"));
		
		View v = convertView;
		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(this.mContext);
			v = vi.inflate(R.layout.formscreen_adapter_list_item, null);

            // Установка высоты списка
			v.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, formatted_screens_list_height));
		}

		// Размер шрифта для надписи на строке списка
		int main_menu_list_label_size = Integer.parseInt(prefs.getString(
				"main_menu_list_label_size", "30"));

        // Установка надписи для строки списка
		Typeface font = Typeface.createFromAsset(mContext.getAssets(),
				"russo.ttf");
		TextView mTitle = (TextView) v
				.findViewById(R.id.formscreen_adapter_title);
		mTitle.setTypeface(font);
		mTitle.setTextSize(main_menu_list_label_size);
		mTitle.setTextColor(Color.parseColor("#ffffff"));
		mTitle.setText(mApplication.mFormattedScreens.mFormattedScreens
				.get(position).mName);

		v.setOnClickListener(mFormattedScreenListener(position));
		return v;
	}
}