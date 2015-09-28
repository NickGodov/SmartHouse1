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
 * ���� ����� ���� ������� ��������� �������� ��������.
 * ��� ������������� ������� ������, �� extras �������
 * �������� ����� {@link MenuTreeNode}, ����� ���� ����������� ��� ���������.
 * ����� ��������������� ������ ��� TextView � EditText
 *
 * ��� ������ ���� ����������� ��������� ������� {@link com.isosystem.smarthouse.MainMenuPageSendRangeIntValueActivity.ValueMessageReceiver}
 * ������� ������� ��������� �� {@link com.isosystem.smarthouse.connection.USBReceiveService} � ������� �� �����������.
 *
 * ����� �������� ���������, �� ���� ��������� ������ ��� ������� ("&,"), ����� ����
 * ����������� �-��� String.split, ������� ��������� �������� �������� �� ������� '-' � �����.
 * ��� �������� ������������ �� ������
 *
 * ����� ������������ ������ �������� � �������� ������ "����������",
 * ������� ����������� ������ �������� (�������������� �������� � ���������� ��������� �������
 * ��������), ����� ���� ����� ����������� ������ ��������. ���� ��� �������� ������ ���������,
 * � ������� {@link MessageDispatcher} ��� ������������ �����������.
 *
 * @author ����������� ������� (nick.godov@gmail.com)
 * @see com.isosystem.smarthouse.MainMenuPageSendRangeIntValueActivity.ValueMessageReceiver
 * @see com.isosystem.smarthouse.connection.USBReceiveService
 * @see MenuTreeNode
 * @see MessageDispatcher
 */
public class MainMenuPageSendRangeIntValueActivity extends Activity {

    MyApplication mApp;
    Context mContext;

    /** ������� ��� ��������� �������� �� ����������� */
    ValueMessageReceiver mReceiver;

    /** ������� �������� ����� */
    MenuTreeNode node;

    /** ������� ��� ������� ��������� �������� */
    TextView mFirstIncomingValue;

    /** ������� ��� ������� ��������� �������� */
    TextView mSecondIncomingValue;

    /** ���� ��� ������� ���������� �������� */
    EditText mFirstOutgoingValue;

    /** ���� ��� ������� ���������� �������� */
    EditText mSecondOutgoingValue;

    /** ������� ������ '������ ��������' */
    TextView mFirstValueLabel;

    /** ������� ������ '������ ��������' */
    TextView mSecondValueLabel;

    // ����� �����, ���������� �� ���-������� �������� ����

    /** ����� ������ ��� ������� �������� */
    String mInvalidFirstValueText;

    /** ����� ������ ��� ������� �������� */
    String mInvalidSecondValueText;

    /** ������� ��� ������� ��������� �������� */
    String mIncomingFirstValueFormula;

    /** ������� ��� ������� ��������� �������� */
    String mIncomingSecondValueFormula;

    /** ���������� ������ ����� ������� ��� ������� �������� */
    String mFirstFractionDigits;

    /** ���������� ������ ����� ������� ��� ������� �������� */
    String mSecondFractionDigits;

    /** ������� ��� ������� ���������� �������� */
    String mOutgoingFirstValueFormula;

    /** ������� ������� ���������� ��� ������� ���������� �������� */
    String mOutgoingFirstValueValidation;

    /** ������� ��� ������� ���������� �������� */
    String mOutgoingSecondValueFormula;

    /** ������� ������� ���������� ��� ������� ���������� �������� */
    String mOutgoingSecondValueValidation;

    /** ��������� ��� ��������� ������ �� ����������� */
    String mGiveMeValueMessage;

    /** ��������� ��� �������� �������� ����������� */
    String mOutgoingValueMessage;

