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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.data.MenuTreeNode;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainMenuPageSendPasswordActivity extends Activity {

    MyApplication mApp;
    Context mContext;

    // ������� ��� ��������� �������� �� �����������
    ValueMessageReceiver mReceiver;

    // ������� �������� �����
    MenuTreeNode node;
    // ������, ��������� �������������
    EditText mOutgoingPassword;

    // ������ � ���������� ��� �������� ������
    String mOutgoingValueMessage;
    // ��������� ��� �������� ��������
    MessageDispatcher mDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mApp = (MyApplication) getApplicationContext();
        setContentView(R.layout.activity_page_send_password);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // �������� ActionBar � StatusBar
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setSystemUiVisibility(8);

        // ��������� �������� ����
        node = (MenuTreeNode) getIntent().getSerializableExtra("Node");
        // ����� ���-������� ���������� ����
        HashMap<String, String> pMap = node.paramsMap;

        // ������, ��������� �������������
        mOutgoingPassword = (EditText) findViewById(R.id.psv_outgoing_value);

        // ������, ���������� �� ���-������� �������� ����

        // ���������
        TextView mHeaderText = (TextView) findViewById(R.id.psv_tv_headertext);
        mHeaderText.setText(pMap.get("HeaderText"));

        // ����� ��������
        TextView mDescriptionText = (TextView) findViewById(R.id.psv_tv_description_text);
        mDescriptionText.setText(pMap.get("DescriptionText"));

        // �������� ����
        ImageView mImage = (ImageView) findViewById(R.id.psv_image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

        // ������� ��������� ��� �������� �����������
        mOutgoingValueMessage = pMap.get("OutgoingValueMessage");

        // ������ "������"
        Button mSendButton = (Button) findViewById(R.id.psv_send_button);
        mSendButton.setOnClickListener(sendButtonListener);
        // ������ "�����"
        Button mBackButton = (Button) findViewById(R.id.psv_back_button);
        mBackButton.setOnClickListener(backButtonListener);

        // ��������� ������
        SetFont(R.id.psv_outgoing_value);
        SetFont(R.id.psv_send_button);
        SetFont(R.id.psv_back_button);
        SetFont(R.id.psv_tv02);
        SetFont(R.id.psv_tv_description_text);
        SetFont(R.id.psv_tv_headertext);

        try {
            // ������� � ���������� ��������� �������
            mReceiver = new ValueMessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE);
            registerReceiver(mReceiver, filter);
            Logging.v("������������ ������� PageBool");
        } catch (Exception e) {
            Logging.v("���������� ��� ������� ���������������� �������");
            e.printStackTrace();
            finish();
        }

        // ������� ������ ����������
        mDispatcher = new MessageDispatcher(this);
    }

    /**
     * ������������� �������� �����. � �������� �������� ��������� - id ��������
     * �� R.java
     */
    private void SetFont(int id) {
        Typeface font = Typeface.createFromAsset(getAssets(), "myfont.ttf");
        TextView et = (TextView) findViewById(id);
        et.setTypeface(font);
        et.invalidate();
    }

    /**
     * ��������� ��� ������ ������� ������
     */
    private OnClickListener sendButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            String password = mOutgoingPassword.getText().toString();

            // ������� ������
            if (TextUtils.isEmpty(password.trim())) {
                Notifications.showError(mContext, "���� ������ ������");
                return;
            }
            mDispatcher.SendValueMessage(mOutgoingValueMessage,
                    password, true);
            ((Activity) mContext).finish();
        }
    };

    /**
     * ��������� ��� ������ "�����"
     */
    private OnClickListener backButtonListener = new OnClickListener() {

        @Override
        public void onClick(final View v) {
            finish();
        }
    };

    /**
     * �������������� �������� ���� ���������������� ������: <br />
     * 1. � ������� ����������� ��������� ���� ����� � ��������� �� �����������; <br />
     * 2. ���� ����� �������, �������� ��������� ������ � INT; <br />
     * 3. �������� ��������� � ������������� ������� ���� ���������������� ������. <br />
     * @param message ��������� �� �����������
     */
    private void forcedFormattedScreenStart(String message){
        String number = "";
        // ��������� ����� ���� � ���������
        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(message);
        while (m.find()) {
            number = m.group();
        }

        // ������ ����� ���� � �������� ���������
        int screen_number = -1;
        try {
            screen_number = Integer.parseInt(number);
            if (screen_number >=0 && screen_number < mApp.mFormattedScreens.mFormattedScreens.size()) {
                // ����� ���� ����������, �������� ���������
                mDispatcher = new MessageDispatcher(this);
                mDispatcher.sendGiveMeValueMessage(mApp.mFormattedScreens.mFormattedScreens.get(screen_number).mCannotOpenWindowMessage,true);
            } else {
                // ����� ���� ������������
                Intent i = new Intent();
                String alarmMessage = "�������� ��������� � ���������������� ������";
                mApp.mAlarmMessages.addAlarmMessage(
                        mApp, alarmMessage,
                        Notifications.MessageType.ControllerMessage);
                // ������ ���������
                i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
                mApp.sendBroadcast(i);
            }
        } catch (NumberFormatException e) {
            // ��������� ������ � �������� ������� (�� ������ ����� �����)
            Intent i = new Intent();
            String alarmMessage = "�������� ��������� � ���������������� ������";
            mApp.mAlarmMessages.addAlarmMessage(
                    mApp, alarmMessage,
                    Notifications.MessageType.ControllerMessage);
            // ������ ���������
            i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
            mApp.sendBroadcast(i);

            Logging.v("���������� ��� ������� �������� ������ ���� ���������������� ������." +
                    "������ ��������: " + number);
            e.printStackTrace();
        }
    }

    class ValueMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // ���� �������������� �������� ����, �������� ����� � �������� ���
            // extra � ���� ��������� �� �����������
            if (intent.getAction().equals(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE)) {
                String msg = intent.getStringExtra("message");
                forcedFormattedScreenStart(msg);
                return;
            }
        }
    }

}
