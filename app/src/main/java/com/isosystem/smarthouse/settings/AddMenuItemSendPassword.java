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
import com.isosystem.smarthouse.dialogs.OutgoingMessagePasswordCheckDialog;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("deprecation")
public class AddMenuItemSendPassword extends Activity {

	MyApplication mApplication;
	Context mContext;
	
	Gallery mGallery;
	ArrayList<String> mImages = null;
	ImageView mGalleryPicker;

	/** Режим окна (редактирование\создание) */
	boolean mEditMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_mainmenu_add_send_password);

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

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_description);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_outgoing_prefix);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		// Устанавливаем слушатель на кнопку проверки исходяшего сообщения
		ImageButton mOutgoingMessageDialogButton = (ImageButton) this
				.findViewById(R.id.button_check_outgoing_prefix);
		mOutgoingMessageDialogButton
				.setOnClickListener(outgoingMessageListener);
		
		/** Определяем режим работы окна (создание или редактирование */
		try {
			mEditMode = getIntent().getBooleanExtra("isEdited", false);
		} catch (Exception e) {
			Logging.v("Исключение при попытке считать режим работы окна");
			e.printStackTrace();
		}
		
		// Если режим редактирования - заполняем поля
		if (mEditMode) {		
			setFieldValues();
		}
	}
	
	/**
	 * Установка значений полей в режиме редактирования
	 */
	private void setFieldValues() {
		
		HashMap<String, String> pMap = mApplication.mTree.tempNode.paramsMap;
		
		// Хеша может не быть, если был изменен тип вершины
		if (pMap == null) return;
			
		// Установка названия
		EditText mHeaderText = (EditText) findViewById(R.id.header_text);
		if (pMap.get("HeaderText")!=null)
			mHeaderText.setText(pMap.get("HeaderText"));
				
		// Установка описания
		EditText mDescText = (EditText) findViewById(R.id.description_text);
		if (pMap.get("DescriptionText")!=null)
			mDescText.setText(pMap.get("DescriptionText"));
		
		if (pMap.get("SelectedImage")!=null) {
			// Выбор изображения для пункта меню
			int pos = mImages.indexOf(pMap.get("SelectedImage"));
			
			// Если изображение было найдено
			// Вручную выделяем иконку в галерее
			// и ставим изображение в пикер
			if (pos!=-1) {
				mGallery.setSelection(pos);
				Bitmap b = BitmapFactory.decodeFile(mImages.get(pos));
				mGalleryPicker.setImageBitmap(b);
				mGalleryPicker.setTag(mImages.get(pos));
			}
		}
		
		// Префикс для отправки введенного значения
		EditText mOutgoingValueMessage = (EditText) findViewById(R.id.outgoing_prefix_text);
		if (pMap.get("OutgoingValueMessage")!=null)
			mOutgoingValueMessage.setText(pMap.get("OutgoingValueMessage"));
	}
	

	private OnItemClickListener galleryImageSelectListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			Bitmap b = BitmapFactory.decodeFile(mImages.get(position));
			mGalleryPicker.setImageBitmap(b);
			mGalleryPicker.setTag(mImages.get(position));
		}
	};

	private OnClickListener outgoingMessageListener = new OnClickListener() {
		// Проверка формулы для обработки входящего значения
		@Override
		public void onClick(final View v) {

			EditText mOutgoingPrefix = (EditText) findViewById(R.id.outgoing_prefix_text);

			OutgoingMessagePasswordCheckDialog dialog = new OutgoingMessagePasswordCheckDialog(
					mOutgoingPrefix.getText().toString());

			dialog.show(getFragmentManager(), "Outgoing message password check");
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
				tooltip = "Текст, который будет показываться в качестве заголовка окна, формат - строка";
				break;
			// Описание
			case R.id.button_help_description:
				tooltip = "Текст, который будет показываться под заголовком, формат - строка";
				break;
			case R.id.button_help_outgoing_prefix:
				tooltip = "Сообщение со значением, которое будет выслано контроллеру. Настройщик вводит префикс сообщения. При отсылке, программа возьмет пройденное валидацию значение и вышлет сообщение <[префикс][,][значение]>";
				break;
			default:
				tooltip = "Если вы видите это сообщение, сообщите разработчику об ошибке, указав ситуацию, при которой вы увидели это сообщение";
				break;
			}
			// Показываем Toast с сообщением
			Notifications.showTooltip(mContext, tooltip);
		}
	};

	/**
	 * Считываем пути к картинкам галлереи
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
	 * Отмена добавления нового пункта.
	 * В этом случае, необходимо загрузить предыдущую версию меню
	 */
	private void undoMenuItemAdding() {
		
		try {
			mApplication.mProcessor.loadMenuTreeFromInternalStorage();
		} catch (Exception e) {
			Logging.v("Исключение при попытке загрузить меню из файла");
			e.printStackTrace();
		}
	}
	
	private void addNewMenuItem() {

		EditText mHeaderText = (EditText) findViewById(R.id.header_text);
		EditText mDescText = (EditText) findViewById(R.id.description_text);
		EditText mOutgoingValueMessage = (EditText) findViewById(R.id.outgoing_prefix_text);

		ImageView mSelectedImage = (ImageView) findViewById(R.id.tile_image);

		// Проверка заполненности обязательных полей
		// Обязательные поля: 3 надписи и 2 сообщения + картинка
		if ((mHeaderText.getText().toString() == null)  || (mHeaderText.getText().toString().trim().isEmpty())
				|| (mDescText.getText().toString() == null) || (mDescText.getText().toString().trim().isEmpty())
				|| (mOutgoingValueMessage.getText().toString() == null) || (mOutgoingValueMessage.getText().toString().trim().isEmpty())) {
			Notifications.showError(mContext,
					"Не заполнены обазятельне поля (они отмечены *)");
			return;
		}

		// Создаем хеш для хранения строковых значений (как правило,
		// сообщения)
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

		mParamsMap.put("OutgoingValueMessage", mOutgoingValueMessage.getText()
				.toString());

		mParamsMap.put("SelectedImage", mSelectedImage.getTag().toString());

		mApplication.mTree.tempNode.paramsMap = mParamsMap;

		try {
			mApplication.mProcessor.saveMenuTreeToInternalStorage();
		} catch (Exception e) {
			e.printStackTrace();
		}

		finish();
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
									addNewMenuItem();
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

	private OnClickListener mBackListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("Вы действительно хотите выйти?")
					.setPositiveButton("Выйти",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
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
