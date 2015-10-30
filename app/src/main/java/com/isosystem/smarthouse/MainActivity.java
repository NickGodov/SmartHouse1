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
 * Класс главного activity приложения.
 * <br>
 * Данный класс отвечает за три фрагмента клиентской части
 * (Сообщения - Меню (дефолтный фрагмент) - Окна вывода)
 * <br>
 * Класс содержит:<br>
 * 1. Настройки для полноэкранного режима и для запрета спящего режима устройства <br>
 * 2. Загрузку меню, окон вывода, создание папок на sdcard (загрузка сообщений вынесена в класс Application
 * для возможности старта приложения при старте планшета) <br>
 * 3. Реализацию хранителя экрана <br>
 * 4. ViewPager для фрагментов <br>
 * <p/>
 * Хранитель экрана работает следующим образом:
 * 1. Определен Handler {@link #mScreenSaverHandler} и Runnable {@link #mScreenSaverRunnable},
 * который запускается с задержкой (postDelayed). Задержка как раз и есть время бездействия.
 * 2. При паузе активити выполнение Runnable отменяется.
 * 3. При взаимодействии пользователя с планшетом {@link #onUserInteraction()},
 * Runnable перезапускается (отменяется и запускается снова с задержкой).
 * 4. Когда время бездействия истекло, запускается Runnable (срабатывает хранитель),
 * где определяется выставленный режим хранителя, сравнивает текущее время с установленным
 * в настройках временем, и запускается слайд-шоу
 * {@link com.isosystem.smarthouse.utils.ScreenSaverActivity} или затеменение
 * {@link com.isosystem.smarthouse.utils.ScreenDimActivity}
 */

public class MainActivity extends Activity {

    /**
     * Пейджер для фрагментов
     */
    ViewPager mViewPager;
    /**
     * Адаптер для пейджера для фрагментов
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * Ссылка на объект класса приложения
     */
    MyApplication mApplication;

    /**
     * Ссылка на сам активити
     */
    Activity mThisActivity;

    /**
     * Handler для хранителя экрана
     */
    Handler mScreenSaverHandler;
    /**
     * Использовать ли хранитель экрана (данные берутся из настроек
     */
    Boolean mUseScreenSaver = false;
    /**
     * Время бездействия (В СЕКУНДАХ) по истечении которого запускается хранитель экрана
     */
    int mScreenSaverIdleTime;

    /**
     * При старте активити:
     * <p/>
     * 1. Запрет спящего режима
     * 2. Установка полноэкранного режима
     * 3. Загрузка данных см. {@link #loadExternalData()}
     * 4. Настройка Viewpager для фрагментов
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThisActivity = this;
        mApplication = (MyApplication) getApplicationContext();

        setFullScreen();

        // Загрузка меню и окон вывода из файлов в /data/data
        loadExternalData();

        setContentView(R.layout.activity_main);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Страницы пейджера:
        // 0) Сообщения
        // 1) Главное меню
        // 2) Окна форматированного вывода
        // Дефолтная страница пейджера - 0 *1* 2
        mViewPager.setCurrentItem(1);
    }

    /**
     * В данном метода осуществляется загрузка меню и окон вывода из
     * внутренного хранилища
     */
    private void loadExternalData() {
        //Загрузка меню
        if (!mApplication.mProcessor.loadMenuTreeFromInternalStorage()) {
            Logging.v("Загрузка меню из внутреннего хранилища прошла с ошибкой");
        }

        //Загрузка окон вывода
        if (!mApplication.mProcessor.loadFscreensFromInternalStorage()) {
            Logging.v("Загрузка окон вывода из внутреннего хранилища прошла с ошибкой");
        }
    }

    /**
     * Проверка и создание папок на sdcard, см. {@link #checkExternalDirectoryStructure()}
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
            // Запрет на отключение экрана
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
            // Включение полноэкранного режим планшета
            if (getActionBar() != null) {
                getActionBar().hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Данный метод осуществляет проверку и, в случае необходимости,
     * создание папок на sdcard, а также добавляет несколько картинок в папку с изображениями.
     * Названия папок хранятся в {@link com.isosystem.smarthouse.Globals}
     * По-умолчанию, структура папок на внешнем устройстве следующая:
     * smarthouse - корневая папка
     * smarthouse/images/ - папка, где хранятся изображения для плиток и для окон установки значений
     * smarthouse/screensaver/ - папка, где хранятся изображения для слайд-шоу
     * smarthouse/logs/ - папка, где хранятся логи
     * <p/>
     * Добавление нескольких картинок происходит в методе {@link #copyImagesFromAssetsToExternalDirectory()}
     */
    private void checkExternalDirectoryStructure() {
        String state = Environment.getExternalStorageState();
        // Если sdcard примонтирована
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            File externalFilesDir = Environment.getExternalStorageDirectory();
            // Папка sdcard/smarthouse
            File externalRootDirectory = new File(externalFilesDir + File.separator
                    + Globals.EXTERNAL_ROOT_DIRECTORY);
            externalRootDirectory.mkdirs();

            // Папка sdcard/smarthouse/images
            File externalImagesDirectory = new File(externalFilesDir
                    + File.separator + Globals.EXTERNAL_ROOT_DIRECTORY
                    + File.separator + Globals.EXTERNAL_IMAGES_DIRECTORY);
            externalImagesDirectory.mkdirs();

            // Добавление нескольких дефолтных картинок
            copyImagesFromAssetsToExternalDirectory();

            // Папка sdcard/smarthouse/logs
            File externalLogsDirectory = new File(externalFilesDir + File.separator
                    + Globals.EXTERNAL_ROOT_DIRECTORY + File.separator
                    + Globals.EXTERNAL_LOGS_DIRECTORY);
            externalLogsDirectory.mkdirs();

            // Папка sdcard/smarthouse/screensaver
            File externalSSDirectory = new File(externalFilesDir + File.separator
                    + Globals.EXTERNAL_ROOT_DIRECTORY + File.separator
                    + Globals.EXTERNAL_SCREENSAVER_IMAGES_DIRECTORY);
            externalSSDirectory.mkdirs();
        }
    }

    /**
     * Данный метод копирует несколько изображений из папки assets/imgs в папку
     * images. Изображения используются для галереи при добавлении окон
     * управления. Т.к. изображение является обязательным полем для добавления
     * окна, необходимо предоставить несколько изображений и проверять их
     * наличие при запуске программы.
     * <p/>
     * Название папки в assets и папки, куда добавляются изображения указаны в {@link com.isosystem.smarthouse.Globals}
     */
    private void copyImagesFromAssetsToExternalDirectory() {

        AssetManager assetManager = getAssets();

        // Список изображений в assets/imgs
        String[] imagesFilesList = null;

        // Получаем список файлов
        try {
            imagesFilesList = assetManager
                    .list(Globals.ASSETS_IMAGES_DIRECTORY);
        } catch (IOException e) {
            Logging.v("Исключение при получении списка изображений в папке assets/imgs");
            e.printStackTrace();
        }

        if (imagesFilesList == null) {
            return;
        }

        // Путь к изображениям на внешнем хранилище
        String imagesExternalDirectory = Environment
                .getExternalStorageDirectory()
                + File.separator
                + Globals.EXTERNAL_ROOT_DIRECTORY
                + File.separator
                + Globals.EXTERNAL_IMAGES_DIRECTORY;

        // Копируем каждое найденное изображение из assets/imgs в images
        for (String imageFile : imagesFilesList) {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            // Пытаемся копировать
            try {

                // Новый файл, куда будет писаться файл из assets\imgs
                File outputFile = new File(imagesExternalDirectory, imageFile);

                // Если файл уже существует, пропускаем итерацию цикла
                if (outputFile.exists()) {
                    continue;
                }

                // Создаем входной поток
                inputStream = assetManager.open(Globals.ASSETS_IMAGES_DIRECTORY
                        + File.separator + imageFile);

                // Создаем выходной поток
                outputStream = new FileOutputStream(outputFile);

                // Копируем файл
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }

                // Закрываем и обнуляем потоки
                inputStream.close();
                inputStream = null;
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                Logging.v("Исключение при попытке скопировать "
                        + imageFile + " из assets/imgs в images");
                e.printStackTrace();
            }
        } //end for
    } // end method

    /**
     * Происходит считывание из настроек следующих параметров:
     * 1. Нужно ли использовать хранитель экрана
     * 2. Время бездействия для появления хранителя экрана
     * 3. Установка задержка для запуска {@link #mScreenSaverRunnable} в
     * {@link #mScreenSaverHandler}
     */
    @Override
    protected void onResume() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mApplication);

        // Проверка использования хранителя экрана
        mUseScreenSaver = prefs.getBoolean("enable_screen_saver", false);

        // Считывание времени бездействия и запуск Handler`а
        if (mUseScreenSaver) {
            mScreenSaverIdleTime = Integer.parseInt(prefs.getString(
                    "screen_saver_idle_time", "25"));

            mScreenSaverHandler = new Handler();
            // mScreenSaverIdleTime устанавливается в секундах, поэтому * 1000
            mScreenSaverHandler.postDelayed(mScreenSaverRunnable,
                    mScreenSaverIdleTime * 1000);
        }
        super.onResume();
    }

    /**
     * Удаление {@link #mScreenSaverRunnable} из {@link #mScreenSaverHandler}
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
     * Запуск хранителя экрана, если сработал счетчик бездействия:
     * 1) Определяется количество режимов хранителя экрана (слайд-шоу И/ИЛИ затемнение)
     * 2) Если два режима, то определить какой режим активен в данный момент:
     * - считать из настроек время работы затемнения - определить по текущему времени,
     * активно ли затемнение
     * 3) В зависимости от режима хранителя, запуск слайд-шоу или затемнение
     * <p/>
     * Более подробное описание вычисления режимов и запуск соответствующего режима приведено внутри
     * метода
     */
    private Runnable mScreenSaverRunnable = new Runnable() {
        public void run() {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mApplication);

            // FALSE, если затемнение
            // TRUE, если слайд-шоу
            Boolean screenSaverMode = true;

            // Используется ли затемнение экрана
            Boolean useScreenDim = prefs.getBoolean("enable_screen_dim", false);

            // Если используется затемнение,
            // необходимо определить наступило ли время работы режима
            if (useScreenDim) {
                String time = prefs.getString("screen_dim_enable_time",
                        "19:00-8:00");

                // Парсинг и конца работы
                // Например, 19:00-8:00 разбивается на [19:00] и [8:00]
                String[] time_period = time.split("-");

                // Парсинг значения начала работы
                // Например 19:00 разбивается на [19] и [00]
                String hour_start = time_period[0].split(":")[0];
                String minute_start = time_period[0].split(":")[1];

                // Парсинг значения начала работы
                // Например 8:00 разбивается на [8] и [00]
                String hour_end = time_period[1].split(":")[0];
                String minute_end = time_period[1].split(":")[1];

                // Получение временного промежутка в виде целых числа типа 1800
                // Например 19:45 преобразовывается в 1945
                int start_time = Integer.parseInt(hour_start) * 100
                        + Integer.parseInt(minute_start);
                int end_time = Integer.parseInt(hour_end) * 100
                        + Integer.parseInt(minute_end);

                // Получение текущего времени в виде целого числа типа 1800
                SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
                int current_time = Integer.parseInt(sdf.format(Calendar
                        .getInstance().getTime()));

                // Необходимо сравнить текущее время и время для затемнения,
                // чтобы определить - какой вариант хранителя использовать.
                // Если временной промежуток задан в течение одного дня
                // "с 10 до 18", тогда current_time должно быть
                // больше start_time, но меньше end_time
                // Если промежуток "с 19 до 12", тогда current_time должно быть
                // или больше start_time, или меньше end_time
                // Если старт и конец работы совпадают - круглосуточно действует
                // затемнение
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
                // Слайд-шоу
                Intent i = new Intent(mThisActivity, ScreenSaverActivity.class);
                startActivity(i);
            } else {
                // Затемнение экрана
                Intent i = new Intent(mThisActivity, ScreenDimActivity.class);
                startActivity(i);
            }
        }
    };

    // endregion

    /**
     * При взаимодействии пользователя с планшетом, обновление счетчика бездействия
     */
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (mUseScreenSaver) {
            // Обновляем счетчик бездействия
            mScreenSaverHandler.removeCallbacks(mScreenSaverRunnable);
            mScreenSaverHandler.postDelayed(mScreenSaverRunnable,
                    mScreenSaverIdleTime * 1000);
        }
    }

    /**
     * Адаптер для Viewpager. Возвращает объект фрагмента по номеру и выдает общее количество
     * фрагментов
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
                Logging.v("Исключение при возвращении объекта фрагмента. Входной параметр "
                        + String.valueOf(position)
                        + " вышел за пределы количества фрагментов");
                return new MainMessagesFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    } // end class SectionsPagerAdapter
}
