package com.isosystem.smarthouse;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// �������� ��� ���� ���������������� ������

public class MainFormattedScreensFragment extends Fragment {

    public MainFormattedScreensFragment() {
    }

    // ���������, ���� ���� ������� ����������� �������
    static ListView mMenuListView;
    // ���������, ���� ���� ������� ����������� ��������
    static GridView mMenuGridView;

    static View rootView;
    MyApplication mApplication;

    Globals.ConnectionMode connectionMode = Globals.ConnectionMode.USB;

    // ��� ��������� ���������
    AlarmMessageReceiver mReceiver;

    // ��������� ��� ������ ����������� �� USB
    ImageView mUsbConnectedIcon;
    // ��������� ��� ������ �������
    ImageView mPowerSupplyIcon;

    // ���������� ���������
    TextView mMessagesNumber;
    // ������ ��� ���������
    ImageView mMessagesIcon;

    // ������� �����������
    Drawable mDefaultBackground;
    // ������������ �� ����� ���� ���������������� ������
    // �������� ��� �������
    static Boolean mTileMode = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mApplication = (MyApplication) container.getContext()
                .getApplicationContext();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // ���������� ������ ����������� ���� (������, ������)
        mTileMode = prefs.getBoolean("use_formatted_screens_tile", true);
        if (mTileMode) {
            rootView = inflater.inflate(R.layout.fragment_main_formscreens,
                    container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_main_formscreens_list,
                    container, false);
        }

        mUsbConnectedIcon = (ImageView) rootView
                .findViewById(R.id.image_usb_connection);
        checkUsbConnectionIcon();

        mPowerSupplyIcon = (ImageView) rootView
                .findViewById(R.id.image_power_connection);
        checkPowerSupplyIcon();

        // ������ ��� ���������� ���������
        mMessagesIcon = (ImageView) rootView.findViewById(R.id.imageView3);

        // ��������� ������� ��� ���������� ���������
        mMessagesNumber = (TextView) rootView.findViewById(R.id.textView1);
        mMessagesNumber.setTypeface(Typeface.createFromAsset(rootView.getContext().getAssets(),
                "fonto.ttf"));
        mMessagesNumber.setTextColor(Color.BLACK);

        // ��������� ������ � �������
        setMessageNumberIcon();

        // ������� "���������"
        TextView textView = (TextView) rootView.findViewById(R.id.menuheader_text);
        Typeface font = Typeface.createFromAsset(rootView.getContext().getAssets(),
                "russo.ttf");
        textView.setTypeface(font);
        textView.setTextColor(Color.parseColor("white"));
        textView.setTextSize(35.0f);
        textView.setText("�������������� ����");
        textView.setGravity(Gravity.CENTER);
        textView.invalidate();

        mDefaultBackground = rootView.getBackground();

        if (mTileMode) {
            mMenuGridView = (GridView) rootView.findViewById(R.id.formscreenlist);
        } else {
            mMenuListView = (ListView) rootView.findViewById(R.id.formscreenlist);
        }

        // �������� ���� ���� ������
        reloadListViewMenu();

        // ������� �����, URL
        textView = (TextView) rootView.findViewById(R.id.mfl_url);
        font = Typeface.createFromAsset(rootView.getContext()
                .getAssets(), "code.otf");
        textView.setTypeface(font);
        textView.setText(this.getString(R.string.company_url));
        textView.setTextSize(15.0f);
        textView.invalidate();

        // ������� �����, �������
        textView = (TextView) rootView.findViewById(R.id.mfl_phone);
        font = Typeface.createFromAsset(rootView.getContext()
                .getAssets(), "code.otf");
        textView.setTypeface(font);
        textView.setText(this.getString(R.string.company_phone));
        textView.setTextSize(15.0f);
        textView.invalidate();

