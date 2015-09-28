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
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.data.MenuTreeNode;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;
import com.isosystem.smarthouse.utils.BooleanFormulaEvaluator;
import com.isosystem.smarthouse.utils.EvaluatorResult;
import com.isosystem.smarthouse.utils.MathematicalFormulaEvaluator;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainMenuPageSliderIntValueActivity extends Activity {

    MyApplication mApp;
    Context mContext;

    // Ресивер для получения значения от контроллера
    ValueMessageReceiver mReceiver;
    // Текущая конечная точка
    MenuTreeNode node;

    // Формула для обработки входящего значения
    String mIncomingValueFormula;
    // Формула для обработки исходящего значения
    String mOutgoingValueFormula;
    // Сообщение для запроса значения от контроллера
    String mGiveMeValueMessage;
    // Префикс для исходящего значения
    String mOutgoingValueMessage;

    // Слайдер
    SeekBar mSeekBar;

    /** Handler для хранителя экрана */
    Handler mDynamicSendHandler = new Handler();

    // Надписи для слайдера
    // Минимальное значение слайдера
    TextView mTextViewSliderMinValue;
    // Максимальное значение слайдера
    TextView mTextViewSliderMaxValue;
    // Текущее значение слайдера
    TextView mTextViewSliderValue;

    // Минимальное значение слайдера
    Float mSliderMinValue;
    // Максимальное значение слайдера
    Float mSliderMaxValue;
    // Шаг слайдера
    Float mSliderStepValue;

    // Динамический режим отсылки значений
    Boolean mDynamicMode = false;
    // Таймаут для отсылки
    int mDynamicSendTimeout;

    // Предыдущее значение слайдера
    Float mPreviousSliderValue = 0.0f;

    // Кнопка "Установить"
    Button mSendButton;

    // Диспетчер для отправки значения
    MessageDispatcher mDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_slider_int_value);

        // Запрет включения системного хранителя экрана
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

        // Получение данных текущей конечной точки
        node = (MenuTreeNode) getIntent().getSerializableExtra("Node");

        // Берем хеш-таблицу параметров узла
        HashMap<String, String> pMap = node.paramsMap;

        // Заголовок
        TextView mHeaderText = (TextView) findViewById(R.id.psv_tv_headertext);
        mHeaderText.setText(pMap.get("HeaderText"));

        // Текст описания
        TextView mDescriptionText = (TextView) findViewById(R.id.psv_tv_description_text);
        mDescriptionText.setText(pMap.get("DescriptionText"));

        // Картинка для окна
        ImageView mImage = (ImageView) findViewById(R.id.psv_image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

        // Динамический режим
        if (pMap.get("DynamicMode")!=null){
            if (pMap.get("DynamicMode").equals("0")){
                mDynamicMode = false;
            } else if (pMap.get("DynamicMode").equals("1")) {
                mDynamicMode = true;
            } else {
                Notifications.showError(mContext, "Внутренняя ошибка при попытке считать данные");
                Logging.v("Исключение при попытке парсинга данных конечной точки. Параметр: DynamicMode");
            }
        }

        // Тайм-аут отсылки
        if (pMap.get("DynamicSendTimeout")!=null) {
            if (mDynamicMode) {
                try {
                    mDynamicSendTimeout = Integer.parseInt(pMap.get("DynamicSendTimeout"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Notifications.showError(mContext, "Ошибка при считывании данных дла ползунка");
                    Logging.v("Исключение при попытке парсинга данных конечной точки. Параметр: DynamicSendTimeout");
                    MainMenuPageSliderIntValueActivity.this.finish();
                }
            }
        }

        mIncomingValueFormula = pMap.get("IncomingValueFormula");
        mOutgoingValueFormula = pMap.get("OutgoingValueFormula");
        mGiveMeValueMessage = pMap.get("GiveMeValueMessage");
        mOutgoingValueMessage = pMap.get("OutgoingValueMessage");

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        // Считывание значений слайдера
        try {
            mSliderMinValue = Float.parseFloat(pMap.get("SliderMinValue"));
            mSliderMaxValue = Float.parseFloat(pMap.get("SliderMaxValue"));
            mSliderStepValue = Float.parseFloat(pMap.get("SliderStepValue"));
        } catch (Exception e) {
            e.printStackTrace();
            Logging.v("Ошибка при считывании данных дла ползунка");
            Notifications.showError(this,"Внутренняя ошибка данных!");
            MainMenuPageSliderIntValueActivity.this.finish();
        }

        // Установка надписи для максимального значения ползунка
        mTextViewSliderMaxValue = (TextView) findViewById(R.id.seekBarMaxValue);
        mTextViewSliderMaxValue.setText(String.valueOf(mSliderMaxValue));
        // Установка надписи для минимального значения ползунка
        mTextViewSliderMinValue = (TextView) findViewById(R.id.seekBarMinValue);
        mTextViewSliderMinValue.setText(String.valueOf(mSliderMinValue));

        // Начальное значение при старте окна, оно указывает на крайнюю левую границу
        float start_value = mSliderMinValue + (mSeekBar.getProgress()*mSliderStepValue);
        // Устанавливаем стартовое значение для написи текущего значения
        mTextViewSliderValue = (TextView) findViewById(R.id.seekBarValue);
        mTextViewSliderValue.setText(String.valueOf(start_value));
        // Устанавливаем предыдущее значение равное стартовому
        mPreviousSliderValue = start_value;

        // Установка максимального значения слайдера
        // оно равно (max-min)/step
        int max_value = (int)((mSliderMaxValue - mSliderMinValue)/mSliderStepValue);
        mSeekBar.setMax(max_value);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String value = String.valueOf(mSliderMinValue + (mSeekBar.getProgress() * mSliderStepValue));
                mTextViewSliderValue.setText(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //mPreviousSliderValue = mSliderMinValue + (mSeekBar.getProgress() * mSliderStepValue);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Кнопка "Установить"
        mSendButton = (Button) findViewById(R.id.psv_send_button);
        mSendButton.setOnClickListener(sendButtonListener);
        if (mDynamicMode){
            mSendButton.setVisibility(View.GONE);
        } else {
            mSendButton.setVisibility(View.VISIBLE);
        }

        // Кнопка "Назад"
        Button mBackButton = (Button) findViewById(R.id.psv_back_button);
        mBackButton.setOnClickListener(backButtonListener);

        // Установка шрифта
        SetFont(R.id.psv_tv_headertext);
        SetFont(R.id.psv_tv_description_text);
        SetFont(R.id.seekBarValue);
        SetFont(R.id.seekBarMinValue);
        SetFont(R.id.seekBarMaxValue);
        SetFont(R.id.psv_send_button);
        SetFont(R.id.psv_back_button);

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

    private void sendValue(String value) {
        Logging.v("Изначальное значение :" + value);

        // 1. Обработка значения с помощью формулы
        MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                mOutgoingValueFormula, value, "0", true);
        EvaluatorResult evalResult = evaluator.eval();

        // Проверяем результат
        if (!evalResult.isCorrect) {
            Logging.v("Ошибка при обработке введенного значения формулой пересчета значения. Данные не были высланы");
            Notifications.showError(mContext, "Ошибка при пересчете исходящего значения. Значение введено некорректно или формула пересчета задана некорректно. Отсылка сообщений приостановлена");
        } else {
            mDispatcher.SendValueMessage(mOutgoingValueMessage,evalResult.numericRoundedResult, true);
        }
    }

    /**
     * Слушатель для кнопки отсылки значения
     */
    private OnClickListener sendButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            String value = String.valueOf(mSliderMinValue + (mSeekBar.getProgress() * mSliderStepValue));
            sendValue(value);
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
            filter.addAction(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE);
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

    @Override
    protected void onResume() {
        if (mDynamicMode) {
            mDynamicSendHandler.postDelayed(mDynamicSendRunnable,
                    mDynamicSendTimeout);
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        if (mDynamicMode) {
            if (mDynamicSendHandler != null) {
                mDynamicSendHandler.removeCallbacks(mDynamicSendRunnable);
            }
        }
        super.onPause();
    }

    private Runnable mDynamicSendRunnable = new Runnable() {
        public void run() {
            float mCurrentValue = mSliderMinValue + (mSeekBar.getProgress() * mSliderStepValue);
            if (mCurrentValue != mPreviousSliderValue) {
                sendValue(String.valueOf((mCurrentValue)));
                mPreviousSliderValue = mCurrentValue;
            }

            mDynamicSendHandler.postDelayed(mDynamicSendRunnable,
                    mDynamicSendTimeout);
        }
    };

    /**
     * Принудительное открытие окна форматированного вывода: <br />
     * 1. С помощью регулярного выражения ищем число в сообщении от контроллера; <br />
     * 2. Если число найдено, пытаемся перевести строку в INT; <br />
     * 3. Отсылаем сообщение о невозможности открыть окно форматированного вывода. <br />
     * @param message сообщение от контроллера
     */
    private void forcedFormattedScreenStart(String message){
        String number = "";
        // Считываем номер окна в сообщении
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(message);
        while (m.find()) {
            number = m.group();
        }

        // Парсим номер окна и отсылаем сообщение
        int screen_number = -1;
        try {
            screen_number = Integer.parseInt(number);
            if (screen_number >=0 && screen_number < mApp.mFormattedScreens.mFormattedScreens.size()) {
                // Номер окна корректный, отсылаем сообщение
                mDispatcher = new MessageDispatcher(this);
                mDispatcher.sendGiveMeValueMessage(mApp.mFormattedScreens.mFormattedScreens.get(screen_number).mCannotOpenWindowMessage,true);
            } else {
                // Номер окна некорректный
                Intent i = new Intent();
                String alarmMessage = "Неверное обращение к форматированному выводу";
                mApp.mAlarmMessages.addAlarmMessage(
                        mApp, alarmMessage,
                        Notifications.MessageType.ControllerMessage);
                // Кидаем броадкаст
                i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
                mApp.sendBroadcast(i);
            }
        } catch (NumberFormatException e) {
            // Сообщение пришло в неверном формате (не смогли найти число)
            Intent i = new Intent();
            String alarmMessage = "Неверное обращение к форматированному выводу";
            mApp.mAlarmMessages.addAlarmMessage(
                    mApp, alarmMessage,
                    Notifications.MessageType.ControllerMessage);
            // Кидаем броадкаст
            i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
            mApp.sendBroadcast(i);

            Logging.v("Исключение при попытке парсинга номера окна форматированного вывода." +
                    "Строка парсинга: " + number);
            e.printStackTrace();
        }
    }

    class ValueMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Если принудительное открытие окна, вызываем метод и передаем ему
            // extra в виде сообщение от контроллера
            if (intent.getAction().equals(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE)) {
                String msg = intent.getStringExtra("message");
                forcedFormattedScreenStart(msg);
                return;
            }

            // Получено сообщение. Оно должно быть обработано формулой с нужным
            // количеством знаков после запятой
            String msg = intent.getStringExtra("message");

            if (msg.length() < 3) {
                Logging.v("Неверный формат сообщения");
                return;
            }

            msg = msg.substring(2);

            // 1. Обработка значения с помощью формулы обработки входящего значения с указанием количества знаков после запятой
            MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                    mIncomingValueFormula, msg, "2", true);
            EvaluatorResult evalResult = evaluator.eval();

            // Проверяем результат
            if (!evalResult.isCorrect) {
                Notifications.showError(mContext, "Ошибка при пересчете входящего значения. Значение введено некорректно или формула пересчета задана некорректно");
            } else {

                //Устанавливаем значение для слайдера
                float incoming_value = Float.parseFloat(evalResult.numericRoundedResult);

                if (incoming_value > mSliderMaxValue) {
                    Notifications.showError(mContext,"Полученное значение от контроллера больше максимально возможного. Принудительно установлено максимально возможное значение");
                    incoming_value = mSliderMaxValue;
                } else if (incoming_value < mSliderMinValue) {
                    Notifications.showError(mContext,"Полученное значение от контроллера меньше минимально возможного. Принудительно установлено минимально возможное значение");
                    incoming_value = mSliderMinValue;
                }
                int progress_value = (int)((incoming_value - mSliderMinValue)/mSliderStepValue);
                mPreviousSliderValue = incoming_value;
                mSeekBar.setProgress(progress_value);
            }
        }
    }
}
