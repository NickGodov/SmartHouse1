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

    /** ������� ��� ��������� �������� �� ����������� */
    ValueMessageReceiver mReceiver;

    /** ������� �������� ����� */
    MenuTreeNode node;

    /** ����� ������ ��� ����� ������������� ��������� ���/�������-��� */
    String mDateTimeRangeErrorText = "������ ��������� ���/���-�������. ������ �������� ������ ���� ������";

    /** ������� ������ '������ ��������' */
    TextView mFirstValueLabel;

    /** ������� ������ '������ ��������' */
    TextView mSecondValueLabel;

    /** ��������� ��� ��������� ������ �� ����������� */
    String mGiveMeValueMessage;

    /** ��������� ��� �������� �������� ����������� */
    String mOutgoingValueMessage;

    /** ��������� ��� �������� ��������� */
    MessageDispatcher mDispatcher;

    /** ����� ��� ������� �������� */
    TimePicker mFirstTimePicker;

    /** ����� ��� ������� �������� */
    TimePicker mSecondTimePicker;

    /** ���� ��� ������� �������� */
    DatePicker mFirstDatePicker;

    /** ���� ��� ������� �������� */
    DatePicker mSecondDatePicker;

    MenuTree.DateTimeRangeType mRangeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_send_date_time_range_value);

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

        //��� ����
        try {
            int type_number = -1;
            type_number = Integer.parseInt(pMap.get("DateTimeRangeType"));
            mRangeType = MenuTree.DateTimeRangeType.values()[type_number];
        } catch (EnumConstantNotPresentException e) {
            e.printStackTrace();
            Notifications.showError(mContext, "��������� ��������� ������ - �� ������� ������� ��� ���������." +
                    "���������� � ������ ���������");
            Logging.v("�� ������� ������� ��� ���������");
            ((Activity) mContext).finish();
        }

        // ��������� ������� ������� � ���
        setPickers();

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

        // ��������� � ������������ ��������� ���/���-�������
        String datetime_range_error_text = pMap.get("DateTimeErrorText");
        if (!TextUtils.isEmpty(datetime_range_error_text.trim())) {
            mDateTimeRangeErrorText = datetime_range_error_text;
        }

        // �������� ��� ����
        ImageView mImage = (ImageView) findViewById(R.id.image);
        File imageFile = new File(pMap.get("SelectedImage"));
        if (imageFile.exists()) {
            mImage.setImageBitmap(BitmapFactory.decodeFile(imageFile
                    .getAbsolutePath()));
        }

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
     * ��������� ������� ������� � ���.
     * � ����������� �� {@link com.isosystem.smarthouse.data.MenuTree.DateTimeRangeType}
     * ���������� �������� ��� ������ ��������� ��������.
     * ���� �������� ������� - �������� �������� ��� ��������� ����
     * ���� �������� ��� - �������� �������� ��� ��������� �������
     * ���� �������� ��� � ������� - ���������� ��
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
     * ��������� ��� ������ ������� �������� ������ ����� ��������� �������
     * ����� ����������� ����. ����������: 1. ���������� �������� � �������
     * ������� 2. �������� ��������� ������������� �������� 3. ������� ��������
     * �� ����������
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
                    Notifications.showError(mContext,"���������� ����� ��� ������� �������� ������� ������� �����������");
                    return;
                } else if (second_minute > 59 || second_minute < 0) {
                    Notifications.showError(mContext,"���������� ����� ��� ������� �������� ������� ������� �����������");
                    return;
                } else if (first_hour > 23 || first_hour < 0) {
                    Notifications.showError(mContext,"���������� ����� ��� ������� �������� ������� ������� �����������");
                    return;
                } else if (second_hour > 23 || second_hour < 0){
                    Notifications.showError(mContext,"���������� ����� ��� ������� �������� ������� ������� �����������");
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
                    Notifications.showError(mContext,"�������� ��� ��� ������ ���� ������� �����������");
                    return;
                } else if (first_month > 12 || first_month < 1) {
                    Notifications.showError(mContext,"�������� ������ ��� ������ ���� ������� �����������");
                    return;
                } else if (first_year < 1900 || first_year > 2999) {
                    Notifications.showError(mContext,"�������� ���� ��� ������ ���� ������� �����������");
                    return;
                } else if (second_day > 31 || second_day < 1) {
                    Notifications.showError(mContext,"�������� ��� ��� ������ ���� ������� �����������");
                    return;
                } else if (second_month > 12 || second_month < 1) {
                    Notifications.showError(mContext,"�������� ������ ��� ������ ���� ������� �����������");
                    return;
                } else if (second_year < 1900 || second_year > 2999) {
                    Notifications.showError(mContext,"�������� ���� ��� ������ ���� ������� �����������");
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
                Notifications.showError(mContext,"��������� ���������� ������ (��� ��������� ������ �����������). ���������� � ������ ���������.");
                return;
            }

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
     * ����� ������������ �������� ��������� ���/�������-���
     * ������ �������� ������ ���� ������ �� �������, ��� ������
     *
     * @return ��������� ��������. True ���� �������� ������ �������
     */
    private Boolean checkDateTimeRange(){
        if (mRangeType == MenuTree.DateTimeRangeType.TimeRange) {
            // ���� �������� �������, �� �� ����������
            return true;
        } else if (mRangeType == MenuTree.DateTimeRangeType.DateRange) {
            // ���� �������� ���, �� ���������� ������ ����
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

            // ���� ���� ��������� � checkTime = true, ���������� ����
            if (date1.equals(date2)) {
                return checkTime ? checkTimeRange() : true;
            }
            else if (date1.before(date2)) {
                return true;
            } else return false;
        } catch (ParseException e) {
            Logging.v("���������� ��� ������� �������� � ��������� ���� ��� �� datepicker`��");
            e.printStackTrace();
            Notifications.showError(mContext,"���������� ������ ��� ������� �������� ����");
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

            // ���� ���� ��������� � checkTime = true, ���������� ����
            if (time1.equals(time2) || time1.before(time2)) {
                return true;
            } else return false;
        } catch (ParseException e) {
            Logging.v("���������� ��� ������� �������� � ��������� ���� ��������� ������� �� datepicker`��");
            e.printStackTrace();
            Notifications.showError(mContext,"���������� ������ ��� ������� �������� ����");
        }
        return false;
    }

    class ValueMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // �������� ���������. ��� ������ ���� ���������� �������� � ������
            // ����������� ������ ����� �������
            String msg = intent.getStringExtra("message");

            if (msg.length() < 3) {
                Logging.v("�������� ������ ���������");
                return;
            }
            msg = msg.substring(2);

            String first_value;
            String second_value;

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

            if (mRangeType == MenuTree.DateTimeRangeType.TimeRange) {
                int first_hour;
                int first_minute;
                int second_hour;
                int second_minute;

                // ������ ���� � ������
                try {
                    first_hour = Integer.parseInt(first_value.split(":")[0]);
                    first_minute = Integer.parseInt(first_value.split(":")[1]);
                    second_hour = Integer.parseInt(second_value.split(":")[0]);
                    second_minute = Integer.parseInt(second_value.split(":")[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Notifications.showError(mContext, "������ ��� ��������� ��������� ������� �� �����������");
                    Logging.v("���������� ��� ������� ������� ����� �� ��������� �����������");
                    return;
                }

                // �������� ����� � �����
                if (first_minute > 59 || first_minute < 0){
                    Notifications.showError(mContext,"������ ��� ��������� ��������� ������� �� �����������");
                    return;
                } else if (second_minute > 59 || second_minute < 0) {
                    Notifications.showError(mContext,"������ ��� ��������� ��������� ������� �� �����������");
                    return;
                } else if (first_hour > 23 || first_hour < 0) {
                    Notifications.showError(mContext,"������ ��� ��������� ��������� ������� �� �����������");
                    return;
                } else if (second_hour > 23 || second_hour < 0){
                    Notifications.showError(mContext,"������ ��� ��������� ��������� ������� �� �����������");
                    return;
                }

                // ��������� ������� � ���������
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

                // ������ ����
                try {
                    first_day = Integer.parseInt(first_value.split("/", 3)[0]);
                    first_month = Integer.parseInt(first_value.split("/", 3)[1]);
                    first_year = Integer.parseInt(first_value.split("/", 3)[2]);

                    second_day = Integer.parseInt(second_value.split("/", 3)[0]);
                    second_month = Integer.parseInt(second_value.split("/", 3)[1]);
                    second_year = Integer.parseInt(second_value.split("/", 3)[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                    Notifications.showError(mContext, "������ ��� ��������� ��������� ���� �� �����������");
                    Logging.v("���������� ��� ������� ������� ���� �� ��������� �����������");
                    return;
                }

                if (first_day > 31 || first_day < 1) {
                    Notifications.showError(mContext,"�������� ��� ��� ������ ���� ������� �����������");
                    return;
                } else if (first_month > 12 || first_month < 1) {
                    Notifications.showError(mContext,"�������� ������ ��� ������ ���� ������� �����������");
                    return;
                } else if (first_year < 1900 || first_year > 2999) {
                    Notifications.showError(mContext,"�������� ���� ��� ������ ���� ������� �����������");
                    return;
                } else if (second_day > 31 || second_day < 1) {
                    Notifications.showError(mContext,"�������� ��� ��� ������ ���� ������� �����������");
                    return;
                } else if (second_month > 12 || second_month < 1) {
                    Notifications.showError(mContext,"�������� ������ ��� ������ ���� ������� �����������");
                    return;
                } else if (second_year < 1900 || second_year > 2999) {
                    Notifications.showError(mContext,"�������� ���� ��� ������ ���� ������� �����������");
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

                // ������ ���� � �����
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
                    Notifications.showError(mContext, "������ ��� ��������� ��������� ���� �� �����������");
                    Logging.v("���������� ��� ������� ������� ���� �� ��������� �����������");
                    return;
                }

                // �������� ����� � �����
                if (first_minute > 59 || first_minute < 0){
                    Notifications.showError(mContext,"������ ��� ��������� ��������� ������� �� �����������");
                    return;
                } else if (second_minute > 59 || second_minute < 0) {
                    Notifications.showError(mContext,"������ ��� ��������� ��������� ������� �� �����������");
                    return;
                } else if (first_hour > 23 || first_hour < 0) {
                    Notifications.showError(mContext,"������ ��� ��������� ��������� ������� �� �����������");
                    return;
                } else if (second_hour > 23 || second_hour < 0){
                    Notifications.showError(mContext,"������ ��� ��������� ��������� ������� �� �����������");
                    return;
                } else if (first_day > 31 || first_day < 1) {
                    Notifications.showError(mContext,"�������� ��� ��� ������ ���� ������� �����������");
                    return;
                } else if (first_month > 12 || first_month < 1) {
                    Notifications.showError(mContext,"�������� ������ ��� ������ ���� ������� �����������");
                    return;
                } else if (first_year < 1900 || first_year > 2999) {
                    Notifications.showError(mContext,"�������� ���� ��� ������ ���� ������� �����������");
                    return;
                } else if (second_day > 31 || second_day < 1) {
                    Notifications.showError(mContext,"�������� ��� ��� ������ ���� ������� �����������");
                    return;
                } else if (second_month > 12 || second_month < 1) {
                    Notifications.showError(mContext,"�������� ������ ��� ������ ���� ������� �����������");
                    return;
                } else if (second_year < 1900 || second_year > 2999) {
                    Notifications.showError(mContext,"�������� ���� ��� ������ ���� ������� �����������");
                    return;
                }

                mFirstDatePicker.updateDate(first_year,first_month-1,first_day);
                mSecondDatePicker.updateDate(second_year,second_month-1,second_day);

                mFirstTimePicker.setCurrentHour(first_hour);
                mFirstTimePicker.setCurrentMinute(first_minute);
                mSecondTimePicker.setCurrentHour(second_hour);
                mSecondTimePicker.setCurrentMinute(second_minute);
            } else {
                Notifications.showError(mContext,"��������� ���������� ������ (��� ��������� ������ �����������). ���������� � ������ ���������.");
                return;
            }
        }
    }
}