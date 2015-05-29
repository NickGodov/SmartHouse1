package com.isosystem.smarthouse.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.data.MenuTree;
import com.isosystem.smarthouse.dialogs.FormulaCheckDialog;
import com.isosystem.smarthouse.dialogs.OutgoingRangeMessageCheckDialog;
import com.isosystem.smarthouse.dialogs.ValidationFormulaCheckDialog;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Класс, который отвечает за формирование
 * конечной точки "Диапазон числовых значений"
 * <p/>
 * Пользователь вводит данные в форму и нажимает кнопку "Добавить"
 */

@SuppressWarnings("deprecation")
public class AddMenuItemSendRangeDateTimeValue extends Activity {
    Context mContext;
    MyApplication mApplication;

    Spinner mTypeRangeSpinner;

    Gallery mGallery; // Виджет галереи
    ArrayList<String> mImages = null; // Массив с путями для изображений
    ImageView mGalleryPicker; //Пикер изображения галереи

    //Режим окна (редактирование или создание)
    boolean mEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_mainmenu_add_send_range_date_time_value);

        // Отмена затемнения экрана
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mApplication = (MyApplication) getApplicationContext();
        mContext = this;

        // Кнопка добавить
        Button addBtn = (Button) findViewById(R.id.btn_ok);
        addBtn.setOnClickListener(mAddListener);

        // Кнопка отменить
        Button backBtn = (Button) findViewById(R.id.btn_cancel);
        backBtn.setOnClickListener(mBackListener);

        // Изображение выбранной картинки из галереи
        mGalleryPicker = (ImageView) findViewById(R.id.tile_image);
        mGalleryPicker.setTag("");

        // Галерея изображения для экрана
        mGallery = (Gallery) findViewById(R.id.tile_image_gallery);
        mImages = getImages();
        mGallery.setAdapter(new GalleryAdapter(mImages, this));
        mGallery.setOnItemClickListener(galleryImageSelectListener);

        // Установка подсказок для кнопок "Подсказка"
        ImageButton mTooltipButton = (ImageButton) findViewById(R.id.button_help_header);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_range_type);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_description);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_first_error);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_second_error);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_get_value);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_outgoing_prefix);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        // Проверка исходящего сообщения
        ImageButton mOutgoingMessageDialogButton = (ImageButton) this
                .findViewById(R.id.button_check_outgoing_prefix);
        mOutgoingMessageDialogButton
                .setOnClickListener(outgoingMessageListener);

        // Спиннер с выбором типа диапазона
        // Изначально выбирается первый пункт
        mTypeRangeSpinner = (Spinner) findViewById(R.id.type_range_spinner);
        generateTypeRangeSpinner();
        mTypeRangeSpinner.setSelection(0);

        // Определение режима работы окна
        try {
            mEditMode = getIntent().getBooleanExtra("isEdited", false);
        } catch (Exception e) {
            Logging.v("Исключение при попытке считать режим работы окна");
            e.printStackTrace();
        }

        // Заполнение полей в режиме редактирования
        if (mEditMode) {
            setFieldValues();
        }
    }

    /**
     * Генерирует спиннер для выбора типа конечного окна
     */
    private void generateTypeRangeSpinner() {

        mTypeRangeSpinner.setAdapter(null);

        // Получаем список типов окон
        ArrayList<String> screenTypes = new ArrayList<String>();
        for (MenuTree.DateTimeRangeType type : MenuTree.DateTimeRangeType.values()) {
            screenTypes.add(type.toString());
        }

        ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, screenTypes);
        mTypeRangeSpinner.setAdapter(ad);
        ad.notifyDataSetChanged();
    }

    /**
     * Установка значений полей в режиме редактирования
     */
    private void setFieldValues() {
        HashMap<String, String> pMap = mApplication.mTree.tempNode.paramsMap;

        if (pMap == null) return;

        // Установка типа диапазона
        if (pMap.get("DateTimeRangeType") != null) {

            Integer type_position = 0;
            try {
                type_position = Integer.parseInt(pMap.get("DateTimeRangeType"));
            } catch (NumberFormatException e) {
                Logging.v("Ошибка при извлечении типа диапазона из узла меню");
                e.printStackTrace();
            }

            if ( mTypeRangeSpinner.getItemAtPosition(type_position) != null) {
                mTypeRangeSpinner.setSelection(type_position);
            } else {
                Logging.v("Извлеченный тип данных не найден в спиннере");
            }
        }

        // Установка названия
        EditText mHeaderText = (EditText) findViewById(R.id.header_text);
        if (pMap.get("HeaderText") != null)
            mHeaderText.setText(pMap.get("HeaderText"));

        // Установка описания
        EditText mDescText = (EditText) findViewById(R.id.description_text);
        if (pMap.get("DescriptionText") != null)
            mDescText.setText(pMap.get("DescriptionText"));

        // Сообщение при вводе невалидного значения
        EditText mInvalidFirstValueText = (EditText) findViewById(R.id.first_error_text);
        if (pMap.get("InvalidFirstValueText") != null)
            mInvalidFirstValueText.setText(pMap.get("InvalidFirstValueText"));

        // Сообщение при вводе невалидного значения
        EditText mInvalidSecondValueText = (EditText) findViewById(R.id.second_error_text);
        if (pMap.get("InvalidSecondValueText") != null)
            mInvalidSecondValueText.setText(pMap.get("InvalidSecondValueText"));

        if (pMap.get("SelectedImage") != null) {
            // Выбор изображения для пункта меню
            int pos = mImages.indexOf(pMap.get("SelectedImage"));

            // Если изображение было найдено
            // Выделяется иконка в галерее,
            // выставляется изображение в пикер
            if (pos != -1) {
                mGallery.setSelection(pos);
                Bitmap b = BitmapFactory.decodeFile(mImages.get(pos));
                mGalleryPicker.setImageBitmap(b);
                mGalleryPicker.setTag(mImages.get(pos));
            }
        }

        // Запрос текущего значения от контроллера
        EditText mGiveMeValueMessage = (EditText) findViewById(R.id.get_value_text);
        if (pMap.get("GiveMeValueMessage") != null)
            mGiveMeValueMessage.setText(pMap.get("GiveMeValueMessage"));

        // Префикс для отправки введенного значения
        EditText mOutgoingValueMessage = (EditText) findViewById(R.id.outgoing_prefix_text);
        if (pMap.get("OutgoingValueMessage") != null)
            mOutgoingValueMessage.setText(pMap.get("OutgoingValueMessage"));
    }

    /**
     * По клику на картинку галереи, устанавливается imagepicker справа
     */
    private OnItemClickListener galleryImageSelectListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position,
                                long id) {
            Bitmap b = BitmapFactory.decodeFile(mImages.get(position));
            mGalleryPicker.setImageBitmap(b);
            mGalleryPicker.setTag(mImages.get(position));
        }
    };

    /**
     * Проверка исходящего значения. Формула преобразования, формула валидации и префикс
     * передаются в диалог
     */
    private OnClickListener outgoingMessageListener = new OnClickListener() {
        // Проверка формулы для обработки входящего значения
        @Override
        public void onClick(final View v) {
            EditText mOutgoingValueFormula = (EditText) findViewById(R.id.outgoing_formula_text);
            EditText mOutgoingValueValidation = (EditText) findViewById(R.id.validation_formula_text);
            EditText mOutgoingPrefix = (EditText) findViewById(R.id.outgoing_prefix_text);
            EditText mOutgoingSecondValueFormula = (EditText) findViewById(R.id.second_outgoing_formula_text);
            EditText mOutgoingSecondValueValidation = (EditText) findViewById(R.id.validation_second_formula_text);

            OutgoingRangeMessageCheckDialog dialog = new OutgoingRangeMessageCheckDialog(
                    mOutgoingValueFormula.getText().toString(),
                    mOutgoingValueValidation.getText().toString(),
                    mOutgoingSecondValueFormula.getText().toString(),
                    mOutgoingSecondValueValidation.getText().toString(),
                    mOutgoingPrefix.getText().toString(),
                    AddMenuItemSendRangeDateTimeValue.this);

            dialog.show(getFragmentManager(), "Outgoing message check");
        }
    };

    /**
     * Слушатель для кнопок "Подсказка". При нажатии на кнопку показывается
     * Toast с подсказкой
     */
    private OnClickListener tooltipsButtonListener = new OnClickListener() {
        // Проверка формулы для обработки входящего значения
        @Override
        public void onClick(final View v) {
            String tooltip;
            switch (v.getId()) {
                // Тип диапазона
                case R.id.button_help_range_type:
                    tooltip = "Выберите один из типов диапазона. От этого будет зависеть внешний вид конечной точки и ожидаемое сообщение от контроллера";
                    break;
                // Заголовок
                case R.id.button_help_header:
                    tooltip = getResources().getString(R.string.header_text_tooltip);
                    break;
                // Описание
                case R.id.button_help_description:
                    tooltip = "Текст, который будет показываться под заголовком, формат - строка";
                    break;
                // Сообщение не прошло валидацию
                case R.id.button_help_first_error:
                    tooltip = "Сообщение, которое увидит пользователь, если первое введенное значение будет некорректным, формат - строка";
                    break;
                // Сообщение не прошло валидацию
                case R.id.button_help_second_error:
                    tooltip = "Сообщение, которое увидит пользователь, если второе введенное значение будет некорректным, формат - строка";
                    break;
                case R.id.button_help_get_value:
                    tooltip = "Сообщение, которое будет передано контроллеру при старте окна с требованием выслать текущее значение управляемого элемента. Сообщение передается без изменений";
                    break;
                case R.id.button_help_outgoing_prefix:
                    tooltip = "Сообщение со значениями, которое будет выслано контроллеру. Настройщик вводит префикс сообщения. При отсылке, программа возьмет пройденные валидацию значения и вышлет сообщение <[префикс],[значение1]-[значение2]>";
                    break;
                default:
                    tooltip = "Если вы видите это сообщение, сообщите разработчику об ошибке, указав ситуацию, при которой вы увидели это сообщение";
                    break;
            }
            // Вывод Toast с сообщением
            Notifications.showTooltip(mContext, tooltip);
        }
    };

    /**
     * Считывание путей к изображениям галереи
     */
    private ArrayList<String> getImages() {
        ArrayList<String> images = new ArrayList<String>();
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + Globals.EXTERNAL_ROOT_DIRECTORY + File.separator + Globals.EXTERNAL_IMAGES_DIRECTORY);

        if (file.isDirectory()) {
            File[] listFile = file.listFiles();
            for (File f : listFile) {
                images.add(f.getAbsolutePath());
            }
        }
        return images;
    }

    /**
     * Отмена добавления нового пункта
     */
    private void undoMenuItemAdding() {
        try {
            ((MyApplication) getApplicationContext()).mProcessor.loadMenuTreeFromInternalStorage();
        } catch (Exception e) {
            Logging.v("Исключение при попытке загрузить меню из файла");
            e.printStackTrace();
        }
    }

    /**
     * Добавление нового пункта меню
     */
    private boolean addNewMenuItem() {
        EditText mHeaderText = (EditText) findViewById(R.id.header_text);
        EditText mDescText = (EditText) findViewById(R.id.description_text);

        EditText mInvalidFirstValueText = (EditText) findViewById(R.id.first_error_text);
        EditText mInvalidSecondValueText = (EditText) findViewById(R.id.second_error_text);

        EditText mGiveMeValueMessage = (EditText) findViewById(R.id.get_value_text);
        EditText mOutgoingValueMessage = (EditText) findViewById(R.id.outgoing_prefix_text);

        ImageView mSelectedImage = (ImageView) findViewById(R.id.tile_image);

        // Проверка заполненности обязательных полей
        // Обязательные поля: 3 надписи и 3 сообщения + картинка
        if ((mHeaderText.getText().toString() == null) || (mHeaderText.getText().toString().trim().isEmpty())
                || (mDescText.getText().toString() == null) || (mDescText.getText().toString().trim().isEmpty())
                || (mInvalidFirstValueText.getText().toString() == null) || (mInvalidFirstValueText.getText().toString().trim().isEmpty())
                || (mInvalidSecondValueText.getText().toString() == null) || (mInvalidSecondValueText.getText().toString().trim().isEmpty())
                || (mGiveMeValueMessage.getText().toString() == null) || (mGiveMeValueMessage.getText().toString().trim().isEmpty())
                || mTypeRangeSpinner.isSelected()
                || (mOutgoingValueMessage.getText().toString() == null) || (mOutgoingValueMessage.getText().toString().trim().isEmpty())) {
            Notifications.showError(mContext,
                    "Не заполнены обазятельне поля (они отмечены *)");
            return false;
        }

        // Строковые значения (сообщения и надписи)
        HashMap<String, String> mParamsMap = new HashMap<String, String>();

        // Картинка для плитки
        if (mApplication.mTree.tempNode.paramsMap != null) {
            if (mApplication.mTree.tempNode.paramsMap.get("GridImage") != null) {
                mParamsMap.put("GridImage", mApplication.mTree.tempNode.paramsMap.get("GridImage"));
            }
        } // if !null

        // ID элемента

        mParamsMap.put("DateTimeRangeType", String.valueOf(mTypeRangeSpinner.getSelectedItemPosition()));

        mParamsMap.put("HeaderText", mHeaderText.getText().toString());

        // Сообщение контроллеру при входе
        mParamsMap.put("DescriptionText", mDescText.getText().toString());

        // Префикс входящего сообщения для установки значения
        mParamsMap.put("InvalidFirstValueText", mInvalidFirstValueText.getText()
                .toString());

        // Префикс входящего сообщения для установки значения
        mParamsMap.put("InvalidSecondValueText", mInvalidSecondValueText.getText()
                .toString());

        mParamsMap.put("GiveMeValueMessage", mGiveMeValueMessage.getText()
                .toString());

        mParamsMap.put("OutgoingValueMessage", mOutgoingValueMessage.getText()
                .toString());

        mParamsMap.put("SelectedImage", mSelectedImage.getTag().toString());

        // Добавление параметров
        mApplication.mTree.tempNode.paramsMap = mParamsMap;

        try {
            mApplication.mProcessor.saveMenuTreeToInternalStorage();
        } catch (Exception e) {
            Logging.v(getResources().getString(R.string.exception_reload_menu_tree));
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Кнопка "Добавить"
     */
    private OnClickListener mAddListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(
                    "Вы действительно хотите добавить новый пункт меню?")
                    .setPositiveButton("Добавить",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // Добавление нового узла
                                    boolean menuItemAdded = addNewMenuItem();
                                    if (menuItemAdded)
                                        finish();
                                }
                            })
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            }).create().show();
        }
    };

    /**
     * Кнопка "Отменить"
     */
    private OnClickListener mBackListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Вы действительно хотите выйти?")
                    .setPositiveButton("Выйти",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // Отмена добавления вершины
                                    undoMenuItemAdding();
                                    ((Activity) mContext).finish();
                                }
                            })
                    .setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            }).create().show();
        }
    };
}