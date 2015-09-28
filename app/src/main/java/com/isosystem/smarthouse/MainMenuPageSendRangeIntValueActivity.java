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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.data.MenuTreeNode;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;
import com.isosystem.smarthouse.utils.BooleanFormulaEvaluator;
import com.isosystem.smarthouse.utils.EvaluatorResult;
import com.isosystem.smarthouse.utils.MathematicalFormulaEvaluator;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Этот класс окна отсылки диапазона числовых значений.
 * При инициализации объекта класса, из extras берется
 * конечная точка {@link MenuTreeNode}, после чего считываются его параметры.
 * Также устанавливаются шрифты для TextView и EditText
 *
 * При старте окна запускается броадкаст ресивер {@link com.isosystem.smarthouse.MainMenuPageSendRangeIntValueActivity.ValueMessageReceiver}
 * который слушает сообщения от {@link com.isosystem.smarthouse.connection.USBReceiveService} с данными от контроллера.
 *
 * Когда приходит сообщения, из него удаляются первые два символа ("&,"), после чего
 * выполняется ф-ция String.split, которая позволяет получить значения до символа '-' и после.
 * Эти значения показываются на экране
 *
 * Когда пользователь вводит значения и нажимает кнопку "Установить",
 * сначала проверяется первое значение (обрабатывается формулой и происходит валидация булевой
 * формулой), после чего также проверяется второе значение. Если оба значения прошли валидацию,
 * с помощью {@link MessageDispatcher} оно отправляется контроллеру.
 *
 * @author Годовиченко Николай (nick.godov@gmail.com)
 * @see com.isosystem.smarthouse.MainMenuPageSendRangeIntValueActivity.ValueMessageReceiver
 * @see com.isosystem.smarthouse.connection.USBReceiveService
 * @see MenuTreeNode
 * @see MessageDispatcher
 */
public class MainMenuPageSendRangeIntValueActivity extends Activity {

    MyApplication mApp;
    Context mContext;

    /** Ресивер для получения значения от контроллера */
    ValueMessageReceiver mReceiver;

    /** Текущая конечная точка */
    MenuTreeNode node;

    /** Надпись для первого входящего значения */
    TextView mFirstIncomingValue;

    /** Надпись для второго входящего значения */
    TextView mSecondIncomingValue;

    /** Поле для первого исходящего значения */
    EditText mFirstOutgoingValue;

    /** Поле для второго исходящего значения */
    EditText mSecondOutgoingValue;

    /** Надпись вместо 'первое значение' */
    TextView mFirstValueLabel;

    /** Надпись вместо 'второе значение' */
    TextView mSecondValueLabel;

    // Набор строк, полученных из хеш-таблицы текущего узла

    /** Текст ошибки для первого значения */
    String mInvalidFirstValueText;

    /** Текст ошибки для второго значения */
    String mInvalidSecondValueText;

    /** Формула для первого входящего значения */
    String mIncomingFirstValueFormula;

    /** Формула для второго входящего значения */
    String mIncomingSecondValueFormula;

    /** Количество знаков после запятой для первого значения */
    String mFirstFractionDigits;

    /** Количество знаков после запятой для второго значения */
    String mSecondFractionDigits;

    /** Формула для первого исходящего значения */
    String mOutgoingFirstValueFormula;

    /** Булевая формула валидациия для первого исходящего значения */
    String mOutgoingFirstValueValidation;

    /** Формула для второго исходящего значения */
    String mOutgoingSecondValueFormula;

    /** Булевая формула валидациия для второго исходящего значения */
    String mOutgoingSecondValueValidation;

    /** Сообщения для получения данных от контроллера */
    String mGiveMeValueMessage;

    /** Сообщение для отправки значений контроллеру */
    String mOutgoingValueMessage;

