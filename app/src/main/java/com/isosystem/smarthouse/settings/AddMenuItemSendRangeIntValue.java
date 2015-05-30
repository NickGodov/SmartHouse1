package com.isosystem.smarthouse.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.dialogs.FormulaCheckDialog;
import com.isosystem.smarthouse.dialogs.OutgoingMessageCheckDialog;
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
public class AddMenuItemSendRangeIntValue extends Activity {
    Context mContext;
    MyApplication mApplication;

    Gallery mGallery; // Виджет галереи
    ArrayList<String> mImages = null; // Массив с путями для изображений
    ImageView mGalleryPicker; //Пикер изображения галереи

    //Режим окна (редактирование или создание)
    boolean mEditMode;

    Button mAddButton;
    Button mBackButton;

    EditText mFirstValueLabel;
    EditText mSecondValueLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_mainmenu_add_send_range_int_value);

        // Отмена затемнения экрана
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mApplication = (MyApplication) getApplicationContext();
        mContext = this;

        mFirstValueLabel = (EditText) findViewById(R.id.first_value_textview_text);
        mSecondValueLabel = (EditText) findViewById(R.id.second_value_textview_text);

        // Кнопка добавить
        mAddButton = (Button) findViewById(R.id.btn_ok);
        mAddButton.setOnClickListener(mAddListener);
        // Кнопка отменить
        mBackButton = (Button) findViewById(R.id.btn_cancel);
        mBackButton.setOnClickListener(mBackListener);

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

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_description);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_first_error);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_first_value_textview);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_second_value_textview);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_second_error);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_first_incoming_formula);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_second_incoming_formula);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_first_decimal_places);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_second_decimal_places);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_first_outgoing_formula);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_second_outgoing_formula);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_first_validation_formula);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_second_validation_formula);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_get_value);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        mTooltipButton = (ImageButton) findViewById(R.id.button_help_outgoing_prefix);
        mTooltipButton.setOnClickListener(tooltipsButtonListener);

        // Проверка формулы первого входящего значения
        ImageButton mIncomingFirstValueFormulaDialogButton = (ImageButton) this
                .findViewById(R.id.button_check_first_incoming_formula);
        mIncomingFirstValueFormulaDialogButton
                .setOnClickListener(incomingFirstValueFormulaListener);

        // Проверка формулы второго входящего значения
        ImageButton mIncomingSecondValueFormulaDialogButton = (ImageButton) this
                .findViewById(R.id.button_check_second_incoming_formula);
        mIncomingSecondValueFormulaDialogButton
                .setOnClickListener(incomingSecondValueFormulaListener);

        // Проверка формулы первого исходящего значения
        ImageButton mOutgoingFirstValueFormulaDialogButton = (ImageButton) this
                .findViewById(R.id.button_check_first_outgoing_formula);
        mOutgoingFirstValueFormulaDialogButton
                .setOnClickListener(outgoingFirstValueFormulaListener);

        // Проверка валидности первого исходящего значения
        ImageButton mOutgoingFirstValidationDialogButton = (ImageButton) this
                .findViewById(R.id.button_check_first_validation_formula);
        mOutgoingFirstValidationDialogButton
                .setOnClickListener(outgoingFirstValueValidationListener);

        // Проверка формулы второго исходящего значения
        ImageButton mOutgoingSecondValueFormulaDialogButton = (ImageButton) this
                .findViewById(R.id.button_check_second_outgoing_formula);
        mOutgoingSecondValueFormulaDialogButton
                .setOnClickListener(outgoingSecondValueFormulaListener);

        // Проверка валидности второго исходящего значения
        ImageButton mOutgoingSecondValidationDialogButton = (ImageButton) this
                .findViewById(R.id.button_check_second_validation_formula);
        mOutgoingSecondValidationDialogButton
                .setOnClickListener(outgoingSecondValueValidationListener);

        // Проверка исходящего сообщения
        ImageButton mOutgoingMessageDialogButton = (ImageButton) this
                .findViewById(R.id.button_check_outgoing_prefix);
        mOutgoingMessageDialogButton
                .setOnClickListener(outgoingMessageListener);

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
     * Установка значений полей в режиме редактирования
     */
    private void setFieldValues() {
        HashMap<String, String> pMap = mApplication.mTree.tempNode.paramsMap;

        if (pMap == null) return;

        // Установка названия
        EditText mHeaderText = (EditText) findViewById(R.id.header_text);
        if (pMap.get("HeaderText") != null)
            mHeaderText.setText(pMap.get("HeaderText"));

        // Установка описания
        EditText mDescText = (EditText) findViewById(R.id.description_text);
        if (pMap.get("DescriptionText") != null)
            mDescText.setText(pMap.get("DescriptionText"));

        // Надпись для первого значения
        if (pMap.get("FirstValueLabel") != null)
            mFirstValueLabel.setText(pMap.get("FirstValueLabel"));

        // Надпись для второго значения
        if (pMap.get("SecondValueLabel") != null)
            mSecondValueLabel.setText(pMap.get("SecondValueLabel"));

        // Сообщение при вводе первого невалидного значения
        EditText mInvalidFirstValueText = (EditText) findViewById(R.id.first_error_text);
        if (pMap.get("InvalidFirstValueText") != null)
            mInvalidFirstValueText.setText(pMap.get("InvalidFirstValueText"));

        // Сообщение при вводе второго невалидного значения
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

        EditText mFirstIncomingValueFormula = (EditText) findViewById(R.id.incoming_formula_text);
        if (pMap.get("IncomingFirstValueFormula") != null)
            mFirstIncomingValueFormula.setText(pMap.get("IncomingFirstValueFormula"));
        EditText mFirstFractionDigits = (EditText) findViewById(R.id.decimal_places_text);
        if (pMap.get("FirstFractionDigits") != null)
            mFirstFractionDigits.setText(pMap.get("FirstFractionDigits"));

        EditText mSecondIncomingValueFormula = (EditText) findViewById(R.id.second_incoming_formula_text);
        if (pMap.get("IncomingSecondValueFormula") != null)
            mSecondIncomingValueFormula.setText(pMap.get("IncomingSecondValueFormula"));

        EditText mSecondFractionDigits = (EditText) findViewById(R.id.second_decimal_places_text);
        if (pMap.get("SecondFractionDigits") != null)
            mSecondFractionDigits.setText(pMap.get("SecondFractionDigits"));

        EditText mOutgoingFirstValueFormula = (EditText) findViewById(R.id.outgoing_formula_text);
        if (pMap.get("FirstOutgoingValueFormula") != null)
            mOutgoingFirstValueFormula.setText(pMap.get("FirstOutgoingValueFormula"));

        EditText mOutgoingFirstValueValidation = (EditText) findViewById(R.id.validation_formula_text);
        if (pMap.get("FirstOutgoingValueValidation") != null)
            mOutgoingFirstValueValidation.setText(pMap.get("FirstOutgoingValueValidation"));

        EditText mOutgoingSecondValueFormula = (EditText) findViewById(R.id.second_outgoing_formula_text);
        if (pMap.get("SecondOutgoingValueFormula") != null)
            mOutgoingSecondValueFormula.setText(pMap.get("SecondOutgoingValueFormula"));

        EditText mOutgoingSecondValueValidation = (EditText) findViewById(R.id.validation_second_formula_text);
        if (pMap.get("SecondOutgoingValueValidation") != null)
            mOutgoingSecondValueValidation.setText(pMap.get("SecondOutgoingValueValidation"));
        
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
                    AddMenuItemSendRangeIntValue.this);

            dialog.show(getFragmentManager(), "Outgoing message check");
        }
    };

    /**
     * Слушатель для проверки первой формулы для валидации значения. Передаем в диалог
     * формулу проверки исходящего значения и формулу валидации
     */
    private OnClickListener outgoingFirstValueValidationListener = new OnClickListener() {
        // Проверка формулы для обработки входящего значения
        @Override
        public void onClick(final View v) {
            EditText mOutgoingValueFormula = (EditText) findViewById(R.id.outgoing_formula_text);
            EditText mOutgoingValueValidation = (EditText) findViewById(R.id.validation_formula_text);

            ValidationFormulaCheckDialog dialog = new ValidationFormulaCheckDialog(
                    mOutgoingValueFormula.getText().toString(),
                    mOutgoingValueValidation.getText().toString());

            dialog.show(getFragmentManager(), "Outgoing value validation check");
        }
    };

    /**
     * Слушатель для проверки второй формулы для валидации значения. Передаем в диалог
     * формулу проверки исходящего значения и формулу валидации
     */
    private OnClickListener outgoingSecondValueValidationListener = new OnClickListener() {
        // Проверка формулы для обработки входящего значения
        @Override
        public void onClick(final View v) {
            EditText mOutgoingValueFormula = (EditText) findViewById(R.id.second_outgoing_formula_text);
            EditText mOutgoingValueValidation = (EditText) findViewById(R.id.validation_second_formula_text);

            ValidationFormulaCheckDialog dialog = new ValidationFormulaCheckDialog(
                    mOutgoingValueFormula.getText().toString(),
                    mOutgoingValueValidation.getText().toString());

            dialog.show(getFragmentManager(), "Outgoing value validation check");
        }
    };

    /**
     * Слушатель для проверки формулы для обработки первого входящего значения. Сначала
     * необходимо получить количество знаков после запятой После чего создать
     * новый диалог с введенной формулой и количеством знаков после запятой.
     */
    private OnClickListener incomingFirstValueFormulaListener = new OnClickListener() {
        // Проверка формулы для обработки входящего значения
        @Override
        public void onClick(final View v) {
            // Поле с введенным количеством знаков после запятой
            EditText mFractionDigits = (EditText) findViewById(R.id.decimal_places_text);
            // Значение поля формулы обработки входящего значения
            EditText mIncomingValueFormula = (EditText) findViewById(R.id.incoming_formula_text);

            // Открываем диалог проверки формулы. Передаем значение поля формулы
            // и количество знаков
            FormulaCheckDialog dialog = new FormulaCheckDialog(
                    mIncomingValueFormula.getText().toString(), mFractionDigits
                    .getText().toString());
            dialog.show(getFragmentManager(), "Incoming value formula check");
        }
    };

    /**
     * Слушатель для проверки формулы для обработки второго входящего значения. Сначала
     * необходимо получить количество знаков после запятой После чего создать
     * новый диалог с введенной формулой и количеством знаков после запятой.
     */
    private OnClickListener incomingSecondValueFormulaListener = new OnClickListener() {
        // Проверка формулы для обработки входящего значения
        @Override
        public void onClick(final View v) {
            // Поле с введенным количеством знаков после запятой
            EditText mFractionDigits = (EditText) findViewById(R.id.second_decimal_places_text);
            // Значение поля формулы обработки входящего значения
            EditText mIncomingValueFormula = (EditText) findViewById(R.id.second_incoming_formula_text);

            // Открываем диалог проверки формулы. Передаем значение поля формулы
            // и количество знаков
            FormulaCheckDialog dialog = new FormulaCheckDialog(
                    mIncomingValueFormula.getText().toString(), mFractionDigits
                    .getText().toString());
            dialog.show(getFragmentManager(), "Incoming value formula check");
        }
    };

    /**
     * Слушатель для проверки первой формулы для обработки исходящего значения. Создаем
     * диалог с введенной формулой. Т.к. нам нужно целое значение, то выставляем
     * второй параметра диалога в 0
     */
    private OnClickListener outgoingFirstValueFormulaListener = new OnClickListener() {
        // Проверка формулы для обработки входящего значения
        @Override
        public void onClick(final View v) {
            // Значение поля формулы обработки входящего значения
            EditText mOutgoingValueFormula = (EditText) findViewById(R.id.outgoing_formula_text);

            // Открываем диалог проверки формулы. Передаем значение поля формулы
            // и 0, т.к. нам нужно целое значение
            FormulaCheckDialog dialog = new FormulaCheckDialog(
                    mOutgoingValueFormula.getText().toString(), "0");
            dialog.show(getFragmentManager(), "Outgoing value formula check");
        }
    };

    /**
     * Слушатель для проверки второй формулы для обработки исходящего значения. Создаем
     * диалог с введенной формулой. Т.к. нам нужно целое значение, то выставляем
     * второй параметра диалога в 0
     */
    private OnClickListener outgoingSecondValueFormulaListener = new OnClickListener() {
        // Проверка формулы для обработки входящего значения
        @Override
        public void onClick(final View v) {
            // Значение поля формулы обработки входящего значения
            EditText mOutgoingValueFormula = (EditText) findViewById(R.id.second_outgoing_formula_text);

            // Открываем диалог проверки формулы. Передаем значение поля формулы
            // и 0, т.к. нам нужно целое значение
            FormulaCheckDialog dialog = new FormulaCheckDialog(
                    mOutgoingValueFormula.getText().toString(), "0");
            dialog.show(getFragmentManager(), "Outgoing value formula check");
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
                // Заголовок
                case R.id.button_help_header:
                    tooltip = getResources().getString(R.string.header_text_tooltip);
                    break;
                // Описание
                case R.id.button_help_description:
                    tooltip = "Текст, который будет показываться под заголовком, формат - строка";
                    break;
                // Надпись для первого значения
                case R.id.button_help_first_value_textview:
                    tooltip = "Надпись вместо 'Первое значение'";
                    break;
                // Надпись для второго значения
                case R.id.button_help_second_value_textview:
                    tooltip = "Надпись вместо 'Второе значение'";
                    break;
                // Сообщение не прошло валидацию
                case R.id.button_help_first_error:
                    tooltip = "Сообщение, которое увидит пользователь, если первое введенное значение не пройдет валидацию, формат - строка";
                    break;
                // Сообщение не прошло валидацию
                case R.id.button_help_second_error:
                    tooltip = "Сообщение, которое увидит пользователь, если второе введенное значение не пройдет валидацию, формат - строка";
                    break;
                // Формула для обработки первого входящего значения
                case R.id.button_help_first_incoming_formula:
                    tooltip = "Пересчет первого входящего значения. Переменная для значения: x. Пустое поле, если обработка не нужна. Не использовать булевые операторы! Подробнее см. инструкцию";
                    break;
                // Формула для обработки второго входящего значения
                case R.id.button_help_second_incoming_formula:
                    tooltip = "Пересчет второго входящего значения. Переменная для значения: x. Пустое поле, если обработка не нужна. Не использовать булевые операторы! Подробнее см. инструкцию";
                    break;
                // Количество знаков после запятой для первого входящего значения
                case R.id.button_help_first_decimal_places:
                    tooltip = "Количество знаков после запятой в первом ОБРАБОТАННОМ ФОРМУЛОЙ значении. Оставьте поле пустым для целого значения";
                    break;
                // Количество знаков после запятой для второго входящего значения
                case R.id.button_help_second_decimal_places:
                    tooltip = "Количество знаков после запятой во втором ОБРАБОТАННОМ ФОРМУЛОЙ значении. Оставьте поле пустым для целого значения";
                    break;
                case R.id.button_help_first_outgoing_formula:
                    tooltip = "Пересчет первого исходящего значения. Переменная для значения: x. Полученное значение округляется до целого. Пустое поле, если обработка не нужна. Не использовать булевые операторы! Подробнее см. инструкцию";
                    break;
                case R.id.button_help_second_outgoing_formula:
                    tooltip = "Пересчет второго исходящего значения. Переменная для значения: x. Полученное значение округляется до целого. Пустое поле, если обработка не нужна. Не использовать булевые операторы! Подробнее см. инструкцию";
                    break;
                case R.id.button_help_first_validation_formula:
                    tooltip = "Валидация первого ОБРАБОТАННОГО ФОРМУЛОЙ исходящего значения. Переменная для обработанного значения: x. Пустое поле, если валидация не нужна. Подробнее см. инструкцию";
                    break;
                case R.id.button_help_second_validation_formula:
                    tooltip = "Валидация второго ОБРАБОТАННОГО ФОРМУЛОЙ исходящего значения. Переменная для обработанного значения: x. Пустое поле, если валидация не нужна. Подробнее см. инструкцию";
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

        EditText mFirstIncomingValueFormula = (EditText) findViewById(R.id.incoming_formula_text);
        EditText mFirstFractionDigits = (EditText) findViewById(R.id.decimal_places_text);

        EditText mSecondIncomingValueFormula = (EditText) findViewById(R.id.second_incoming_formula_text);
        EditText mSecondFractionDigits = (EditText) findViewById(R.id.second_decimal_places_text);

        EditText mOutgoingFirstValueFormula = (EditText) findViewById(R.id.outgoing_formula_text);
        EditText mOutgoingFirstValueValidation = (EditText) findViewById(R.id.validation_formula_text);

        EditText mOutgoingSecondValueFormula = (EditText) findViewById(R.id.second_outgoing_formula_text);
        EditText mOutgoingSecondValueValidation = (EditText) findViewById(R.id.validation_second_formula_text);

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
        mParamsMap.put("HeaderText", mHeaderText.getText().toString());

        // Сообщение контроллеру при входе
        mParamsMap.put("DescriptionText", mDescText.getText().toString());

        // Надпись для 'Первое значение'
        mParamsMap.put("FirstValueLabel", mFirstValueLabel.getText().toString());

        // Надпись для 'Второе значение'
        mParamsMap.put("SecondValueLabel", mSecondValueLabel.getText().toString());

        // Префикс входящего сообщения для установки значения
        mParamsMap.put("InvalidFirstValueText", mInvalidFirstValueText.getText()
                .toString());

        // Префикс входящего сообщения для установки значения
        mParamsMap.put("InvalidSecondValueText", mInvalidSecondValueText.getText()
                .toString());

        mParamsMap.put("IncomingFirstValueFormula", mFirstIncomingValueFormula.getText()
                .toString());

        mParamsMap.put("IncomingSecondValueFormula", mSecondIncomingValueFormula.getText()
                .toString());

        mParamsMap.put("FirstFractionDigits", mFirstFractionDigits.getText().toString());

        mParamsMap.put("SecondFractionDigits", mSecondFractionDigits.getText().toString());

        mParamsMap.put("FirstOutgoingValueFormula", mOutgoingFirstValueFormula.getText()
                .toString());

        mParamsMap.put("FirstOutgoingValueValidation", mOutgoingFirstValueValidation
                .getText().toString());

        mParamsMap.put("SecondOutgoingValueFormula", mOutgoingSecondValueFormula.getText()
                .toString());

        mParamsMap.put("SecondOutgoingValueValidation", mOutgoingSecondValueValidation
                .getText().toString());

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