package com.isosystem.smarthouse;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainMessagesFragment extends Fragment {
    View rootView;
    MyApplication mApplication;
    // Ресивер для получения алармовых сообщений
    AlarmMessageReceive mReceiver;

    // Контейнер для списка сообщений
    ListView mMessagesList;
    // Кнопка назад
    Button mBackButton;
    // Адаптер для списка сообщений
    MainMessagesAdapter mAdapter;

    Globals.ConnectionMode connectionMode = Globals.ConnectionMode.USB;

    // Картинка для USB-подключения
    ImageView mUsbConnectedIcon;
    // Картинка для подключения питания
    ImageView mPowerSupplyIcon;

    // Количество сообщений
    TextView mMessagesNumber;
    // Картинка для количества сообщений
    ImageView mMessagesIcon;

    // Дефолтный фон
    Drawable mDefaultBackground;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_main_messages, container,
                false);

        mApplication = (MyApplication) rootView.getContext()
                .getApplicationContext();

        mDefaultBackground = rootView.getBackground();

        // Проверка USB-подключения
        mUsbConnectedIcon = (ImageView) rootView
                .findViewById(R.id.image_usb_connection);
        checkUsbConnectionIcon();

        // Проверка подключения по сети
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

        // Обновление списка сообщений
        mMessagesList = (ListView) rootView
                .findViewById(R.id.message_activity_msg_list);
        refreshListView();

        // Удаление алармового сообщения при нажатии на него
        mMessagesList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                mApplication.mAlarmMessages
                        .clearMessage(position, mApplication);
                refreshListView();
                setMessageNumberIcon();
            }
        });

        // Удалить все сообщения
        ImageButton mDeleteAllMessages = (ImageButton) rootView
                .findViewById(R.id.delete_messages);
        mDeleteAllMessages
                .setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        mApplication.mAlarmMessages.clearAllMessages(mApplication);
                        Notifications.showOkMessage(getActivity(), "Сообщения удалены");

                        refreshListView();
                        setMessageNumberIcon();
                        return true;
                    }
                });

        // Надпись внизу, URL
        TextView textView = (TextView) rootView.findViewById(R.id.mfl_url);
        Typeface font = Typeface.createFromAsset(rootView.getContext()
                .getAssets(), "code.otf");
        textView.setTypeface(font);
        textView.setText(this.getString(R.string.company_url));
        textView.setTextSize(15.0f);
        textView.invalidate();

        // Надпись внизу, телефон
        textView = (TextView) rootView.findViewById(R.id.mfl_phone);
        font = Typeface.createFromAsset(rootView.getContext().getAssets(),
                "code.otf");
        textView.setTypeface(font);
        textView.setText(this.getString(R.string.company_phone));
        textView.setTextSize(15.0f);
        textView.invalidate();

        // Надпись "Сообщения"
        textView = (TextView) rootView.findViewById(R.id.menuheader_text);
        font = Typeface.createFromAsset(rootView.getContext().getAssets(),
                "russo.ttf");
        textView.setTypeface(font);
        textView.setTextColor(Color.parseColor("white"));
        textView.setTextSize(35.0f);
        textView.setText(this.getString(R.string.title_alarm_messages));
        textView.setGravity(Gravity.CENTER);
        textView.invalidate();

        return rootView;
    }

    /**
     * Установка количества сообщений на верхней плашке
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

        // В зависимости от количества цифр, изменяется шрифт
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

    @Override
    public void onStart() {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // Тип подключения
        String connection_type = prefs.getString("connection_type", "1");
        if (connection_type.equals("0")) {
            connectionMode = Globals.ConnectionMode.WIFI;
        } else if (connection_type.equals("1")) {
            connectionMode = Globals.ConnectionMode.USB;
        }

        // Старт ресивера для приема алармовых сообщений
        try {
            mReceiver = new AlarmMessageReceive();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
            filter.addAction(Globals.BROADCAST_INTENT_POWER_SUPPLY_CHANGED);
            filter.addAction(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE);
            rootView.getContext().registerReceiver(mReceiver, filter);
            Logging.v("Регистрируем ресивер MainActivity");
        } catch (Exception e) {
            Logging.v("Исключение при попытке зарегистрировать ресивер");
            e.printStackTrace();
        }

        // Проверка питания
        checkPowerSupplyIcon();
        // Проверка USB-соединения
        checkUsbConnectionIcon();
        // Проверка количества сообщений
        setMessageNumberIcon();

        // Обновление списка сообщенияй
        refreshListView();


        // Если в настройках выставлено использование своего фона
        if (!prefs.getBoolean("use_default_main_messages_background", true)) {
            String filepath = prefs.getString(
                    "choose_main_messages_background", "no-image");
            // Если установлен путь к изображению
            if (!filepath.equals("no-image")) {
                BitmapDrawable navigationBackground = new BitmapDrawable(
                        filepath);
                // Если фон плиткой
                if (prefs.getBoolean("main_messages_background_tile", true)) {
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

    /**
     * Изменение картинки питания
     */
    private void checkPowerSupplyIcon() {
        if (isSupplyEnabled()) {
            mPowerSupplyIcon.setImageResource(R.drawable.tablet_power_on);
        } else {
            mPowerSupplyIcon.setImageResource(R.drawable.tablet_power_off);
        }
    }

    /**
     * Проверка USB-соединения
     */
    private void checkUsbConnectionIcon() {
        if (mApplication.isUsbConnected) {
            if (connectionMode == Globals.ConnectionMode.USB) {
                mUsbConnectedIcon.setImageResource(R.drawable.tablet_connection_on);
            } else if (connectionMode == Globals.ConnectionMode.WIFI) {
                mUsbConnectedIcon.setImageResource(R.drawable.tablet_wifi_on);
            }
        } else {
            if (connectionMode == Globals.ConnectionMode.USB) {
                mUsbConnectedIcon.setImageResource(R.drawable.tablet_connection_off);
            } else if (connectionMode == Globals.ConnectionMode.WIFI) {
                mUsbConnectedIcon.setImageResource(R.drawable.tablet_wifi_off);
            }
        }
    }

    /**
     * Проверка питания
     *
     * @return подсоединено ли питание
     */
    private Boolean isSupplyEnabled() {
        Intent intent = mApplication.registerReceiver(null, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        int plugged = 0;
        plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean result = (plugged != 0 && plugged != -1);
        return result;
    }

    @Override
    public void onStop() {
        // Остановка ресивера для приема алармовых сообщений
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
     * Подключение адаптера
     */
    private void refreshListView() {
        MainMessagesAdapter adapter = new MainMessagesAdapter(
                rootView.getContext(),
                mApplication.mAlarmMessages.mAlarmMessages);
        mMessagesList.setAdapter(adapter);
    }

    /**
     * Принудительное открытие окна форматированного вывода: <br />
     * 1. С помощью регулярного выражения ищем число в сообщении от контроллера; <br />
     * 2. Если число найдено, пытаемся перевести строку в INT; <br />
     * 3. Открываем нужное окно форматированного вывода. <br />
     *
     * @param message сообщение от контроллера
     */
    private void forcedFormattedScreenStart(String message) {
        String number = "";

        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(message);
        while (m.find()) {
            number = m.group();
        }

        int screen_number = -1;
        try {
            screen_number = Integer.parseInt(number);
            if (screen_number >= 0 && screen_number < mApplication.mFormattedScreens.mFormattedScreens.size()) {
                Intent intent = new Intent(getActivity(),
                        FormattedScreensActivity.class);
                // Передаем номер нажатого окна в FormatterScreenActivity
                intent.putExtra("formScreenIndex", screen_number);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.flipin, R.anim.flipout);
            } else {
                //throw new NumberFormatException("format screen number is out of bounds");
            }
        } catch (NumberFormatException e) {
            Intent i = new Intent();
            String alarmMessage = "Неверное обращение к форматированному выводу";
            mApplication.mAlarmMessages.addAlarmMessage(
                    mApplication, alarmMessage,
                    Notifications.MessageType.ControllerMessage);
            // Кидаем броадкаст
            i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
            mApplication.sendBroadcast(i);

            Logging.v("Исключение при попытке парсинга номера окна форматированного вывода." +
                    "Строка парсинга: " + number);
            e.printStackTrace();
        }
    }

    // Если пришло алармовое сообщение - обновление
    // списка с сообщениями
    class AlarmMessageReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Если статус питания изменился - проверка питания
            if (intent.getAction().equals(Globals.BROADCAST_INTENT_POWER_SUPPLY_CHANGED))
                checkPowerSupplyIcon();

            checkUsbConnectionIcon();
            setMessageNumberIcon();
            refreshListView();

            // Если принудительное открытие окна, вызываем метод и передаем ему
            // extra в виде сообщение от контроллера
            if (intent.getAction().equals("SMARTHOUSE.FORCED_FORMSCREEN_MESSAGE_RECEIVED")) {
                String msg = intent.getStringExtra("message");
                //forcedFormattedScreenStart(msg);
            }
        }
    } // end of class
}