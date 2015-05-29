package com.isosystem.smarthouse;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
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

public class MainMenuPageSendIntValueActivity extends Activity {

    MyApplication mApp;
    Context mContext;
    // Ресивер для получения значения от контроллера
    ValueMessageReceiver mReceiver;

    // Текущая конечная точка
    MenuTreeNode node;
    // Входящее значение
    TextView mIncomingValue;
    // Исходящее значение
    EditText mOutgoingValue;

    // Набор строк, полученных из хеш-таблицы текущего узла
    String mInvalidValueText;
    String mIncomingValueFormula;
    String mFractionDigits;
    String mOutgoingValueFormula;
    String mOutgoingValueValidation;
    String mGiveMeValueMessage;
    String mOutgoingValueMessage;

    // Диспетчер для отправки значения
    MessageDispatcher mDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_send_value);

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
        mIncomingValue = (TextView) findViewById(R.id.psv_incoming_value);
        // Значение пользователя
        mOutgoingValue = (EditText) findViewById(R.id.psv_outgoing_value);

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

        // Вытягиваем строковые параметры из хеш-таблицы
        mInvalidValueText = pMap.get("InvalidValueText");
        mIncomingValueFormula = pMap.get("IncomingValueFormula");
        mFractionDigits = pMap.get("FractionDigits");
        mOutgoingValueFormula = pMap.get("OutgoingValueFormula");
        mOutgoingValueValidation = pMap.get("OutgoingValueValidation");
        mGiveMeValueMessage = pMap.get("GiveMeValueMessage");
        mOutgoingValueMessage = pMap.get("OutgoingValueMessage");

        // Кнопка "Установить"
        Button mSendButton = (Button) findViewById(R.id.psv_send_button);
        mSendButton.setOnClickListener(sendButtonListener);
        // Кнопка "Назад"
        Button mBackButton = (Button) findViewById(R.id.psv_back_button);
        mBackButton.setOnClickListener(backButtonListener);

        // Установка шрифта
        SetFont(R.id.psv_incoming_value);
        SetFont(R.id.psv_outgoing_value);
        SetFont(R.id.psv_send_button);
        SetFont(R.id.psv_back_button);
        SetFont(R.id.psv_tv01);
        SetFont(R.id.psv_tv02);
        SetFont(R.id.psv_tv_description_text);
        SetFont(R.id.psv_tv_headertext);

        // Создаем объект диспетчера
        mDispatcher = new MessageDispatcher(this);
        mDispatcher.sendGiveMeValueMessage(mGiveMeValueMessage, true);
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

            // Значение, введенное пользователем
            String variable = mOutgoingValue.getText().toString();
            Logging.v("Изначальное значение :" + variable);

            // 1. Обработка значения с помощью формулы
            MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                    mOutgoingValueFormula, variable, "0", true);
            EvaluatorResult evalResult = evaluator.eval();

            // Проверяем результат
            if (!evalResult.isCorrect) {
                Logging.v("Ошибка при обработке введенного значения формулой пересчета значения");
                Notifications.showError(mContext, "Ошибка при пересчете исходящего значения. Значение введено некорректно или формула пересчета задана некорректно");
            } else {
                Logging.v("Значение после формулы: " + evalResult.numericRoundedResult);

                // 2. Валидация обработанного значения булевой формулой
                BooleanFormulaEvaluator bEvaluator = new BooleanFormulaEvaluator(
                        mOutgoingValueValidation,
                        evalResult.numericRoundedResult);
                EvaluatorResult boolEvalResult = bEvaluator.eval();

                // Проверка результатов валидации
                if (!boolEvalResult.isCorrect) {
                    // Валидация не удалась
                    Logging.v("Ошибка при валидации пересчитанного значения булевой формулой");
                    Notifications.showError(mContext, "Ошибка при попытке валидации исходящего значения. Значение введено некорректно или формула валидации задана некорректно");
                } else {
                    if (!boolEvalResult.booleanResult) {
                        // Если значение не прошло валидацию - выводим ошибку с текстом, который ввел настройщик при создании конечной точки
                        Notifications.showError(mContext, mInvalidValueText);
                    } else {
                        // 3. Значение прошло валидацию и отсылается контроллеру

                        mDispatcher.SendValueMessage(mOutgoingValueMessage,
                                evalResult.numericRoundedResult, true);

                                ((Activity) mContext).finish();
                    }
                }
            }
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

            Logging.v("Значение: " + msg);
            Logging.v("Формула: " + mIncomingValueFormula);

            // 1. Обработка значения с помощью формулы обработки входящего значения с указанием количества знаков после запятой
            MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                    mIncomingValueFormula, msg, mFractionDigits, true);
            EvaluatorResult evalResult = evaluator.eval();

            // Проверяем результат
            if (!evalResult.isCorrect) {
                Notifications.showError(mContext, "Ошибка при пересчете входящего значения. Значение введено некорректно или формула пересчета задана некорректно");
            } else {
                Logging.v("Значение после формулы");
                mIncomingValue.setText(evalResult.numericRoundedResult);
                mIncomingValue.invalidate();
            }
        }
    }
}
