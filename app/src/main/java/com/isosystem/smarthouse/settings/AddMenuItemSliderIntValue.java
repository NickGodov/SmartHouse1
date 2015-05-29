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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.dialogs.FormulaCheckDialog;
import com.isosystem.smarthouse.dialogs.OutgoingMessageCheckDialog;
import com.isosystem.smarthouse.dialogs.ValidationFormulaCheckDialog;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class AddMenuItemSliderIntValue extends Activity {
	Context mContext;
	MyApplication mApplication;
	
	Gallery mGallery;
	ArrayList<String> mImages = null;
	ImageView mGalleryPicker; //Пикер изображения галереи

	CheckBox mDynamicMode;
	EditText mDynamicTimeout;

	//Режим окна (редактирование или создание)
	boolean mEditMode;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_mainmenu_add_slider_int_value);

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

		mDynamicMode = (CheckBox) findViewById(R.id.dynamic_mode);
		mDynamicTimeout = (EditText) findViewById(R.id.dynamic_send_timeout);

		// Установка подсказок для кнопок "Подсказка"
		
		ImageButton mTooltipButton = (ImageButton) findViewById(R.id.button_help_header);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_description);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_error);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_min_value);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_max_value);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_slider_step_value);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_incoming_formula);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_outgoing_formula);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_get_value);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_dynamic_send);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_outgoing_prefix);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		// Проверка формулы входящего значения
		ImageButton mIncomingValueFormulaDialogButton = (ImageButton) this
				.findViewById(R.id.button_check_incoming_formula);
		mIncomingValueFormulaDialogButton
				.setOnClickListener(incomingValueFormulaListener);

		// Проверка формулы исходящего значения
		ImageButton mOutgoingValueFormulaDialogButton = (ImageButton) this
				.findViewById(R.id.button_check_outgoing_formula);
		mOutgoingValueFormulaDialogButton
				.setOnClickListener(outgoingValueFormulaListener);

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
				
		// ?????????? ????? ? ?????? ??????????????
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
		if (pMap.get("HeaderText")!=null)
			mHeaderText.setText(pMap.get("HeaderText"));

		// Установка описания
		EditText mDescText = (EditText) findViewById(R.id.description_text);
		if (pMap.get("DescriptionText")!=null)
			mDescText.setText(pMap.get("DescriptionText"));

		// Сообщение при вводе невалидного значения
		EditText mInvalidValueText = (EditText) findViewById(R.id.error_text);
		if (pMap.get("InvalidValueText")!=null)
			mInvalidValueText.setText(pMap.get("InvalidValueText"));

		if (pMap.get("SelectedImage")!=null) {
			// Выбор изображения для пункта меню
			int pos = mImages.indexOf(pMap.get("SelectedImage"));

			// Если изображение было найдено
			// Выделяется иконка в галерее,
			// выставляется изображение в пикер
			if (pos!=-1) {
				mGallery.setSelection(pos);
				Bitmap b = BitmapFactory.decodeFile(mImages.get(pos));
				mGalleryPicker.setImageBitmap(b);
				mGalleryPicker.setTag(mImages.get(pos));
			}
		}

		// Формула для обработки входящего значения
		EditText mSliderMinValue = (EditText) findViewById(R.id.slider_min_value);
		if (pMap.get("SliderMinValue")!=null)
			mSliderMinValue.setText(pMap.get("SliderMinValue"));

		// Количество знаков после запятой
		EditText mSliderMaxValue = (EditText) findViewById(R.id.slider_max_value);
		if (pMap.get("SliderMaxValue")!=null)
			mSliderMaxValue.setText(pMap.get("SliderMaxValue"));

		// Формула для обработки исходящего значения
		EditText mSliderStepValue = (EditText) findViewById(R.id.slider_step_value);
		if (pMap.get("SliderStepValue")!=null)
			mSliderStepValue.setText(pMap.get("SliderStepValue"));

		// Булевая форма для валидации значения
		EditText mIncomingValueFormula = (EditText) findViewById(R.id.incoming_formula_text);
		if (pMap.get("IncomingValueFormula")!=null)
			mIncomingValueFormula.setText(pMap.get("IncomingValueFormula"));

		// Запрос текущего значения от контроллера
		EditText mOutgoingValueFormula = (EditText) findViewById(R.id.outgoing_formula_text);
		if (pMap.get("OutgoingValueFormula")!=null)
			mOutgoingValueFormula.setText(pMap.get("OutgoingValueFormula"));

		// Префикс для отправки введенного значения
		EditText mGiveMeValueMessage = (EditText) findViewById(R.id.get_value_text);
		if (pMap.get("GiveMeValueMessage")!=null)
			mGiveMeValueMessage.setText(pMap.get("GiveMeValueMessage"));

		// Динамический режим
		if (pMap.get("DynamicMode")!=null){
			if (pMap.get("DynamicMode").equals("0")){
				mDynamicMode.setChecked(false);
			} else if (pMap.get("DynamicMode").equals("1")) {
				mDynamicMode.setChecked(true);
			}
		}

		// Тайм-аут отсылки
		if (pMap.get("DynamicSendTimeout")!=null)
			mDynamicTimeout.setText(pMap.get("DynamicSendTimeout"));

		// Префикс для отправки введенного значения
		EditText mOutgoingValueMessage = (EditText) findViewById(R.id.outgoing_prefix_text);
		if (pMap.get("OutgoingValueMessage")!=null)
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

			OutgoingMessageCheckDialog dialog = new OutgoingMessageCheckDialog(
					mOutgoingValueFormula.getText().toString(),
					mOutgoingValueValidation.getText().toString(),
					mOutgoingPrefix.getText().toString());

			dialog.show(getFragmentManager(), "Outgoing message check");
		}
	};

	/**
	 * Слушатель для проверки формулы для валидации значения. Передаем в диалог
	 * формулу проверки исходящего значения и формулу валидации
	 */
	private OnClickListener outgoingValueValidationListener = new OnClickListener() {
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
	 * Слушатель для проверки формулы для обработки входящего значения. Сначала
	 * необходимо получить количество знаков после запятой После чего создать
	 * новый диалог с введенной формулой и количеством знаков после запятой.
	 */
	private OnClickListener incomingValueFormulaListener = new OnClickListener() {
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
	 * Слушатель для проверки формулы для обработки исходящего значения. Создаем
	 * диалог с введенной формулой. Т.к. нам нужно целое значение, то выставляем
	 * второй параметра диалога в 0
	 */
	private OnClickListener outgoingValueFormulaListener = new OnClickListener() {
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
				// Сообщение не прошло валидацию
				case R.id.button_help_error:
					tooltip = "Сообщение, которое увидит пользователь, если введенное значение не пройдет валидацию, формат - строка";
					break;
				case R.id.button_help_max_value:
					tooltip = "Максимальное значения слайдера ДО ОБРАБОТКИ ФОРМУЛОЙ";
					break;
				case R.id.button_help_min_value:
					tooltip = "Минимальное значения слайдера ДО ОБРАБОТКИ ФОРМУЛОЙ";
					break;
				case R.id.button_help_slider_step_value:
					tooltip = "Шаг одного деления слайдера";
					break;
				case R.id.button_help_incoming_formula:
					tooltip = "Пересчет входящего значения. Переменная для значения: x. Пустое поле, если обработка не нужна. Не использовать булевые операторы! Подробнее см. инструкцию";
					break;
				case R.id.button_help_outgoing_formula:
					tooltip = "Пересчет исходящего значения. Переменная для значения: x. Полученное значение округляется до целого. Пустое поле, если обработка не нужна. Не использовать булевые операторы! Подробнее см. инструкцию";
					break;
				case R.id.button_help_get_value:
					tooltip = "Сообщение, которое будет передано контроллеру при старте окна с требованием выслать текущее значение управляемого элемента. Сообщение передается без изменений";
					break;
				case R.id.button_help_dynamic_send:
					tooltip = "Отсылка сообщения при движении ползунка, а не при нажатии кнопки 'Установить'. Отсылка произойдет после указанного времени тайм-аута (200 мсек по умолчанию)";
					break;
				case R.id.button_help_outgoing_prefix:
					tooltip = "Сообщение со значением, которое будет выслано контроллеру. Настройщик вводит префикс сообщения. При отсылке, программа возьмет пройденное валидацию значение и вышлет сообщение <[префикс],[значение]>";
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
		EditText mInvalidValueText = (EditText) findViewById(R.id.error_text);

		EditText mSliderMinValue = (EditText) findViewById(R.id.slider_min_value);
		EditText mSliderMaxValue = (EditText) findViewById(R.id.slider_max_value);
		EditText mSliderStepValue = (EditText) findViewById(R.id.slider_step_value);

		EditText mDynamicSendTimeout = (EditText) findViewById(R.id.dynamic_send_timeout);

		EditText mIncomingValueFormula = (EditText) findViewById(R.id.incoming_formula_text);
		EditText mOutgoingValueFormula = (EditText) findViewById(R.id.outgoing_formula_text);
		EditText mGiveMeValueMessage = (EditText) findViewById(R.id.get_value_text);
		EditText mOutgoingValueMessage = (EditText) findViewById(R.id.outgoing_prefix_text);

		ImageView mSelectedImage = (ImageView) findViewById(R.id.tile_image);

		// Проверка заполненности обязательных полей
		// Обязательные поля: 3 надписи и 2 сообщения + картинка
		if ((mHeaderText.getText().toString() == null) || (mHeaderText.getText().toString().trim().isEmpty())
				|| (mDescText.getText().toString() == null) || (mDescText.getText().toString().trim().isEmpty())
				|| (mInvalidValueText.getText().toString() == null) || (mInvalidValueText.getText().toString().trim().isEmpty())
				|| (mSliderMinValue.getText().toString().trim().isEmpty())
				|| (mSliderMaxValue.getText().toString().trim().isEmpty())
				|| (mSliderStepValue.getText().toString().trim().isEmpty())
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
			if (mApplication.mTree.tempNode.paramsMap.get("GridImage")!=null) {
				mParamsMap.put("GridImage", mApplication.mTree.tempNode.paramsMap.get("GridImage"));
			}
		} // if !null

		// ID элемента
		mParamsMap.put("HeaderText", mHeaderText.getText().toString());

		// Сообщение контроллеру при входе
		mParamsMap.put("DescriptionText", mDescText.getText().toString());

		// Префикс входящего сообщения для установки значения
		mParamsMap.put("InvalidValueText", mInvalidValueText.getText()
				.toString());

		// Настройки слайдера
		mParamsMap.put("SliderMinValue",mSliderMinValue.getText().toString());
		mParamsMap.put("SliderMaxValue",mSliderMaxValue.getText().toString());
		mParamsMap.put("SliderStepValue",mSliderStepValue.getText().toString());

		mParamsMap.put("IncomingValueFormula", mIncomingValueFormula.getText()
				.toString());

		mParamsMap.put("OutgoingValueFormula", mOutgoingValueFormula.getText()
				.toString());

		mParamsMap.put("GiveMeValueMessage", mGiveMeValueMessage.getText()
				.toString());

		// Настройки отсылки
		String dynamic_mode;
		if (mDynamicMode.isChecked()) {
			dynamic_mode = "1";
		} else {
			dynamic_mode = "0";
		}
		mParamsMap.put("DynamicMode",dynamic_mode);

		mParamsMap.put ("DynamicSendTimeout",mDynamicSendTimeout.getText().toString());

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
			builder.setMessage("Вы действительно хотите добавить новый пункт меню?")
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