    /** Диспетчер для отправки сообщения */
    MessageDispatcher mDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_send_range_value);

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

        // Значение из контроллера
        mFirstIncomingValue = (TextView) findViewById(R.id.first_incoming_value);
        mSecondIncomingValue = (TextView) findViewById(R.id.second_incoming_value);

        // Значение пользователя
        mFirstOutgoingValue = (EditText) findViewById(R.id.first_outgoing_value);
        mSecondOutgoingValue = (EditText) findViewById(R.id.second_outgoing_value);

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

        // Картинка для окна
        ImageView mImage = (ImageView) findViewById(R.id.image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

        // Вытягиваем строковые параметры из хеш-таблицы
        mInvalidFirstValueText = pMap.get("InvalidFirstValueText");
        mInvalidSecondValueText = pMap.get("InvalidSecondValueText");

        mIncomingFirstValueFormula = pMap.get("IncomingFirstValueFormula");
        mIncomingSecondValueFormula = pMap.get("IncomingSecondValueFormula");

        mFirstFractionDigits = pMap.get("FirstFractionDigits");
        mSecondFractionDigits = pMap.get("SecondFractionDigits");

        mOutgoingFirstValueFormula = pMap.get("FirstOutgoingValueFormula");
        mOutgoingSecondValueFormula = pMap.get("SecondOutgoingValueFormula");

        mOutgoingFirstValueValidation = pMap.get("FirstOutgoingValueValidation");
        mOutgoingSecondValueValidation = pMap.get("SecondOutgoingValueValidation");

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
        SetFont(R.id.first_incoming_value);
        SetFont(R.id.second_incoming_value);
        SetFont(R.id.current_value_label);
        SetFont(R.id.new_label_value);
        SetFont(R.id.first_outgoing_value);
        SetFont(R.id.second_outgoing_value);

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
     * Слушатель для кнопки отсылки значения Данный метод реализует бОльшую
     * часть функционала окна. Необходимо: 1. Обработать значение с помощью
     * формулы 2. Провести валидацию обработанного значения 3. Выслать значение
     * на контроллер
     */
    private OnClickListener sendButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {

            // --- ОБРАБОТКА ПЕРВОГО ЗНАЧЕНИЯ ---

            // Первое значение, введенное пользователем
            String variable = mFirstOutgoingValue.getText().toString();

            // 1. Обработка значения с помощью формулы
            MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                    mOutgoingFirstValueFormula, variable, "0", true);
            EvaluatorResult evalResult = evaluator.eval();

            // Проверка обработанного результата
            if (!evalResult.isCorrect) {
                Notifications.showError(mContext, "Ошибка при пересчете первого исходящего значения. Значение введено некорректно или формула пересчета задана некорректно");
                return;
            }

            // 2. Валидация обработанного первого значения булевой формулой
            BooleanFormulaEvaluator bEvaluator = new BooleanFormulaEvaluator(
                    mOutgoingFirstValueValidation,
                    evalResult.numericRoundedResult);
            EvaluatorResult boolEvalResult = bEvaluator.eval();

            // Проверка результатов валидации
            // Формула или значение некорректно
            if (!boolEvalResult.isCorrect) {
                Notifications.showError(mContext, "Ошибка при попытке валидации первого исходящего значения. Значение введено некорректно или формула валидации задана некорректно");
                return;
            }

            // Значение не прошло валидацию
            if (!boolEvalResult.booleanResult) {
                // Если значение не прошло валидацию - выводим ошибку с текстом, который ввел настройщик при создании конечной точки
                Notifications.showError(mContext, mInvalidFirstValueText);
                return;
            }

            // --- ОБРАБОТКА ВТОРОГО ЗНАЧЕНИЯ ---
            // Первое значение, введенное пользователем
            String second_variable = mSecondOutgoingValue.getText().toString();

            // 1. Обработка значения с помощью формулы
            MathematicalFormulaEvaluator second_evaluator = new MathematicalFormulaEvaluator(
                    mOutgoingSecondValueFormula, second_variable, "0", true);
            EvaluatorResult second_evalResult = second_evaluator.eval();

            // Проверка обработанного результата
            if (!second_evalResult.isCorrect) {
                Notifications.showError(mContext, "Ошибка при пересчете второго исходящего значения. Значение введено некорректно или формула пересчета задана некорректно");
                return;
            }

            // 2. Валидация обработанного первого значения булевой формулой
            BooleanFormulaEvaluator second_bEvaluator = new BooleanFormulaEvaluator(
                    mOutgoingSecondValueValidation,
                    second_evalResult.numericRoundedResult);
            EvaluatorResult second_boolEvalResult = second_bEvaluator.eval();

            // Проверка результатов валидации
            // Формула или значение некорректно
            if (!second_boolEvalResult.isCorrect) {
                Notifications.showError(mContext, "Ошибка при попытке валидации второго исходящего значения. Значение введено некорректно или формула валидации задана некорректно");
                return;
            }

            // Значение не прошло валидацию
            if (!second_boolEvalResult.booleanResult) {
                // Если значение не прошло валидацию - выводим ошибку с текстом, который ввел настройщик при создании конечной точки
                Notifications.showError(mContext, mInvalidSecondValueText);
                return;
            }

            // 3. Значение прошло валидацию и отсылается контроллеру
            mDispatcher.SendRangeValueMessage(mOutgoingValueMessage, evalResult.numericRoundedResult, second_evalResult.numericRoundedResult, true);

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

            String first_value="";
            String second_value="";

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

            // 1. Обработка первого значения с помощью формулы обработки входящего значения с указанием количества знаков после запятой
            MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                    mIncomingFirstValueFormula, first_value, mFirstFractionDigits, true);
            EvaluatorResult evalResult = evaluator.eval();
            // Проверяем результат
            if (!evalResult.isCorrect) {
                Notifications.showError(mContext, "Ошибка при пересчете входящего значения. Значение введено некорректно или формула пересчета задана некорректно");
                return;
            }

            // 2. Обработка первого значения с помощью формулы обработки входящего значения с указанием количества знаков после запятой
            MathematicalFormulaEvaluator second_evaluator = new MathematicalFormulaEvaluator(
                    mIncomingSecondValueFormula, second_value, mSecondFractionDigits, true);
            EvaluatorResult second_evalResult = second_evaluator.eval();

            // Проверяем результат
            if (!second_evalResult.isCorrect) {
                Notifications.showError(mContext, "Ошибка при пересчете второго входящего значения. Значение введено некорректно или формула пересчета задана некорректно");
                return;
            }

            // Установка первого значения
            mFirstIncomingValue.setText(evalResult.numericRoundedResult);
            mFirstIncomingValue.invalidate();

            // Установка второго значения
            mSecondIncomingValue.setText(second_evalResult.numericRoundedResult);
            mSecondIncomingValue.invalidate();
        }
    }
}