    /** ��������� ��� �������� ��������� */
    MessageDispatcher mDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_send_range_value);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // ��������� �������������� ����� ��������
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

        // ��������� ������� �������� �����
        node = (MenuTreeNode) getIntent().getSerializableExtra("Node");

        // ����� ���-������� ���������� ����
        HashMap<String, String> pMap = node.paramsMap;

        // �������� �� �����������
        mFirstIncomingValue = (TextView) findViewById(R.id.first_incoming_value);
        mSecondIncomingValue = (TextView) findViewById(R.id.second_incoming_value);

        // �������� ������������
        mFirstOutgoingValue = (EditText) findViewById(R.id.first_outgoing_value);
        mSecondOutgoingValue = (EditText) findViewById(R.id.second_outgoing_value);

        // ���������
        TextView mHeaderText = (TextView) findViewById(R.id.header);
        mHeaderText.setText(pMap.get("HeaderText"));

        // ����� ��������
        TextView mDescriptionText = (TextView) findViewById(R.id.description);
        mDescriptionText.setText(pMap.get("DescriptionText"));

        // ������� '������ ��������' � '������ ��������'
        mFirstValueLabel = (TextView) findViewById(R.id.first_value_label);
        mSecondValueLabel = (TextView) findViewById(R.id.second_value_label);

        // ������� ������ '������ ��������'
        String first_value_label = pMap.get("FirstValueLabel");
        if (!TextUtils.isEmpty(first_value_label.trim())) {
            mFirstValueLabel.setText(first_value_label);
        }

        // ������� ������ '������ ��������'
        String second_value_label = pMap.get("SecondValueLabel");
        if (!TextUtils.isEmpty(second_value_label.trim())) {
            mSecondValueLabel.setText(second_value_label);
        }

        // �������� ��� ����
        ImageView mImage = (ImageView) findViewById(R.id.image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

        // ���������� ��������� ��������� �� ���-�������
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

        // ������ "����������"
        Button mSendButton = (Button) findViewById(R.id.send_button);
        mSendButton.setOnClickListener(sendButtonListener);

        // ������ "�����"
        Button mBackButton = (Button) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(backButtonListener);

        // ��������� ������
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

        // ������� ������ ����������
        mDispatcher = new MessageDispatcher(this);
        mDispatcher.sendGiveMeValueMessage(mGiveMeValueMessage,true);
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
     * ��������� ��� ������ ������� �������� ������ ����� ��������� �������
     * ����� ����������� ����. ����������: 1. ���������� �������� � �������
     * ������� 2. �������� ��������� ������������� �������� 3. ������� ��������
     * �� ����������
     */
    private OnClickListener sendButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {

            // --- ��������� ������� �������� ---

            // ������ ��������, ��������� �������������
            String variable = mFirstOutgoingValue.getText().toString();

            // 1. ��������� �������� � ������� �������
            MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                    mOutgoingFirstValueFormula, variable, "0", true);
            EvaluatorResult evalResult = evaluator.eval();

            // �������� ������������� ����������
            if (!evalResult.isCorrect) {
                Notifications.showError(mContext, "������ ��� ��������� ������� ���������� ��������. �������� ������� ����������� ��� ������� ��������� ������ �����������");
                return;
            }

            // 2. ��������� ������������� ������� �������� ������� ��������
            BooleanFormulaEvaluator bEvaluator = new BooleanFormulaEvaluator(
                    mOutgoingFirstValueValidation,
                    evalResult.numericRoundedResult);
            EvaluatorResult boolEvalResult = bEvaluator.eval();

            // �������� ����������� ���������
            // ������� ��� �������� �����������
            if (!boolEvalResult.isCorrect) {
                Notifications.showError(mContext, "������ ��� ������� ��������� ������� ���������� ��������. �������� ������� ����������� ��� ������� ��������� ������ �����������");
                return;
            }

            // �������� �� ������ ���������
            if (!boolEvalResult.booleanResult) {
                // ���� �������� �� ������ ��������� - ������� ������ � �������, ������� ���� ���������� ��� �������� �������� �����
                Notifications.showError(mContext, mInvalidFirstValueText);
                return;
            }

            // --- ��������� ������� �������� ---
            // ������ ��������, ��������� �������������
            String second_variable = mSecondOutgoingValue.getText().toString();

            // 1. ��������� �������� � ������� �������
            MathematicalFormulaEvaluator second_evaluator = new MathematicalFormulaEvaluator(
                    mOutgoingSecondValueFormula, second_variable, "0", true);
            EvaluatorResult second_evalResult = second_evaluator.eval();

            // �������� ������������� ����������
            if (!second_evalResult.isCorrect) {
                Notifications.showError(mContext, "������ ��� ��������� ������� ���������� ��������. �������� ������� ����������� ��� ������� ��������� ������ �����������");
                return;
            }

            // 2. ��������� ������������� ������� �������� ������� ��������
            BooleanFormulaEvaluator second_bEvaluator = new BooleanFormulaEvaluator(
                    mOutgoingSecondValueValidation,
                    second_evalResult.numericRoundedResult);
            EvaluatorResult second_boolEvalResult = second_bEvaluator.eval();

            // �������� ����������� ���������
            // ������� ��� �������� �����������
            if (!second_boolEvalResult.isCorrect) {
                Notifications.showError(mContext, "������ ��� ������� ��������� ������� ���������� ��������. �������� ������� ����������� ��� ������� ��������� ������ �����������");
                return;
            }

            // �������� �� ������ ���������
            if (!second_boolEvalResult.booleanResult) {
                // ���� �������� �� ������ ��������� - ������� ������ � �������, ������� ���� ���������� ��� �������� �������� �����
                Notifications.showError(mContext, mInvalidSecondValueText);
                return;
            }

            // 3. �������� ������ ��������� � ���������� �����������
            mDispatcher.SendRangeValueMessage(mOutgoingValueMessage, evalResult.numericRoundedResult, second_evalResult.numericRoundedResult, true);

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

    @Override
    public void onStart() {
        super.onStart();
        try {
            // ������� � ���������� ��������� �������
            mReceiver = new ValueMessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Globals.BROADCAST_INTENT_VALUE_MESSAGE);
            filter.addAction(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE);
            registerReceiver(mReceiver, filter);
            Logging.v("������������ ������� Page");
        } catch (Exception e) {
            Logging.v("���������� ��� ������� ���������������� �������");
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onStop() {
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
                Logging.v("����������� ������� Page");
            } catch (Exception e) {
                Logging.v("���������� ��� ������� ���������� �������");
                e.printStackTrace();
                finish();
            }
        }
        super.onStop();
    }

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

            // �������� ���������. ��� ������ ���� ���������� �������� � ������
            // ����������� ������ ����� �������
            String msg = intent.getStringExtra("message");

            if (msg.length() < 3) {
                Logging.v("�������� ������ ���������");
                return;
            }
            msg = msg.substring(2);

            String first_value="";
            String second_value="";

            // ������ ���������, ������� ������ �������� (�� ����� '-') � ������ ��������
            try {
                String[] parts = msg.split("-");
                first_value = parts[0];
                second_value = parts[1];
            } catch (Exception e) {
                e.printStackTrace();
                Notifications.showError(mContext,"������ ��� ������� ���������� ��������� �� �����������");
                Logging.v("���������� ��� ������� �������� ��������� �� �����������. ��������� �� �����������: " + msg);
                return;
            }

            // 1. ��������� ������� �������� � ������� ������� ��������� ��������� �������� � ��������� ���������� ������ ����� �������
            MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                    mIncomingFirstValueFormula, first_value, mFirstFractionDigits, true);
            EvaluatorResult evalResult = evaluator.eval();
            // ��������� ���������
            if (!evalResult.isCorrect) {
                Notifications.showError(mContext, "������ ��� ��������� ��������� ��������. �������� ������� ����������� ��� ������� ��������� ������ �����������");
                return;
            }

            // 2. ��������� ������� �������� � ������� ������� ��������� ��������� �������� � ��������� ���������� ������ ����� �������
            MathematicalFormulaEvaluator second_evaluator = new MathematicalFormulaEvaluator(
                    mIncomingSecondValueFormula, second_value, mSecondFractionDigits, true);
            EvaluatorResult second_evalResult = second_evaluator.eval();

            // ��������� ���������
            if (!second_evalResult.isCorrect) {
                Notifications.showError(mContext, "������ ��� ��������� ������� ��������� ��������. �������� ������� ����������� ��� ������� ��������� ������ �����������");
                return;
            }

            // ��������� ������� ��������
            mFirstIncomingValue.setText(evalResult.numericRoundedResult);
            mFirstIncomingValue.invalidate();

            // ��������� ������� ��������
            mSecondIncomingValue.setText(second_evalResult.numericRoundedResult);
            mSecondIncomingValue.invalidate();
        }
    }
}
