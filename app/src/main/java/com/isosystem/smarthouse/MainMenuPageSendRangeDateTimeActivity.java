package com.isosystem.smarthouse;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.data.MenuTree;
import com.isosystem.smarthouse.data.MenuTreeNode;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;
import com.isosystem.smarthouse.utils.BooleanFormulaEvaluator;
import com.isosystem.smarthouse.utils.EvaluatorResult;
import com.isosystem.smarthouse.utils.MathematicalFormulaEvaluator;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.SimpleTimeZone;

public class MainMenuPageSendRangeDateTimeActivity extends Activity {

    MyApplication mApp;
    Context mContext;

    /** Ресивер для получения значения от контроллера */
    ValueMessageReceiver mReceiver;

    /** Текущая конечная точка */
    MenuTreeNode node;

    /** Текст ошибки при вводе некорректного диапазона дат/времени-дат */
    String mDateTimeRangeErrorText = "Ошибка диапазона дат/дат-времени. Первое значение должно быть раньше";

    /** Надпись вместо 'первое значение' */
    TextView mFirstValueLabel;

    /** Надпись вместо 'второе значение' */
    TextView mSecondValueLabel;

    /** Сообщения для получения данных от контроллера */
    String mGiveMeValueMessage;

    /** Сообщение для отправки значений контроллеру */
    String mOutgoingValueMessage;

    /** Диспетчер для отправки сообщения */
    MessageDispatcher mDispatcher;

    /** Время для первого значения */
    TimePicker mFirstTimePicker;

    /** Время для второго значения */
    TimePicker mSecondTimePicker;

    /** Дата для первого значения */
    DatePicker mFirstDatePicker;

    /** Дата для второго значения */
    DatePicker mSecondDatePicker;

