package com.isosystem.smarthouse;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.isosystem.smarthouse.logging.Logging;

// Фрагмент для окок форматированного вывода

public class MainFormattedScreensFragment extends Fragment {

    public MainFormattedScreensFragment() {
    }

    // Контейнер, если было выбрано отображение списком
    static ListView mMenuListView;
    // Контейнер, если было выбрано отображение плитками
    static GridView mMenuGridView;

    static View rootView;
    MyApplication mApplication;

    // Для алармовых сообщений
    AlarmMessageReceiver mReceiver;

    // Контейнер для иконки подключения по USB
    ImageView mUsbConnectedIcon;
    // Контейнер для иконки питания
    ImageView mPowerSupplyIcon;

    // Количество сообщений
    TextView mMessagesNumber;
    // Иконка для сообщений
    ImageView mMessagesIcon;

    // Фоновое изображение
    Drawable mDefaultBackground;
    // Используется ли вывод окон форматированного вывода
    // плитками или списком
    static Boolean mTileMode = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mApplication = (MyApplication) container.getContext()
                .getApplicationContext();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // Считывание режима отображения окон (список, плитки)
        mTileMode = prefs.getBoolean("use_formatted_screens_tile", true);
        if (mTileMode) {
            rootView = inflater.inflate(R.layout.fragment_main_formscreens,
                    container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_main_formscreens_list,
                    container, false);
        }

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
        mMessagesNumber.setTypeface(Typeface.createFromAsset(rootView.getContext().getAssets(),
                "fonto.ttf"));
        mMessagesNumber.setTextColor(Color.BLACK);

        // Настройка иконки и надписи
        setMessageNumberIcon();

        // Надпись "Сообщения"
        TextView textView = (TextView) rootView.findViewById(R.id.menuheader_text);
        Typeface font = Typeface.createFromAsset(rootView.getContext().getAssets(),
                "russo.ttf");
        textView.setTypeface(font);
        textView.setTextColor(Color.parseColor("white"));
        textView.setTextSize(35.0f);
        textView.setText("Информационные окна");
        textView.setGravity(Gravity.CENTER);
        textView.invalidate();

        mDefaultBackground = rootView.getBackground();

        if (mTileMode) {
            mMenuGridView = (GridView) rootView.findViewById(R.id.formscreenlist);
        } else {
            mMenuListView = (ListView) rootView.findViewById(R.id.formscreenlist);
        }

        // Обновить меню окон вывода
        reloadListViewMenu();

        // Надпись внизу, URL
        textView = (TextView) rootView.findViewById(R.id.mfl_url);
        font = Typeface.createFromAsset(rootView.getContext()
                .getAssets(), "code.otf");
        textView.setTypeface(font);
        textView.setText(this.getString(R.string.company_url));
        textView.setTextSize(15.0f);
        textView.invalidate();

        // Надпись внизу, телефон
        textView = (TextView) rootView.findViewById(R.id.mfl_phone);
        font = Typeface.createFromAsset(rootView.getContext()
                .getAssets(), "code.otf");
        textView.setTypeface(font);
        textView.setText(this.getString(R.string.company_phone));
        textView.setTextSize(15.0f);
        textView.invalidate();

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

        // В зависимости от режима отображение (плитки/список)
        // происходит настройка GridView/ListView
        if (mTileMode) {
            configureGridView();
        } else {
            configureListView();
        }

