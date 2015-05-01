package com.isosystem.smarthouse;

import android.app.Activity;
import android.content.Context;
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
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.util.HashMap;

public class MainMenuPageSendPasswordActivity extends Activity {

    MyApplication mApp;
    Context mContext;

    // Текущая конечная точка
    MenuTreeNode node;
    // Пароль, введенный пользователем
    EditText mOutgoingPassword;

    // Строка с сообщением для отправки пароля
    String mOutgoingValueMessage;
    // Диспетчер для отправки значения
    MessageDispatcher mDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mApp = (MyApplication) getApplicationContext();
        setContentView(R.layout.activity_page_send_password);

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

        // Получение текущего узла
        node = (MenuTreeNode) getIntent().getSerializableExtra("Node");
        // Берем хеш-таблицу параметров узла
        HashMap<String, String> pMap = node.paramsMap;

        // Пароль, введенный пользователем
        mOutgoingPassword = (EditText) findViewById(R.id.psv_outgoing_value);

        // Строки, полученные из хеш-таблицы текущего узла

        // Заголовок
        TextView mHeaderText = (TextView) findViewById(R.id.psv_tv_headertext);
        mHeaderText.setText(pMap.get("HeaderText"));

        // Текст описания
        TextView mDescriptionText = (TextView) findViewById(R.id.psv_tv_description_text);
        mDescriptionText.setText(pMap.get("DescriptionText"));

        // Картинка окна
        ImageView mImage = (ImageView) findViewById(R.id.psv_image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

        // Префикс сообщения для отправки контроллеру
        mOutgoingValueMessage = pMap.get("OutgoingValueMessage");

        // Кнопка "Ввести"
        Button mSendButton = (Button) findViewById(R.id.psv_send_button);
        mSendButton.setOnClickListener(sendButtonListener);
        // Кнопка "Назад"
        Button mBackButton = (Button) findViewById(R.id.psv_back_button);
        mBackButton.setOnClickListener(backButtonListener);

        // Установка шрифта
        SetFont(R.id.psv_outgoing_value);
        SetFont(R.id.psv_send_button);
        SetFont(R.id.psv_back_button);
        SetFont(R.id.psv_tv02);
        SetFont(R.id.psv_tv_description_text);
        SetFont(R.id.psv_tv_headertext);

        // Создаем объект диспетчера
        mDispatcher = new MessageDispatcher(this);
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
     * Слушатель для кнопки отсылки пароля
     */
    private OnClickListener sendButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            String password = mOutgoingPassword.getText().toString();

            // Отслыка пароля
            if (TextUtils.isEmpty(password.trim())) {
                Notifications.showError(mContext, "Поле пароля пустое");
                return;
            }
            mDispatcher.SendValueMessage(mOutgoingValueMessage,
                    password, true);
            ((Activity) mContext).finish();
            overridePendingTransition(R.anim.flipin, R.anim.flipout);
        }
    };

    /**
     * Слушатель для кнопки "Назад"
     */
    private OnClickListener backButtonListener = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            finish();
            overridePendingTransition(R.anim.flipin, R.anim.flipout);
        }
    };
}