        return rootView;
    }

    @Override
    public void onStart() {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // ��� �����������
        String connection_type = prefs.getString("connection_type", "1");
        if (connection_type.equals("0")) {
            connectionMode = Globals.ConnectionMode.WIFI;
        } else if (connection_type.equals("1")) {
            connectionMode = Globals.ConnectionMode.USB;
        }

        try {
            mReceiver = new AlarmMessageReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
            filter.addAction(Globals.BROADCAST_INTENT_POWER_SUPPLY_CHANGED);
            filter.addAction(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE);
            rootView.getContext().registerReceiver(mReceiver, filter);
            Logging.v("������������ ������� MainActivity");
        } catch (Exception e) {
            Logging.v("���������� ��� ������� ���������������� �������");
            e.printStackTrace();
        }

        // � ����������� �� ������ ����������� (������/������)
        // ���������� ��������� GridView/ListView
        if (mTileMode) {
            configureGridView();
        } else {
            configureListView();
        }

        // �������� ��������� �������
        checkPowerSupplyIcon();
        // �������� ��������� USB-�����������
        checkUsbConnectionIcon();
        // �������� ���������� ���������
        setMessageNumberIcon();



        // ���� � ���������� ���������� ������������� ���������� ����
        if (!prefs.getBoolean("use_default_main_fscreens_background", true)) {
            String filepath = prefs.getString("choose_main_fscreens_background", "no-image");
            // ���� ���������� ���� � �����������
            if (!filepath.equals("no-image")) {
                BitmapDrawable navigationBackground = new BitmapDrawable(filepath);
                // ���� ��� �������
                if (prefs.getBoolean("main_fscreens_background_tile", true)) {
                    navigationBackground.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                }
                rootView.setBackgroundDrawable(navigationBackground);
            }
        } else {
            rootView.setBackgroundDrawable(mDefaultBackground);
        }
        super.onStart();
    }

    /**
     * ��������� ������ GridView ��� ������
     */
    private void configureGridView() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // ���������� ������� � ����
        int num_columns = Integer.parseInt(prefs.getString("formatted_screens_tiles_in_row", "5"));
        mMenuGridView.setNumColumns(num_columns);

        // ����� ������������
        int stretch_mode = Integer.parseInt(prefs.getString("formatted_screens_tile_stretch_mode", "3"));
        mMenuGridView.setStretchMode(stretch_mode);

        // �������������� ����� ����� ��������
        int horizontal_spacing = Integer.parseInt(prefs.getString("formatted_screens_tile_horizontal_spacing", "1"));
        mMenuGridView.setHorizontalSpacing(horizontal_spacing);

        // ���� ������ ���������� �����, �� ���������� ���������� �������������� � ������������ ������
        // �����, ����� �������� �� ��������
        boolean square_spacing = prefs.getBoolean("formatted_screens_tile_align_vertical_spacing", true);
        if (square_spacing) {
            mMenuGridView.setVerticalSpacing(horizontal_spacing);
        } else {
            int vertical_spacing = Integer.parseInt(prefs.getString("formatted_screens_tile_vertical_spacing", "1"));
            mMenuGridView.setVerticalSpacing(vertical_spacing);
        }
    }

    /**
     * ��������� ������ ListView ��� ������
     */
    private void configureListView() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // ���������� ������� � ����
        int divider_height = Integer.parseInt(prefs.getString("formatted_screens_list_divider_height", "5"));
        boolean transparent_divider = prefs.getBoolean("formatted_screens_list_divider_transparent", true);

        if (transparent_divider) {
            ColorDrawable transparent = new ColorDrawable(android.R.color.transparent);
            mMenuListView.setDivider(transparent);
        } else {
            ColorDrawable div_color = new ColorDrawable(Color.LTGRAY);
            mMenuListView.setDivider(div_color);
        }

        mMenuListView.setDividerHeight(divider_height);

        int margins = Integer.parseInt(prefs.getString("formatted_screens_list_margins", "10"));

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mMenuListView
                .getLayoutParams();

        mlp.setMargins(margins, 5, margins, 5);

        mMenuListView.setLayoutParams(mlp);
    }

    /**
     * � ����������� �� ������� ����������� ����, ����������� ���������
     */
    public static void reloadListViewMenu() {
        if (mTileMode) {
            mMenuGridView
                    .setAdapter(new MainFormattedScreensAdapterGrid(rootView.getContext()));
        } else {
            mMenuListView
                    .setAdapter(new MainFormattedScreensAdapterList(rootView.getContext()));
        }
    }

    @Override
    public void onResume() {
        checkPowerSupplyIcon();
        checkUsbConnectionIcon();
        setMessageNumberIcon();

        reloadListViewMenu();
        super.onResume();
    }

    @Override
    public void onStop() {
        try {
            rootView.getContext().unregisterReceiver(mReceiver);
            Logging.v("����������� ������� MainActivity");
        } catch (Exception e) {
            Logging.v("���������� ��� ������� ���������� �������");
            e.printStackTrace();
        }
        super.onStop();
    }

    /**
     * �������� ��������� ������� � ��������� ��������������� ��������
     */
    private void checkPowerSupplyIcon() {
        if (isSupplyEnabled()) {
            mPowerSupplyIcon.setImageResource(R.drawable.tablet_power_on);
        } else {
            mPowerSupplyIcon.setImageResource(R.drawable.tablet_power_off);
        }
    }
    /**
     * �������� ��������� USB-����������� � ��������� ��������������� ��������
     */
    private void checkUsbConnectionIcon() {
        if (mApplication.isUsbConnected) {
            if (connectionMode == Globals.ConnectionMode.USB) {
                mUsbConnectedIcon.setImageResource(R.drawable.tablet_connection_on);
            } else if (connectionMode == Globals.ConnectionMode.WIFI) {
                mUsbConnectedIcon.setImageResource(R.drawable.tablet_wifi_on);
            }
        } else {
            if (connectionMode == Globals.ConnectionMode.USB) {
                mUsbConnectedIcon.setImageResource(R.drawable.tablet_connection_off);
            } else if (connectionMode == Globals.ConnectionMode.WIFI) {
                mUsbConnectedIcon.setImageResource(R.drawable.tablet_wifi_off);
            }
        }
    }

    /**
     * ��������, ���������� �� ������� � ��������
     *
     * @return ���������� �� �������
     */
    private Boolean isSupplyEnabled() {
        Intent intent = mApplication.registerReceiver(null, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        int plugged = 0;
        plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean result = (plugged != 0 && plugged != -1);
        return result;
    }

    /**
     * ��������� ������ ���������� ���������
     */
    private void setMessageNumberIcon() {

        try {
            // ���� ����� ��������� ��� - ������ ������
            if (mApplication.mAlarmMessages.mAlarmMessages.size() == 0) {
                mMessagesIcon.setVisibility(View.INVISIBLE);
                mMessagesNumber.setVisibility(View.INVISIBLE);
            } else {
                mMessagesIcon.setVisibility(View.VISIBLE);
                mMessagesNumber.setVisibility(View.VISIBLE);
                mMessagesNumber.setText(String
                        .valueOf(mApplication.mAlarmMessages.mAlarmMessages
                                .size()));
            }
        } catch (Exception e) {
            Logging.v("���������� ��� ������� ��������� onReceive � MainActivity");
            e.printStackTrace();
        }

        // ������ ������ ������ � ����������� �� ���������� ����
        // ����� ���������� ��������� �� �������� �� �����
        if (mMessagesNumber.getText().toString().length() == 1) {
            mMessagesNumber.setTextSize(25.0f);
        } else if (mMessagesNumber.getText().toString().length() == 2) {
            mMessagesNumber.setTextSize(23.0f);
        } else if (mMessagesNumber.getText().toString().length() == 3) {
            mMessagesNumber.setTextSize(21.0f);
        } else if (mMessagesNumber.getText().toString().length() == 4) {
            mMessagesNumber.setTextSize(19.0f);
        }

        mMessagesIcon.invalidate();
        mMessagesNumber.invalidate();
    }

    /**
     * �������������� �������� ���� ���������������� ������: <br />
     * 1. � ������� ����������� ��������� ���� ����� � ��������� �� �����������; <br />
     * 2. ���� ����� �������, �������� ��������� ������ � INT; <br />
     * 3. ��������� ������ ���� ���������������� ������. <br />
     * @param message ��������� �� �����������
     */
    private void forcedFormattedScreenStart(String message){
        String number = "";

        Pattern p = Pattern.compile("[0-9]+");
        Matcher m = p.matcher(message);
        while (m.find()) {
            number = m.group();
        }

        int screen_number = -1;
        try {
            screen_number = Integer.parseInt(number);
            if (screen_number >=0 && screen_number < mApplication.mFormattedScreens.mFormattedScreens.size()) {
                Intent intent = new Intent(getActivity(),
                        FormattedScreensActivity.class);
                // �������� ����� �������� ���� � FormatterScreenActivity
                intent.putExtra("formScreenIndex", screen_number);
                getActivity().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.flipin,R.anim.flipout);
            } else {
                //throw new NumberFormatException("format screen number is out of bounds");
            }
        } catch (NumberFormatException e) {
            Intent i = new Intent();
            String alarmMessage = "�������� ��������� � ���������������� ������";
            mApplication.mAlarmMessages.addAlarmMessage(
                    mApplication, alarmMessage,
                    Notifications.MessageType.ControllerMessage);
            // ������ ���������
            i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
            mApplication.sendBroadcast(i);

            Logging.v("���������� ��� ������� �������� ������ ���� ���������������� ������." +
                    "������ ��������: " + number);
            e.printStackTrace();
        }
    }

    class AlarmMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Globals.BROADCAST_INTENT_POWER_SUPPLY_CHANGED))
                checkPowerSupplyIcon();
            checkUsbConnectionIcon();
            setMessageNumberIcon();

            // ���� �������������� �������� ����, �������� ����� � �������� ���
            // extra � ���� ��������� �� �����������
            if (intent.getAction().equals(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE)) {
                String msg = intent.getStringExtra("message");
                //forcedFormattedScreenStart(msg);
            }
        }
    }
}