    MenuTree.DateTimeRangeType mRangeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_send_date_time_range_value);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Включение полноэкранного режим планшета
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setSystemUiVisibility(8);
        // <<-----------------------------------

        mContext = this;
        mApp = (MyApplication) getApplicationContext();

        // Получение текущей конечной точки
        node = (MenuTreeNode) getIntent().getSerializableExtra("Node");

        // Берем хеш-таблицу параметров узла
        HashMap<String, String> pMap = node.paramsMap;

        //Тип окна
        try {
            int type_number = -1;
            type_number = Integer.parseInt(pMap.get("DateTimeRangeType"));
            mRangeType = MenuTree.DateTimeRangeType.values()[type_number];
        } catch (EnumConstantNotPresentException e) {
            e.printStackTrace();
            Notifications.showError(mContext, "Произошла внутрення ошибка - не удалось считать тип диапазона." +
                    "Обратитесь в службу поддержки");
            Logging.v("Не удалось считать тип диапазона");
            ((Activity) mContext).finish();
        }

        // Установка пикеров времени и дат
        setPickers();

        // Заголовок
        TextView mHeaderText = (TextView) findViewById(R.id.header);
        mHeaderText.setText(pMap.get("HeaderText"));

        // Текст описания
        TextView mDescriptionText = (TextView) findViewById(R.id.description);
        mDescriptionText.setText(pMap.get("DescriptionText"));

        // Надписи 'первое значение' и 'второе значение'
        mFirstValueLabel = (TextView) findViewById(R.id.first_value_label);
        mSecondValueLabel = (TextView) findViewById(R.id.second_value_label);

        // Надпись вместо 'Первое значение'
        String first_value_label = pMap.get("FirstValueLabel");
        if (!TextUtils.isEmpty(first_value_label.trim())) {
            mFirstValueLabel.setText(first_value_label);
        }

        // Надпись вместо 'Второе значение'
        String second_value_label = pMap.get("SecondValueLabel");
        if (!TextUtils.isEmpty(second_value_label.trim())) {
            mSecondValueLabel.setText(second_value_label);
        }

        // Сообщение о некорректном диапазоне дат/дат-времени
        String datetime_range_error_text = pMap.get("DateTimeErrorText");
        if (!TextUtils.isEmpty(datetime_range_error_text.trim())) {
            mDateTimeRangeErrorText = datetime_range_error_text;
        }

        // Картинка для окна
        ImageView mImage = (ImageView) findViewById(R.id.image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

        mGiveMeValueMessage = pMap.get("GiveMeValueMessage");
        mOutgoingValueMessage = pMap.get("OutgoingValueMessage");

        // Кнопка "Установить"
        Button mSendButton = (Button) findViewById(R.id.send_button);
        mSendButton.setOnClickListener(sendButtonListener);

        // Кнопка "Назад"
        Button mBackButton = (Button) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(backButtonListener);

        // Установка шрифта
        SetFont(R.id.header);
        SetFont(R.id.description);
        SetFont(R.id.first_value_label);
        SetFont(R.id.second_value_label);

        // Создаем объект диспетчера
        mDispatcher = new MessageDispatcher(this);
        mDispatcher.sendGiveMeValueMessage(mGiveMeValueMessage,true);
    }

    /**
     * Устанавливаем красивый шрифт. В качестве входного параметра - id элемента
     * из R.java
     */
    private void SetFont(int id) {
        Typeface font = Typeface.createFromAsset(getAssets(), "myfont.ttf");
        TextView et = (TextView) findViewById(id);
        et.setTypeface(font);
        et.invalidate();
    }

    /**
     * Установка пикеров времени и дат.
     * В зависимости от {@link com.isosystem.smarthouse.data.MenuTree.DateTimeRangeType}
     * необходимо показать или скрыть отдельные элементы.
     * Если диапазон времени - скрываем элементы для установки даты
     * Если диапазон дат - скрываем элементы для установки времени
     * Если диапазон дат и времени - показываем всё
     */
    private void setPickers() {
        mFirstTimePicker = (TimePicker) findViewById(R.id.first_time_picker);
        mSecondTimePicker = (TimePicker) findViewById(R.id.second_time_picker);
        mFirstDatePicker = (DatePicker) findViewById(R.id.first_date_picker);
        mSecondDatePicker = (DatePicker) findViewById(R.id.second_date_picker);

        if (mRangeType == MenuTree.DateTimeRangeType.DateRange) {
            mFirstTimePicker.setVisibility(View.GONE);
            mSecondTimePicker.setVisibility(View.GONE);
        } else if (mRangeType == MenuTree.DateTimeRangeType.TimeRange) {
            mFirstDatePicker.setVisibility(View.GONE);
            mSecondDatePicker.setVisibility(View.GONE);
        }

        mFirstTimePicker.setIs24HourView(true);
        mFirstTimePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
        mSecondTimePicker.setIs24HourView(true);
        mSecondTimePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        mFirstDatePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        mSecondDatePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
    }

    /**
     * Слушатель для кнопки отсылки значения Данный метод реализует бОльшую
     * часть функционала окна. Необходимо: 1. Обработать значение с помощью
     * формулы 2. Провести валидацию обработанного значения 3. Выслать значение
     * на контроллер
     */
    private OnClickListener sendButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {

            if (!checkDateTimeRange()) {
                Notifications.showError(mContext,mDateTimeRangeErrorText);
                return;
            }

            if (mRangeType == MenuTree.DateTimeRangeType.TimeRange || mRangeType == MenuTree.DateTimeRangeType.DateTimeRange) {
                int first_minute = -1;
                int second_minute = -1;
                int first_hour = -1;
                int second_hour = -1;

                first_minute  = mFirstTimePicker.getCurrentMinute();
                second_minute = mSecondTimePicker.getCurrentMinute();
                first_hour    = mFirstTimePicker.getCurrentHour();
                second_hour   = mSecondTimePicker.getCurrentHour();

                if (first_minute > 59 || first_minute < 0){
                    Notifications.showError(mContext,"Количество минут для первого значения времени введено некорректно");
                    return;
                } else if (second_minute > 59 || second_minute < 0) {
                    Notifications.showError(mContext,"Количество минут для второго значения времени введено некорректно");
                    return;
                } else if (first_hour > 23 || first_hour < 0) {
                    Notifications.showError(mContext,"Количество часов для первого значения времени введено некорректно");
                    return;
                } else if (second_hour > 23 || second_hour < 0){
                    Notifications.showError(mContext,"Количество часов для второго значения времени введено некорректно");
                    return;
                }
            }

            if (mRangeType == MenuTree.DateTimeRangeType.DateRange || mRangeType == MenuTree.DateTimeRangeType.DateTimeRange) {
                int first_day = -1;
                int first_month = -1;
                int first_year = -1;

                int second_day = -1;
                int second_month = -1;
                int second_year = -1;

                first_day = mFirstDatePicker.getDayOfMonth();
                first_month = mFirstDatePicker.getMonth() + 1;
                first_year = mFirstDatePicker.getYear();

                second_day = mSecondDatePicker.getDayOfMonth();
                second_month = mSecondDatePicker.getMonth() + 1;
                second_year = mSecondDatePicker.getYear();

                if (first_day > 31 || first_day < 1) {
                    Notifications.showError(mContext,"Значение дня для первой даты введено некорректно");
                    return;
                } else if (first_month > 12 || first_month < 1) {
                    Notifications.showError(mContext,"Значение месяца для первой даты введено некорректно");
                    return;
                } else if (first_year < 1900 || first_year > 2999) {
                    Notifications.showError(mContext,"Значение года для первой даты введено некорректно");
                    return;
                } else if (second_day > 31 || second_day < 1) {
                    Notifications.showError(mContext,"Значение дня для второй даты введено некорректно");
                    return;
                } else if (second_month > 12 || second_month < 1) {
                    Notifications.showError(mContext,"Значение месяца для второй даты введено некорректно");
                    return;
                } else if (second_year < 1900 || second_year > 2999) {
                    Notifications.showError(mContext,"Значение года для второй даты введено некорректно");
                    return;
                }
            }

            if (mRangeType == MenuTree.DateTimeRangeType.TimeRange) {
                mDispatcher.sendTimeRangeMessage(mOutgoingValueMessage,
                        mFirstTimePicker.getCurrentHour(),
                        mFirstTimePicker.getCurrentMinute(),
                        mSecondTimePicker.getCurrentHour(),
                        mSecondTimePicker.getCurrentMinute(), true);
            } else if (mRangeType == MenuTree.DateTimeRangeType.DateRange) {
               mDispatcher.sendDateRangeMessage(mOutgoingValueMessage,
                       mFirstDatePicker.getDayOfMonth(),
                       mFirstDatePicker.getMonth() + 1,
                       mFirstDatePicker.getYear(),
                       mSecondDatePicker.getDayOfMonth(),
                       mSecondDatePicker.getMonth() + 1,
                       mSecondDatePicker.getYear(), true);
            } else if (mRangeType == MenuTree.DateTimeRangeType.DateTimeRange) {
                mDispatcher.sendDateTimeRangeMessage(mOutgoingValueMessage,
                        mFirstTimePicker.getCurrentHour(),
                        mFirstTimePicker.getCurrentMinute(),
                        mFirstDatePicker.getDayOfMonth(),
                        mFirstDatePicker.getMonth() + 1,
                        mFirstDatePicker.getYear(),
                        mSecondTimePicker.getCurrentHour(),
                        mSecondTimePicker.getCurrentMinute(),
                        mSecondDatePicker.getDayOfMonth(),
                        mSecondDatePicker.getMonth() + 1,
                        mSecondDatePicker.getYear(),true);
            } else {
                Notifications.showError(mContext,"Произошла внутренняя ошибка (тип диапазона считан некорректно). Обратитесь в службу поддержки.");
                return;
            }

            ((Activity) mContext).finish();
        }
    };

    /**
     * Слушатель для кнопки "Назад"
     */
    private OnClickListener backButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            finish();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        try {
            // Создаем и подключаем броадкаст ресивер
            mReceiver = new ValueMessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Globals.BROADCAST_INTENT_VALUE_MESSAGE);
            registerReceiver(mReceiver, filter);
            Logging.v("Регистрируем ресивер Page");
        } catch (Exception e) {
            Logging.v("Исключение при попытке зарегистрировать ресивер");
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onStop() {
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
                Logging.v("Освобождаем ресивер Page");
            } catch (Exception e) {
                Logging.v("Исключение при попытке освободить ресивер");
                e.printStackTrace();
                finish();
            }
        }
        super.onStop();
    }

    /**
     * Метод осуществляет проверку диапазона дат/времени-дат
     * Первое значение должно быть раньше по времени, чем второе
     *
     * @return результат проверки. True если проверка прошла успешно
     */
    private Boolean checkDateTimeRange(){
        if (mRangeType == MenuTree.DateTimeRangeType.TimeRange) {
            // Если диапазон времени, то не сравниваем
            return true;
        } else if (mRangeType == MenuTree.DateTimeRangeType.DateRange) {
            // Если диапазон дат, то сравниваеи только дата
            return checkDateRange(false);
        } else if (mRangeType == MenuTree.DateTimeRangeType.DateTimeRange) {
            return checkDateRange(true);
        }
        return false;
    }

    private Boolean checkDateRange(Boolean checkTime){
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = sdf.parse(mFirstDatePicker.getYear() + "-" +
                    mFirstDatePicker.getMonth() + "-" +
                    mFirstDatePicker.getDayOfMonth());
            Date date2 = sdf.parse(mSecondDatePicker.getYear() + "-" +
                    mSecondDatePicker.getMonth() + "-" +
                    mSecondDatePicker.getDayOfMonth());

            // Если даты одинаковы и checkTime = true, сравниваем даты
            if (date1.equals(date2)) {
                return checkTime ? checkTimeRange() : true;
            }
            else if (date1.before(date2)) {
                return true;
            } else return false;
        } catch (ParseException e) {
            Logging.v("Исключение при попытке парсинга и сравнения двух дат из datepicker`ов");
            e.printStackTrace();
            Notifications.showError(mContext,"Внутренняя ошибка при попытке сравнить даты");
        }
        return false;
    }

    private Boolean checkTimeRange(){
        SimpleDateFormat sdf= new SimpleDateFormat("HH:mm");
        try {
            Date time1 = sdf.parse(mFirstTimePicker.getCurrentHour() + ":" +
                    mFirstTimePicker.getCurrentMinute());
            Date time2 = sdf.parse(mSecondTimePicker.getCurrentHour() + ":" +
                    mSecondTimePicker.getCurrentMinute());

            // Если даты одинаковы и checkTime = true, сравниваем даты
            if (time1.equals(time2) || time1.before(time2)) {
                return true;
            } else return false;
        } catch (ParseException e) {
            Logging.v("Исключение при попытке парсинга и сравнения двух показаний времени из datepicker`ов");
            e.printStackTrace();
            Notifications.showError(mContext,"Внутренняя ошибка при попытке сравнить даты");
        }
        return false;
    }

    class ValueMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Получено сообщение. Оно должно быть обработано формулой с нужным
            // количеством знаков после запятой
            String msg = intent.getStringExtra("message");

            if (msg.length() < 3) {
                Logging.v("Неверный формат сообщения");
                return;
            }
            msg = msg.substring(2);

            String first_value;
            String second_value;

            // Парсим сообщение, выделяя первое значение (до знака '-') и второе значение
            try {
                String[] parts = msg.split("-");
                first_value = parts[0];
                second_value = parts[1];
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.showError(mContext,"Ошибка при попытке обработать сообщение от контроллера");
                Logging.v("Исключение при попытке парсинга сообщения от контроллера. Сообщение от контроллера: " + msg);
                return;
            }

            if (mRangeType == MenuTree.DateTimeRangeType.TimeRange) {
                int first_hour;
                int first_minute;
                int second_hour;
                int second_minute;

                // Парсим часы и минуты
                try {
                    first_hour = Integer.parseInt(first_value.split(":")[0]);
                    first_minute = Integer.parseInt(first_value.split(":")[1]);
                    second_hour = Integer.parseInt(second_value.split(":")[0]);
                    second_minute = Integer.parseInt(second_value.split(":")[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Notifications.showError(mContext, "Ошибка при получении показания времени от контроллера");
                    Logging.v("Исключение при попытке считать время из сообщения контроллера");
                    return;
                }

                // Проверка часов и минут
                if (first_minute > 59 || first_minute < 0){
                    Notifications.showError(mContext,"Ошибка при получении показания времени от контроллера");
                    return;
                } else if (second_minute > 59 || second_minute < 0) {
                    Notifications.showError(mContext,"Ошибка при получении показания времени от контроллера");
                    return;
                } else if (first_hour > 23 || first_hour < 0) {
                    Notifications.showError(mContext,"Ошибка при получении показания времени от контроллера");
                    return;
                } else if (second_hour > 23 || second_hour < 0){
                    Notifications.showError(mContext,"Ошибка при получении показания времени от контроллера");
                    return;
                }

                // Установка времени в контролах
                mFirstTimePicker.setCurrentHour(first_hour);
                mFirstTimePicker.setCurrentMinute(first_minute);
                mSecondTimePicker.setCurrentHour(second_hour);
                mSecondTimePicker.setCurrentMinute(second_minute);
            } else if (mRangeType == MenuTree.DateTimeRangeType.DateRange) {

                int first_day;
                int first_month;
                int first_year;
                int second_day;
                int second_month;
                int second_year;

                // Парсим даты
                try {
                    first_day = Integer.parseInt(first_value.split("/", 3)[0]);
                    first_month = Integer.parseInt(first_value.split("/", 3)[1]);
                    first_year = Integer.parseInt(first_value.split("/", 3)[2]);

                    second_day = Integer.parseInt(second_value.split("/", 3)[0]);
                    second_month = Integer.parseInt(second_value.split("/", 3)[1]);
                    second_year = Integer.parseInt(second_value.split("/", 3)[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Notifications.showError(mContext, "Ошибка при получении показания даты от контроллера");
                    Logging.v("Исключение при попытке считать дату из сообщения контроллера");
                    return;
                }

                if (first_day > 31 || first_day < 1) {
                    Notifications.showError(mContext,"Значение дня для первой даты введено некорректно");
                    return;
                } else if (first_month > 12 || first_month < 1) {
                    Notifications.showError(mContext,"Значение месяца для первой даты введено некорректно");
                    return;
                } else if (first_year < 1900 || first_year > 2999) {
                    Notifications.showError(mContext,"Значение года для первой даты введено некорректно");
                    return;
                } else if (second_day > 31 || second_day < 1) {
                    Notifications.showError(mContext,"Значение дня для второй даты введено некорректно");
                    return;
                } else if (second_month > 12 || second_month < 1) {
                    Notifications.showError(mContext,"Значение месяца для второй даты введено некорректно");
                    return;
                } else if (second_year < 1900 || second_year > 2999) {
                    Notifications.showError(mContext,"Значение года для второй даты введено некорректно");
                    return;
                }

                mFirstDatePicker.updateDate(first_year,first_month-1,first_day);
                mSecondDatePicker.updateDate(second_year,second_month-1,second_day);


            } else if (mRangeType == MenuTree.DateTimeRangeType.DateTimeRange) {

                String first_date;
                String first_time;
                String second_date;
                String second_time;

                first_date = first_value.split(" ")[0];
                first_time = first_value.split(" ")[1];

                second_date = second_value.split(" ")[0];
                second_time = second_value.split(" ")[1];

                int first_hour;
                int first_minute;
                int second_hour;
                int second_minute;

                int first_day;
                int first_month;
                int first_year;
                int second_day;
                int second_month;
                int second_year;

                // Парсим дату и время
                try {
                    first_hour = Integer.parseInt(first_time.split(":")[0]);
                    first_minute = Integer.parseInt(first_time.split(":")[1]);
                    second_hour = Integer.parseInt(second_time.split(":")[0]);
                    second_minute = Integer.parseInt(second_time.split(":")[1]);

                    first_day = Integer.parseInt(first_date.split("/", 3)[0]);
                    first_month = Integer.parseInt(first_date.split("/", 3)[1]);
                    first_year = Integer.parseInt(first_date.split("/", 3)[2]);

                    second_day = Integer.parseInt(second_date.split("/", 3)[0]);
                    second_month = Integer.parseInt(second_date.split("/", 3)[1]);
                    second_year = Integer.parseInt(second_date.split("/", 3)[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Notifications.showError(mContext, "Ошибка при получении показания даты от контроллера");
                    Logging.v("Исключение при попытке считать дату из сообщения контроллера");
                    return;
                }

                // Проверка часов и минут
                if (first_minute > 59 || first_minute < 0){
                    Notifications.showError(mContext,"Ошибка при получении показания времени от контроллера");
                    return;
                } else if (second_minute > 59 || second_minute < 0) {
                    Notifications.showError(mContext,"Ошибка при получении показания времени от контроллера");
                    return;
                } else if (first_hour > 23 || first_hour < 0) {
                    Notifications.showError(mContext,"Ошибка при получении показания времени от контроллера");
                    return;
                } else if (second_hour > 23 || second_hour < 0){
                    Notifications.showError(mContext,"Ошибка при получении показания времени от контроллера");
                    return;
                } else if (first_day > 31 || first_day < 1) {
                    Notifications.showError(mContext,"Значение дня для первой даты введено некорректно");
                    return;
                } else if (first_month > 12 || first_month < 1) {
                    Notifications.showError(mContext,"Значение месяца для первой даты введено некорректно");
                    return;
                } else if (first_year < 1900 || first_year > 2999) {
                    Notifications.showError(mContext,"Значение года для первой даты введено некорректно");
                    return;
                } else if (second_day > 31 || second_day < 1) {
                    Notifications.showError(mContext,"Значение дня для второй даты введено некорректно");
                    return;
                } else if (second_month > 12 || second_month < 1) {
                    Notifications.showError(mContext,"Значение месяца для второй даты введено некорректно");
                    return;
                } else if (second_year < 1900 || second_year > 2999) {
                    Notifications.showError(mContext,"Значение года для второй даты введено некорректно");
                    return;
                }

                mFirstDatePicker.updateDate(first_year,first_month-1,first_day);
                mSecondDatePicker.updateDate(second_year,second_month-1,second_day);

                mFirstTimePicker.setCurrentHour(first_hour);
                mFirstTimePicker.setCurrentMinute(first_minute);
                mSecondTimePicker.setCurrentHour(second_hour);
                mSecondTimePicker.setCurrentMinute(second_minute);
            } else {
                Notifications.showError(mContext,"Произошла внутренняя ошибка (тип диапазона считан некорректно). Обратитесь в службу поддержки.");
                return;
            }
        }
    }
}