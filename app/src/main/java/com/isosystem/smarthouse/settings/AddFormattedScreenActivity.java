package com.isosystem.smarthouse.settings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
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
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class AddFormattedScreenActivity extends Activity {

	MyApplication mApplication;
	Context mContext;

	// Режим редактирования
	boolean mEditMode;
	// Позиция редактируемого окна
	int mEditedPosition;

	// Поля ввола
	EditText mScreenName;
	EditText mFontSize;
	EditText mLinesNumber;

	CheckBox mScrollableWindow;

	EditText mStartTransfer;
	EditText mEndTransfer;

	// Кнопки "Добавить"/"Отменить"
	Button mAddButton;
	Button mCancelButton;

	Gallery mGallery;
	ArrayList<String> mImages = null;
	ImageView mGalleryPicker; //Пикер изображения галереи
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_formscreen_add);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mContext = this;
		mApplication = (MyApplication) mContext.getApplicationContext();

		/** Определяем режим работы окна (создание или редактирование) */
		try {
			mEditMode = getIntent().getBooleanExtra("isEdited", false);
		} catch (Exception e) {
			Logging.v("Исключение при попытке считать режим работы окна");
			e.printStackTrace();
		}

		mScreenName = (EditText) findViewById(R.id.formscreen_name);
		mFontSize = (EditText) findViewById(R.id.text_size);
		mLinesNumber = (EditText) findViewById(R.id.lines_number);

		mStartTransfer = (EditText) findViewById(R.id.transfer_start_message);
		mEndTransfer = (EditText) findViewById(R.id.transfer_end_message);

		mScrollableWindow = (CheckBox) findViewById(R.id.scrollable_window);

		mAddButton = (Button) findViewById(R.id.btn_ok);
		mAddButton.setOnClickListener(mAddListener);

		mCancelButton = (Button) findViewById(R.id.btn_cancel);
		mCancelButton.setOnClickListener(mReturnListener);

		// Изображение выбранной картинки из галереи
		mGalleryPicker = (ImageView) findViewById(R.id.tile_image);
		mGalleryPicker.setTag("");

		// Галерея изображения для экрана
		mGallery = (Gallery) findViewById(R.id.tile_image_gallery);
		mImages = getImages();
		mGallery.setAdapter(new GalleryAdapter(mImages, this));
		mGallery.setOnItemClickListener(galleryImageSelectListener);

		ImageButton mTooltipButton = (ImageButton) findViewById(R.id.button_help_formscreen_name);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_text_size);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_lines_number);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_scrollable_window);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_start_transfer);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_end_transfer);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		// В режиме редактирования, считываем позицию редактируемого окна
		if (mEditMode) {
			try {
				mEditedPosition = getIntent().getIntExtra("edited_screen_position", -1);
			} catch (Exception e) {
				Logging.v("Исключение при попытке считать режим работы окна");
				e.printStackTrace();
			}
		}
		
		// Если не удалось считать позицию редактируемого окна
		if (mEditedPosition == -1) {
			Notifications.showError(mContext, "Ошибка при попытке отредактировать окно форматированного вывода");
			this.finish();
		}

		// В режиме редактирования заполнение полей с данными
		if (mEditMode) {
			setFieldValues();
		}
	}
	
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
	 * Слушатель для кнопок "Подсказка". При нажатии на кнопку показывается
	 * Toast с подсказкой
	 */
	private OnClickListener tooltipsButtonListener = new OnClickListener() {
		// Проверка формулы для обработки входящего значения
		@Override
		public void onClick(final View v) {
			String tooltip;
			switch (v.getId()) {
				case R.id.button_help_formscreen_name:
					tooltip = "Имя окна, которое будет отображаться в плитках или в списке форматированного вывода";
					break;
				case R.id.button_help_text_size:
					tooltip = "Размер шрифта в пикселях";
					break;
				case R.id.button_help_lines_number:
					tooltip = "Количество строк в окне вывода. Сообщения, где номер строки больше указанного количества строк, будут выводиться в последней возможной строке";
					break;
				case R.id.button_help_scrollable_window:
					tooltip = "Если прокрутка включена, то окно можно вертикально скроллить, если сообщения не влезают в окно. Если прокрутка выключена, строки будут ужиматься, чтобы поместиться в окно";
					break;
				case R.id.button_help_start_transfer:
					tooltip = "Сообщение контроллеру о начале передачи сообщений форматированного вывода";
					break;
				case R.id.button_help_end_transfer:
					tooltip = "Сообщение контроллеру об окончании передачи сообщений форматированного вывода";
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
	 * Заполнение полей в режиме редактирования
	 */
	private void setFieldValues() {
		mScreenName.setText(mApplication.mFormattedScreens.mFormattedScreens.get(mEditedPosition).mName);
		mStartTransfer.setText(mApplication.mFormattedScreens.mFormattedScreens.get(mEditedPosition).mInputStart);
		mEndTransfer.setText(mApplication.mFormattedScreens.mFormattedScreens.get(mEditedPosition).mInputEnd);
		
		HashMap<String, String> pMap = mApplication.mFormattedScreens.mFormattedScreens.get(mEditedPosition).paramsMap;
		
		if (pMap != null) {
			if (pMap.get("GridImage")!=null) {
				// Выбор изображения для пункта меню
				int pos = mImages.indexOf(pMap.get("GridImage"));
				
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
		} // if !null

		// Прокрутка окна
		if (pMap.get("ScrollableWindow")!=null){
			if (pMap.get("ScrollableWindow").equals("0")){
				mScrollableWindow.setChecked(false);
			} else if (pMap.get("ScrollableWindow").equals("1")) {
				mScrollableWindow.setChecked(true);
			}
		}

		// Размер шрифта
		if (pMap.get("FontSize")!=null)
			mFontSize.setText(pMap.get("FontSize"));

		// Количество строк
		if (pMap.get("LinesNumber")!=null)
			mLinesNumber.setText(pMap.get("LinesNumber"));
	}

	private OnClickListener mReturnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	/**
	 * Обработка нажатия кнопки "Подтвердить"
	 */
	private OnClickListener mAddListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if ((TextUtils.isEmpty(mScreenName.getText().toString().trim()))
					|| (TextUtils.isEmpty(mStartTransfer.getText().toString().trim()))
					|| (TextUtils.isEmpty(mEndTransfer.getText().toString().trim()))
					|| (mGalleryPicker.getDrawable() == null) || (mGalleryPicker.getDrawable().toString().trim().isEmpty())) {
				Notifications.showError(mContext, "Не все поля заполнены");
				return;
			} else {
				String name = mScreenName.getText().toString();
				String start = mStartTransfer.getText().toString();
				String end = mEndTransfer.getText().toString();

				HashMap<String, String> mParamsMap = new HashMap<String, String>();
				mParamsMap.put("GridImage", mGalleryPicker.getTag().toString());

				mParamsMap.put("FontSize", mFontSize.getText().toString());
				mParamsMap.put("LinesNumber", mLinesNumber.getText().toString());

				// Прокрутка окна
				String scrollable_window;
				if (mScrollableWindow.isChecked()) {
					scrollable_window = "1";
				} else {
					scrollable_window = "0";
				}
				mParamsMap.put("ScrollableWindow",scrollable_window);

				// В режиме редактирования меняем поля у существующего окна
				// Иначе создаем новое окно
				if (mEditMode) {
					mApplication.mFormattedScreens.changeFormattedScreen(mContext, mEditedPosition, name, start, end, mParamsMap);
				} else {
					mApplication.mFormattedScreens.addFormattedScreen(mContext, name, start, end, mParamsMap);
				}
			}
			finish();
		}

	};

}