        // Проверка состояния питания
        checkPowerSupplyIcon();
        // Проверка состояния USB-подключения
        checkUsbConnectionIcon();
        // Проверка количества сообщений
        setMessageNumberIcon();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // Если в настройках выставлено использование кастомного фона
        if (!prefs.getBoolean("use_default_main_fscreens_background", true)) {
            String filepath = prefs.getString("choose_main_fscreens_background", "no-image");
            // Если установлен путь к изображению
            if (!filepath.equals("no-image")) {
                BitmapDrawable navigationBackground = new BitmapDrawable(filepath);
                // Если фон плиткой
                if (prefs.getBoolean("main_fscreens_background_tile", true)) {
                    navigationBackground.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                }
                rootView.setBackgroundDrawable(navigationBackground);
            }
        } else {
            rootView.setBackgroundDrawable(mDefaultBackground);
        }
        super.onStart();
    }

    /**
     * Настройка вывода GridView для плиток
     */
    private void configureGridView() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // Количество колонок в ряду
        int num_columns = Integer.parseInt(prefs.getString("formatted_screens_tiles_in_row", "5"));
        mMenuGridView.setNumColumns(num_columns);

        // Режим растягивания
        int stretch_mode = Integer.parseInt(prefs.getString("formatted_screens_tile_stretch_mode", "3"));
        mMenuGridView.setStretchMode(stretch_mode);

        // Горизонтальный зазор между плитками
        int horizontal_spacing = Integer.parseInt(prefs.getString("formatted_screens_tile_horizontal_spacing", "1"));
        mMenuGridView.setHorizontalSpacing(horizontal_spacing);

        // Если указан квадратный зазор, то выставляем одинаковый горизонтальный и вертикальный зазоры
        // иначе, берем значение из настроек
        boolean square_spacing = prefs.getBoolean("formatted_screens_tile_align_vertical_spacing", true);
        if (square_spacing) {
            mMenuGridView.setVerticalSpacing(horizontal_spacing);
        } else {
            int vertical_spacing = Integer.parseInt(prefs.getString("formatted_screens_tile_vertical_spacing", "1"));
            mMenuGridView.setVerticalSpacing(vertical_spacing);
        }
    }

    /**
     * Настройка вывода ListView для списка
     */
    private void configureListView() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // Количество колонок в ряду
        int divider_height = Integer.parseInt(prefs.getString("formatted_screens_list_divider_height", "5"));
        boolean transparent_divider = prefs.getBoolean("formatted_screens_list_divider_transparent", true);

        if (transparent_divider) {
            ColorDrawable transparent = new ColorDrawable(android.R.color.transparent);
            mMenuListView.setDivider(transparent);
        } else {
            ColorDrawable div_color = new ColorDrawable(Color.LTGRAY);
            mMenuListView.setDivider(div_color);
        }

        mMenuListView.setDividerHeight(divider_height);

        int margins = Integer.parseInt(prefs.getString("formatted_screens_list_margins", "10"));

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mMenuListView
                .getLayoutParams();

        mlp.setMargins(margins, 5, margins, 5);

        mMenuListView.setLayoutParams(mlp);
    }

    /**
     * В зависимости от режимов отображения меню, подключение адаптеров
     */
    public static void reloadListViewMenu() {
        if (mTileMode) {
            mMenuGridView
                    .setAdapter(new MainFormattedScreensAdapterGrid(rootView.getContext()));
        } else {
            mMenuListView
                    .setAdapter(new MainFormattedScreensAdapterList(rootView.getContext()));
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
     * Проверка состояния питания и установка соответствующей картинки
     */
    private void checkPowerSupplyIcon() {
        if (isSupplyEnabled()) {
            mPowerSupplyIcon.setImageResource(R.drawable.tablet_power_on);
        } else {
            mPowerSupplyIcon.setImageResource(R.drawable.tablet_power_off);
        }
    }
    /**
     * Проверка состояния USB-подключения и установка соответствующей картинки
     */
    private void checkUsbConnectionIcon() {
        if (mApplication.isUsbConnected) {
            mUsbConnectedIcon.setImageResource(R.drawable.tablet_connection_on);
        } else {
            mUsbConnectedIcon
                    .setImageResource(R.drawable.tablet_connection_off);
        }
    }

    /**
     * Проверка, подключено ли питание к планшету
     *
     * @return подключено ли питание
     */
    private Boolean isSupplyEnabled() {
        Intent intent = mApplication.registerReceiver(null, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        int plugged = 0;
        plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean result = (plugged != 0 && plugged != -1);
        return result;
    }

    /**
     * Установка иконки количества сообщения
     */
    private void setMessageNumberIcon() {

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

        // Меняем размер шрифта в зависимости от количества цифр
        // чтобы количество сообщений не вылазило за рамки
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

    class AlarmMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SMARTHOUSE.POWER_SUPPLY_CHANGED"))
                checkPowerSupplyIcon();
            checkUsbConnectionIcon();
            setMessageNumberIcon();
        }
    }
}