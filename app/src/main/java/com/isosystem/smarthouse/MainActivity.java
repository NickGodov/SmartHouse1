package com.isosystem.smarthouse;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.isosystem.smarthouse.connection.USBReceiveService;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.utils.ScreenDimActivity;
import com.isosystem.smarthouse.utils.ScreenSaverActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * ����� �������� activity ����������.
 * <br>
 * ������ ����� �������� �� ��� ��������� ���������� �����
 * (��������� - ���� (��������� ��������) - ���� ������)
 * <br>
 * ����� ��������:<br>
 * 1. ��������� ��� �������������� ������ � ��� ������� ������� ������ ���������� <br>
 * 2. �������� ����, ���� ������, �������� ����� �� sdcard (�������� ��������� �������� � ����� Application
 * ��� ����������� ������ ���������� ��� ������ ��������) <br>
 * 3. ���������� ��������� ������ <br>
 * 4. ViewPager ��� ���������� <br>
 * <p/>
 * ��������� ������ �������� ��������� �������:
 * 1. ��������� Handler {@link #mScreenSaverHandler} � Runnable {@link #mScreenSaverRunnable},
 * ������� ����������� � ��������� (postDelayed). �������� ��� ��� � ���� ����� �����������.
 * 2. ��� ����� �������� ���������� Runnable ����������.
 * 3. ��� �������������� ������������ � ��������� {@link #onUserInteraction()},
 * Runnable ��������������� (���������� � ����������� ����� � ���������).
 * 4. ����� ����� ����������� �������, ����������� Runnable (����������� ���������),
 * ��� ������������ ������������ ����� ���������, ���������� ������� ����� � �������������
 * � ���������� ��������, � ����������� �����-���
 * {@link com.isosystem.smarthouse.utils.ScreenSaverActivity} ��� �����������
 * {@link com.isosystem.smarthouse.utils.ScreenDimActivity}
 */

public class MainActivity extends Activity {

    /**
     * ������� ��� ����������
     */
    ViewPager mViewPager;
    /**
     * ������� ��� �������� ��� ����������
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * ������ �� ������ ������ ����������
     */
    MyApplication mApplication;

    /**
     * ������ �� ��� ��������
     */
    Activity mThisActivity;

    /**
     * Handler ��� ��������� ������
     */
    Handler mScreenSaverHandler;
    /**
     * ������������ �� ��������� ������ (������ ������� �� ��������
     */
    Boolean mUseScreenSaver = false;
    /**
     * ����� ����������� (� ��������) �� ��������� �������� ����������� ��������� ������
     */
    int mScreenSaverIdleTime;

    /**
     * ��� ������ ��������:
     * <p/>
     * 1. ������ ������� ������
     * 2. ��������� �������������� ������
     * 3. �������� ������ ��. {@link #loadExternalData()}
     * 4. ��������� Viewpager ��� ����������
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThisActivity = this;
        mApplication = (MyApplication) getApplicationContext();

        setFullScreen();

        // �������� ���� � ���� ������ �� ������ � /data/data
        loadExternalData();

        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // �������� ��������:
        // 0) ���������
        // 1) ������� ����
        // 2) ���� ���������������� ������
        // ��������� �������� �������� - 0 *1* 2
        mViewPager.setCurrentItem(1);
    }

    /**
     * � ������ ������ �������������� �������� ���� � ���� ������ ��
     * ����������� ���������
     */
    private void loadExternalData() {
        //�������� ����
        if (!mApplication.mProcessor.loadMenuTreeFromInternalStorage()) {
            Logging.v("�������� ���� �� ����������� ��������� ������ � �������");
        }

        //�������� ���� ������
        if (!mApplication.mProcessor.loadFscreensFromInternalStorage()) {
            Logging.v("�������� ���� ������ �� ����������� ��������� ������ � �������");
        }
    }

    /**
     * �������� � �������� ����� �� sdcard, ��. {@link #checkExternalDirectoryStructure()}
     */
    @Override
    protected void onStart() {
        checkExternalDirectoryStructure();

        setFullScreen();

        Intent i = new Intent(getApplicationContext(), USBReceiveService.class);
        startService(i);

        super.onStart();
    }

    private void setFullScreen() {
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // ������ �� ���������� ������
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            Process proc = null;
            String ProcID = "79"; //HONEYCOMB AND OLDER

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                ProcID = "42"; //ICS AND NEWER
            }

            try {
                proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "service call activity " + ProcID + " s16 com.android.systemui"});
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                proc.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ��������� �������������� ����� ��������
            if (getActionBar() != null) {
                getActionBar().hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ������ ����� ������������ �������� �, � ������ �������������,
     * �������� ����� �� sdcard, � ����� ��������� ��������� �������� � ����� � �������������.
     * �������� ����� �������� � {@link com.isosystem.smarthouse.Globals}
     * ��-���������, ��������� ����� �� ������� ���������� ���������:
     * smarthouse - �������� �����
     * smarthouse/images/ - �����, ��� �������� ����������� ��� ������ � ��� ���� ��������� ��������
     * smarthouse/screensaver/ - �����, ��� �������� ����������� ��� �����-���
     * smarthouse/logs/ - �����, ��� �������� ����
     * <p/>
     * ���������� ���������� �������� ���������� � ������ {@link #copyImagesFromAssetsToExternalDirectory()}
     */
    private void checkExternalDirectoryStructure() {
        String state = Environment.getExternalStorageState();
        // ���� sdcard ��������������
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File externalFilesDir = Environment.getExternalStorageDirectory();
            // ����� sdcard/smarthouse
            File externalRootDirectory = new File(externalFilesDir + File.separator
                    + Globals.EXTERNAL_ROOT_DIRECTORY);
            externalRootDirectory.mkdirs();

            // ����� sdcard/smarthouse/images
            File externalImagesDirectory = new File(externalFilesDir
                    + File.separator + Globals.EXTERNAL_ROOT_DIRECTORY
                    + File.separator + Globals.EXTERNAL_IMAGES_DIRECTORY);
            externalImagesDirectory.mkdirs();

            // ���������� ���������� ��������� ��������
            copyImagesFromAssetsToExternalDirectory();

            // ����� sdcard/smarthouse/logs
            File externalLogsDirectory = new File(externalFilesDir + File.separator
                    + Globals.EXTERNAL_ROOT_DIRECTORY + File.separator
                    + Globals.EXTERNAL_LOGS_DIRECTORY);
            externalLogsDirectory.mkdirs();

            // ����� sdcard/smarthouse/screensaver
            File externalSSDirectory = new File(externalFilesDir + File.separator
                    + Globals.EXTERNAL_ROOT_DIRECTORY + File.separator
                    + Globals.EXTERNAL_SCREENSAVER_IMAGES_DIRECTORY);
            externalSSDirectory.mkdirs();
        }
    }

    /**
     * ������ ����� �������� ��������� ����������� �� ����� assets/imgs � �����
     * images. ����������� ������������ ��� ������� ��� ���������� ����
     * ����������. �.�. ����������� �������� ������������ ����� ��� ����������
     * ����, ���������� ������������ ��������� ����������� � ��������� ��
     * ������� ��� ������� ���������.
     * <p/>
     * �������� ����� � assets � �����, ���� ����������� ����������� ������� � {@link com.isosystem.smarthouse.Globals}
     */
    private void copyImagesFromAssetsToExternalDirectory() {

        AssetManager assetManager = getAssets();

        // ������ ����������� � assets/imgs
        String[] imagesFilesList = null;

        // �������� ������ ������
        try {
            imagesFilesList = assetManager
                    .list(Globals.ASSETS_IMAGES_DIRECTORY);
        } catch (IOException e) {
            Logging.v("���������� ��� ��������� ������ ����������� � ����� assets/imgs");
            e.printStackTrace();
        }

        if (imagesFilesList == null) {
            return;
        }

        // ���� � ������������ �� ������� ���������
        String imagesExternalDirectory = Environment
                .getExternalStorageDirectory()
                + File.separator
                + Globals.EXTERNAL_ROOT_DIRECTORY
                + File.separator
                + Globals.EXTERNAL_IMAGES_DIRECTORY;

        // �������� ������ ��������� ����������� �� assets/imgs � images
        for (String imageFile : imagesFilesList) {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            // �������� ����������
            try {

                // ����� ����, ���� ����� �������� ���� �� assets\imgs
                File outputFile = new File(imagesExternalDirectory, imageFile);

                // ���� ���� ��� ����������, ���������� �������� �����
                if (outputFile.exists()) {
                    continue;
                }

                // ������� ������� �����
                inputStream = assetManager.open(Globals.ASSETS_IMAGES_DIRECTORY
                        + File.separator + imageFile);

                // ������� �������� �����
                outputStream = new FileOutputStream(outputFile);

                // �������� ����
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }

                // ��������� � �������� ������
                inputStream.close();
                inputStream = null;
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                Logging.v("���������� ��� ������� ����������� "
                        + imageFile + " �� assets/imgs � images");
                e.printStackTrace();
            }
        } //end for
    } // end method

    /**
     * ���������� ���������� �� �������� ��������� ����������:
     * 1. ����� �� ������������ ��������� ������
     * 2. ����� ����������� ��� ��������� ��������� ������
     * 3. ��������� �������� ��� ������� {@link #mScreenSaverRunnable} �
     * {@link #mScreenSaverHandler}
     */
    @Override
    protected void onResume() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // �������� ������������� ��������� ������
        mUseScreenSaver = prefs.getBoolean("enable_screen_saver", false);

        // ���������� ������� ����������� � ������ Handler`�
        if (mUseScreenSaver) {
            mScreenSaverIdleTime = Integer.parseInt(prefs.getString(
                    "screen_saver_idle_time", "25"));

            mScreenSaverHandler = new Handler();
            // mScreenSaverIdleTime ��������������� � ��������, ������� * 1000
            mScreenSaverHandler.postDelayed(mScreenSaverRunnable,
                    mScreenSaverIdleTime * 1000);
        }
        super.onResume();
    }

    /**
     * �������� {@link #mScreenSaverRunnable} �� {@link #mScreenSaverHandler}
     */
    @Override
    protected void onPause() {
        if (mScreenSaverHandler != null) {
            mScreenSaverHandler.removeCallbacks(mScreenSaverRunnable);
        }
        super.onPause();
    }

    // region Handler
    /**
     * ������ ��������� ������, ���� �������� ������� �����������:
     * 1) ������������ ���������� ������� ��������� ������ (�����-��� �/��� ����������)
     * 2) ���� ��� ������, �� ���������� ����� ����� ������� � ������ ������:
     * - ������� �� �������� ����� ������ ���������� - ���������� �� �������� �������,
     * ������� �� ����������
     * 3) � ����������� �� ������ ���������, ������ �����-��� ��� ����������
     * <p/>
     * ����� ��������� �������� ���������� ������� � ������ ���������������� ������ ��������� ������
     * ������
     */
    private Runnable mScreenSaverRunnable = new Runnable() {
        public void run() {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mApplication);

            // FALSE, ���� ����������
            // TRUE, ���� �����-���
            Boolean screenSaverMode = true;

            // ������������ �� ���������� ������
            Boolean useScreenDim = prefs.getBoolean("enable_screen_dim", false);

            // ���� ������������ ����������,
            // ���������� ���������� ��������� �� ����� ������ ������
            if (useScreenDim) {
                String time = prefs.getString("screen_dim_enable_time",
                        "19:00-8:00");

                // ������� � ����� ������
                // ��������, 19:00-8:00 ����������� �� [19:00] � [8:00]
                String[] time_period = time.split("-");

                // ������� �������� ������ ������
                // �������� 19:00 ����������� �� [19] � [00]
                String hour_start = time_period[0].split(":")[0];
                String minute_start = time_period[0].split(":")[1];

                // ������� �������� ������ ������
                // �������� 8:00 ����������� �� [8] � [00]
                String hour_end = time_period[1].split(":")[0];
                String minute_end = time_period[1].split(":")[1];

                // ��������� ���������� ���������� � ���� ����� ����� ���� 1800
                // �������� 19:45 ����������������� � 1945
                int start_time = Integer.parseInt(hour_start) * 100
                        + Integer.parseInt(minute_start);
                int end_time = Integer.parseInt(hour_end) * 100
                        + Integer.parseInt(minute_end);

                // ��������� �������� ������� � ���� ������ ����� ���� 1800
                SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
                int current_time = Integer.parseInt(sdf.format(Calendar
                        .getInstance().getTime()));

                // ���������� �������� ������� ����� � ����� ��� ����������,
                // ����� ���������� - ����� ������� ��������� ������������.
                // ���� ��������� ���������� ����� � ������� ������ ���
                // "� 10 �� 18", ����� current_time ������ ����
                // ������ start_time, �� ������ end_time
                // ���� ���������� "� 19 �� 12", ����� current_time ������ ����
                // ��� ������ start_time, ��� ������ end_time
                // ���� ����� � ����� ������ ��������� - ������������� ���������
                // ����������
                if (start_time < end_time && current_time >= start_time
                        && current_time <= end_time)
                    screenSaverMode = false;
                else if (start_time > end_time
                        && (current_time >= start_time || current_time <= end_time))
                    screenSaverMode = false;
                else if (start_time == end_time)
                    screenSaverMode = false;
            }

            if (screenSaverMode) {
                // �����-���
                Intent i = new Intent(mThisActivity, ScreenSaverActivity.class);
                startActivity(i);
            } else {
                // ���������� ������
                Intent i = new Intent(mThisActivity, ScreenDimActivity.class);
                startActivity(i);
            }
        }
    };

    // endregion

    /**
     * ��� �������������� ������������ � ���������, ���������� �������� �����������
     */
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (mUseScreenSaver) {
            // ��������� ������� �����������
            mScreenSaverHandler.removeCallbacks(mScreenSaverRunnable);
            mScreenSaverHandler.postDelayed(mScreenSaverRunnable,
                    mScreenSaverIdleTime * 1000);
        }
    }

    /**
     * ������� ��� Viewpager. ���������� ������ ��������� �� ������ � ������ ����� ����������
     * ����������
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new MainMessagesFragment();
            } else if (position == 1) {
                return new MainMenuFragment();
            } else if (position == 2) {
                return new MainFormattedScreensFragment();
            } else {
                Logging.v("���������� ��� ����������� ������� ���������. ������� �������� "
                        + String.valueOf(position)
                        + " ����� �� ������� ���������� ����������");
                return new MainMessagesFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    } // end class SectionsPagerAdapter
}
