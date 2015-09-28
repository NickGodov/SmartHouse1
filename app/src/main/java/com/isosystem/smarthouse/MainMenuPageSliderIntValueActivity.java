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

    // ������� ��� ��������� �������� �� �����������
    ValueMessageReceiver mReceiver;
    // ������� �������� �����
    MenuTreeNode node;

    // ������� ��� ��������� ��������� ��������
    String mIncomingValueFormula;
    // ������� ��� ��������� ���������� ��������
    String mOutgoingValueFormula;
    // ��������� ��� ������� �������� �� �����������
    String mGiveMeValueMessage;
    // ������� ��� ���������� ��������
    String mOutgoingValueMessage;

    // �������
    SeekBar mSeekBar;

    /** Handler ��� ��������� ������ */
    Handler mDynamicSendHandler = new Handler();

    // ������� ��� ��������
    // ����������� �������� ��������
    TextView mTextViewSliderMinValue;
    // ������������ �������� ��������
    TextView mTextViewSliderMaxValue;
    // ������� �������� ��������
    TextView mTextViewSliderValue;

    // ����������� �������� ��������
    Float mSliderMinValue;
    // ������������ �������� ��������
    Float mSliderMaxValue;
    // ��� ��������
    Float mSliderStepValue;

    // ������������ ����� ������� ��������
    Boolean mDynamicMode = false;
    // ������� ��� �������
    int mDynamicSendTimeout;

    // ���������� �������� ��������
    Float mPreviousSliderValue = 0.0f;

    // ������ "����������"
    Button mSendButton;

    // ��������� ��� �������� ��������
    MessageDispatcher mDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_slider_int_value);

        // ������ ��������� ���������� ��������� ������
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

        // ��������� ������ ������� �������� �����
        node = (MenuTreeNode) getIntent().getSerializableExtra("Node");

        // ����� ���-������� ���������� ����
        HashMap<String, String> pMap = node.paramsMap;

        // ���������
        TextView mHeaderText = (TextView) findViewById(R.id.psv_tv_headertext);
        mHeaderText.setText(pMap.get("HeaderText"));

        // ����� ��������
        TextView mDescriptionText = (TextView) findViewById(R.id.psv_tv_description_text);
        mDescriptionText.setText(pMap.get("DescriptionText"));

        // �������� ��� ����
        ImageView mImage = (ImageView) findViewById(R.id.psv_image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

        // ������������ �����
        if (pMap.get("DynamicMode")!=null){
            if (pMap.get("DynamicMode").equals("0")){
                mDynamicMode = false;
            } else if (pMap.get("DynamicMode").equals("1")) {
                mDynamicMode = true;
            } else {
                Notifications.showError(mContext, "���������� ������ ��� ������� ������� ������");
                Logging.v("���������� ��� ������� �������� ������ �������� �����. ��������: DynamicMode");
            }
        }

        // ����-��� �������
        if (pMap.get("DynamicSendTimeout")!=null) {
            if (mDynamicMode) {
                try {
                    mDynamicSendTimeout = Integer.parseInt(pMap.get("DynamicSendTimeout"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Notifications.showError(mContext, "������ ��� ���������� ������ ��� ��������");
                    Logging.v("���������� ��� ������� �������� ������ �������� �����. ��������: DynamicSendTimeout");
                    MainMenuPageSliderIntValueActivity.this.finish();
                }
            }
        }

        mIncomingValueFormula = pMap.get("IncomingValueFormula");
        mOutgoingValueFormula = pMap.get("OutgoingValueFormula");
        mGiveMeValueMessage = pMap.get("GiveMeValueMessage");
        mOutgoingValueMessage = pMap.get("OutgoingValueMessage");

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        // ���������� �������� ��������
        try {
            mSliderMinValue = Float.parseFloat(pMap.get("SliderMinValue"));
            mSliderMaxValue = Float.parseFloat(pMap.get("SliderMaxValue"));
            mSliderStepValue = Float.parseFloat(pMap.get("SliderStepValue"));
        } catch (Exception e) {
            e.printStackTrace();
            Logging.v("������ ��� ���������� ������ ��� ��������");
            Notifications.showError(this,"���������� ������ ������!");
            MainMenuPageSliderIntValueActivity.this.finish();
        }

        // ��������� ������� ��� ������������� �������� ��������
        mTextViewSliderMaxValue = (TextView) findViewById(R.id.seekBarMaxValue);
        mTextViewSliderMaxValue.setText(String.valueOf(mSliderMaxValue));
        // ��������� ������� ��� ������������ �������� ��������
        mTextViewSliderMinValue = (TextView) findViewById(R.id.seekBarMinValue);
        mTextViewSliderMinValue.setText(String.valueOf(mSliderMinValue));

        // ��������� �������� ��� ������ ����, ��� ��������� �� ������� ����� �������
        float start_value = mSliderMinValue + (mSeekBar.getProgress()*mSliderStepValue);
        // ������������� ��������� �������� ��� ������ �������� ��������
        mTextViewSliderValue = (TextView) findViewById(R.id.seekBarValue);
        mTextViewSliderValue.setText(String.valueOf(start_value));
        // ������������� ���������� �������� ������ ����������
        mPreviousSliderValue = start_value;

        // ��������� ������������� �������� ��������
        // ��� ����� (max-min)/step
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

        //������ "����������"
        mSendButton = (Button) findViewById(R.id.psv_send_button);
        mSendButton.setOnClickListener(sendButtonListener);
        if (mDynamicMode){
            mSendButton.setVisibility(View.GONE);
        } else {
            mSendButton.setVisibility(View.VISIBLE);
        }

        // ������ "�����"
        Button mBackButton = (Button) findViewById(R.id.psv_back_button);
        mBackButton.setOnClickListener(backButtonListener);

        // ��������� ������
        SetFont(R.id.psv_tv_headertext);
        SetFont(R.id.psv_tv_description_text);
        SetFont(R.id.seekBarValue);
        SetFont(R.id.seekBarMinValue);
        SetFont(R.id.seekBarMaxValue);
        SetFont(R.id.psv_send_button);
        SetFont(R.id.psv_back_button);

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

    private void sendValue(String value) {
        Logging.v("����������� �������� :" + value);

        // 1. ��������� �������� � ������� �������
        MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                mOutgoingValueFormula, value, "0", true);
        EvaluatorResult evalResult = evaluator.eval();

        // ��������� ���������
        if (!evalResult.isCorrect) {
            Logging.v("������ ��� ��������� ���������� �������� �������� ��������� ��������. ������ �� ���� �������");
            Notifications.showError(mContext, "������ ��� ��������� ���������� ��������. �������� ������� ����������� ��� ������� ��������� ������ �����������. ������� ��������� ��������������");
        } else {
            mDispatcher.SendValueMessage(mOutgoingValueMessage,evalResult.numericRoundedResult, true);
        }
    }

    /**
     * ��������� ��� ������ ������� ��������
     */
    private OnClickListener sendButtonListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            String value = String.valueOf(mSliderMinValue + (mSeekBar.getProgress() * mSliderStepValue));
            sendValue(value);
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

            // 1. ��������� �������� � ������� ������� ��������� ��������� �������� � ��������� ���������� ������ ����� �������
            MathematicalFormulaEvaluator evaluator = new MathematicalFormulaEvaluator(
                    mIncomingValueFormula, msg, "2", true);
            EvaluatorResult evalResult = evaluator.eval();

            // ��������� ���������
            if (!evalResult.isCorrect) {
                Notifications.showError(mContext, "������ ��� ��������� ��������� ��������. �������� ������� ����������� ��� ������� ��������� ������ �����������");
            } else {

                //������������� �������� ��� ��������
                float incoming_value = Float.parseFloat(evalResult.numericRoundedResult);

                if (incoming_value > mSliderMaxValue) {
                    Notifications.showError(mContext,"���������� �������� �� ����������� ������ ����������� ����������. ������������� ����������� ����������� ��������� ��������");
                    incoming_value = mSliderMaxValue;
                } else if (incoming_value < mSliderMinValue) {
                    Notifications.showError(mContext,"���������� �������� �� ����������� ������ ���������� ����������. ������������� ����������� ���������� ��������� ��������");
                    incoming_value = mSliderMinValue;
                }
                int progress_value = (int)((incoming_value - mSliderMinValue)/mSliderStepValue);
                mPreviousSliderValue = incoming_value;
                mSeekBar.setProgress(progress_value);
            }
        }
    }
}
