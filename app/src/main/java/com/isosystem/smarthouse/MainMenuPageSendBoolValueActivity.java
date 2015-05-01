package com.isosystem.smarthouse;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.data.MenuTreeNode;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.util.HashMap;

public class MainMenuPageSendBoolValueActivity extends Activity {
    MyApplication mApp;
    Context mContext;
    // Ресивер для получения значения от контроллера
    ValueMessageReceiver mReceiver;

    // Текущая конечная точка
    MenuTreeNode node;
    // Полученное из контроллера значение
    TextView mIncomingValue;

    final String TAG_FALSE = "0";
    final String TAG_TRUE = "1";
    final String TAG_UNKNOWN = "2";

    // Надписи для булевых входящих и исходящих значений
    String mIncomingFalseText = "Выключено";
    String mIncomingTrueText = "Включено";
    // Если значение неизвестно
    String mIncomingUnknownText = "";
    String mOutgoingFalseText = "Выключить";
    String mOutgoingTrueText = "Включить";

    // Сообщение контроллеру для получения текущего значения
    String mGiveMeValueMessage;
    // Сообщение контроллеру для отсылки нового значения
    String mOutgoingValueMessage;
    // Диспетчер для отправки значения
    MessageDispatcher mDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_send_bool);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Сокрытие ActionBar и StatusBar
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setSystemUiVisibility(8);

        mContext = this;
        mApp = (MyApplication) getApplicationContext();

        try {
            // Создаем и подключаем броадкаст ресивер
            mReceiver = new ValueMessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Globals.BROADCAST_INTENT_VALUE_MESSAGE);
            registerReceiver(mReceiver, filter);
            Logging.v("Регистрируем ресивер PageBool");
        } catch (Exception e) {
            Logging.v("Исключение при попытке зарегистрировать ресивер");
            e.printStackTrace();
            finish();
        }

        // Получение текущей конечной точки
        node = (MenuTreeNode) getIntent().getSerializableExtra("Node");

        // Берем хеш-таблицу параметров узла
        HashMap<String, String> pMap = node.paramsMap;

        // Значение из контроллера
        mIncomingValue = (TextView) findViewById(R.id.psb_incoming_value);
        mIncomingValue.setText(mIncomingUnknownText);

        // Заголовок окна
        TextView mHeaderText = (TextView) findViewById(R.id.psb_tv_headertext);
        mHeaderText.setText(pMap.get("HeaderText"));

        // Текст описания
        TextView mDescriptionText = (TextView) findViewById(R.id.psb_tv_description_text);
        mDescriptionText.setText(pMap.get("DescriptionText"));

        // Картинка для окна
        ImageView mImage = (ImageView) findViewById(R.id.psb_image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

        // Надписи для кнопок и для надписи

        // Надпись для входящего 0
        String label = pMap.get("IncomingFalseText");
        if (!TextUtils.isEmpty(label.trim())) {
            mIncomingFalseText = label;
        }

        // Надпись для входящего 1
        label = pMap.get("IncomingTrueText");
        if (!TextUtils.isEmpty(label.trim())) {
            mIncomingTrueText = label;
        }

        // Надпись для исходящего 0
        label = pMap.get("OutgoingFalseText");
        if (!TextUtils.isEmpty(label.trim())) {
            mOutgoingFalseText = label;
        }

        // Надпись для исходящего 1
        label = pMap.get("OutgoingTrueText");
        if (!TextUtils.isEmpty(label.trim())) {
            mOutgoingTrueText = label;
        }

        mGiveMeValueMessage = pMap.get("GiveMeValueMessage");
        mOutgoingValueMessage = pMap.get("OutgoingValueMessage");

        // Кнопка отправки TRUE
        Button mSendTrueButton = (Button) findViewById(R.id.psb_set_true_button);
        mSendTrueButton.setOnClickListener(sendTrueButtonListener);
        mSendTrueButton.setText(mOutgoingTrueText);

        // Кнопка отправки FALSE
        Button mSendFalseButton = (Button) findViewById(R.id.psb_set_false_button);
        mSendFalseButton.setOnClickListener(sendFalseButtonListener);
        mSendFalseButton.setText(mOutgoingFalseText);

        // Кнопка назад
        Button mBackButton = (Button) findViewById(R.id.psb_back_button);
        mBackButton.setOnClickListener(backButtonListener);

        // Установка шрифта
        SetFont(R.id.psb_incoming_value);
        SetFont(R.id.psb_set_true_button);
        SetFont(R.id.psb_set_false_button);
        SetFont(R.id.psb_back_button);
        SetFont(R.id.psb_tv01);
        SetFont(R.id.psb_tv02);
        SetFont(R.id.psb_tv_description_text);
        SetFont(R.id.psb_tv_headertext);

        // Создаем объект диспетчера
        mDispatcher = new MessageDispatcher(this);
        // Отсылаем сообщение типа "Дай мне значение"
        mDispatcher.SendRawMessage(mGiveMeValueMessage);
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
     * Слушатель для кнопки отсылки TRUE значения.
     */
    private OnClickListener sendTrueButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            mDispatcher.sendBooleanMessage(mOutgoingValueMessage, 1, true);
            ((Activity) mContext).finish();
        }
    };

    /**
     * Слушатель для кнопки отсылки FALSE значения.
     */
    private OnClickListener sendFalseButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            mDispatcher.sendBooleanMessage(mOutgoingValueMessage, 0, true);
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
    public void onStop() {

        try {
            unregisterReceiver(mReceiver);
            Logging.v("Освобождаем ресивер PageBool");
        } catch (Exception e) {
            Logging.v("Исключение при попытке освободить ресивер");
            e.printStackTrace();
            finish();
        }
        super.onStop();
    }

    class ValueMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");

            if (msg.length() < 3) {
                Logging.v("Ошибка при получении значения из контроллера");
                return;
            }

            // Удаление служебных символов
            msg = msg.substring(2);

            // Парсинг полученного значения
            int incomingValue = -1;
            try {
                incomingValue = Integer.parseInt(msg);
            } catch (Exception e) {
                Logging.v("Ошибка при получении значения из контроллера");
            }

            // В зависимости от получения 0/1/2 - вывод соответствующей надписи
            // 2 - если контроллер не знает значение
            // Если пришло что-то еще - вывод ошибки
            if (incomingValue == 0) {
                mIncomingValue.setText(mIncomingFalseText);
                mIncomingValue.invalidate();
            } else if (incomingValue == 1) {
                mIncomingValue.setText(mIncomingTrueText);
                mIncomingValue.invalidate();
            } else if (incomingValue == 2) {
                mIncomingValue.setText(mIncomingUnknownText);
                mIncomingValue.invalidate();
            } else {
                mIncomingValue.setText("Ошибка!");
                mIncomingValue.invalidate();
                Logging.v("Ошибка при получении значения из контроллера");
            }
        }
    }
}
