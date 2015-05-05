package com.isosystem.smarthouse.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.data.MenuTree;
import com.isosystem.smarthouse.data.MenuTree.MenuScreenType;
import com.isosystem.smarthouse.data.MenuTree.NodeType;
import com.isosystem.smarthouse.data.MenuTreeNode;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Класс который реализует окно добавления нового пункта меню
 */
public class AddMenuItemActivity extends Activity implements
		RadioGroup.OnCheckedChangeListener {

	/** Спиннер для выбора меню, куда вставлять узел */
	Spinner menuSpinner;
	/** Спиннер для типа конечной точки */
	Spinner mScreenTypeSpinner;

	MyApplication mApplication;
	Context mContext;
	
	Gallery mGallery;
	ArrayList<String> mImages = null;
	ImageView mGalleryPicker; //Пикер изображения галереи
	
	/** Режим редактирования */
	boolean mEditMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_mainmenu_add);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mContext = this;
		mApplication = (MyApplication) getApplicationContext();

		/** Определяем режим работы окна (создание или редактирование) */
		try {
			mEditMode = getIntent().getBooleanExtra("isEdited", false);
		} catch (Exception e) {
			Logging.v("Исключение при попытке считать режим работы окна");
			e.printStackTrace();
		}
		
		/** Кнопка назад */
		Button mReturnButton = (Button) findViewById(R.id.btn_cancel);
		mReturnButton.setOnClickListener(mReturnListener);

		/** Кнопка добавить */
		Button mAddButton = (Button) findViewById(R.id.btn_ok);
		mAddButton.setOnClickListener(mAddListener);

		// Изображение выбранной картинки из галереи
		mGalleryPicker = (ImageView) findViewById(R.id.point_image);
		mGalleryPicker.setTag("");

		// Галерея изображения для экрана
		mGallery = (Gallery) findViewById(R.id.point_image_gallery);
		mImages = getImages();
		mGallery.setAdapter(new GalleryAdapter(mImages, this));
		mGallery.setOnItemClickListener(galleryImageSelectListener);
		
		/** Спиннер с выбором ветки меню */
		menuSpinner = (Spinner) findViewById(R.id.add_to_menu_spinner);
		if (mEditMode) {
			GenerateSpinner();
		} else {			
			View divider = findViewById(R.id.divider_add_to_menu);
			divider.setVisibility(View.GONE);
			TextView tv = (TextView) findViewById(R.id.add_to_menu_label);
			tv.setVisibility(View.GONE);
			menuSpinner.setVisibility(View.GONE);
		}

		/** Спиннер с выбором типа конечной точки */
		mScreenTypeSpinner = (Spinner) findViewById(R.id.point_type_spinner);
		GenerateScreenTypeSpinner();

		/** Радиогруппа и радиокнопки */
		RadioGroup mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		mRadioGroup.setOnCheckedChangeListener(this);
		RadioButton rButton = (RadioButton) findViewById(R.id.radio0);
		
		if (rButton.isChecked()) {
			mScreenTypeSpinner.setEnabled(false);
			mScreenTypeSpinner.setClickable(false);
		} else {
			mScreenTypeSpinner.setEnabled(true);
			mScreenTypeSpinner.setClickable(true);
		}
		
		// Обновление спиннера с типами конечного пункта
		mScreenTypeSpinner.invalidate();
		
		/**
		 * Если режим редактирования, тогда заполняем все поля данными из
		 * существующего узла
		 */
		if (mEditMode) {
			// Редактирование узла
			setFieldValues();
		} else {
			// Создание узла
			mApplication.mTree.tempNode = new MenuTreeNode(false);
		}
	}

	/**
	 * Заполняет поля окна данными из существующего узла Окно находится в режиме
	 * редактирования
	 */
	private void setFieldValues() {
		/** Заполняем имя пункта */
		EditText et1 = (EditText) findViewById(R.id.name_value);
		et1.setText(mApplication.mTree.tempNode.nodeTitle);

		/** Выбираем место узла в дереве */
		try {
			// Родитель редактируемого узла
			int position = mApplication.mTree
					.GetSpinnerPositionForNode(mApplication.mTree.tempNode.parentNode);
			menuSpinner.setSelection(position);
		} catch (Exception e) {
			Logging.v("Исключение при попытке выбрать место редактируемого узла в дереве");
			e.printStackTrace();
		}

		/**
		 * Выбор типа узла (лист, меню). Если узел - лист, выбор типа конечного
		 * узла
		 */
		try {
			if (mApplication.mTree.tempNode.nodeType == NodeType.NODE_MENU) {
				RadioButton rButton = (RadioButton) findViewById(R.id.radio0);
				rButton.setChecked(true);
				onCheckedChanged(null, R.id.radio0);
				// Выбор типа конечного узла
			} else if (mApplication.mTree.tempNode.nodeType == NodeType.NODE_LEAF) {
				RadioButton rButton = (RadioButton) findViewById(R.id.radio1);
				rButton.setChecked(true);
				onCheckedChanged(null, R.id.radio1);
				mScreenTypeSpinner.setSelection(mApplication.mTree.tempNode.screenType.ordinal());
			} else {
				Logging.v("Был получен неизвестный тип узла (не лист и не меню)");
			}
		} catch (Exception e) {
			Logging.v("Исключение при попытке выбрать тип узла и выбрать тип листа");
			e.printStackTrace();
		} // try
		
		HashMap<String, String> pMap = mApplication.mTree.tempNode.paramsMap;
		
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
	}// method
	
	/**
	 * Генерирует спиннер для выбора пункта меню, куда добавлять новый пункт
	 */
	private void GenerateSpinner() {
		menuSpinner.setAdapter(null);

		ArrayList<String> menuNodes;

		/**
		 * Если окно находится в режиме редактирования, необходимо получить
		 * список узлов без текущего редактируемого узла и его потомков
		 */
		if (mEditMode) {
			menuNodes = mApplication.mTree.getMenuNodes(mApplication.mTree.tempNode);
		} else {
			menuNodes = mApplication.mTree.getMenuNodes();
		}

		ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, menuNodes);
		ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		menuSpinner.setAdapter(ad);
		ad.notifyDataSetChanged();
	}
	
	/**
	 * Генерирует спиннер для выбора типа конечного окна
	 */
	private void GenerateScreenTypeSpinner() {

		mScreenTypeSpinner.setAdapter(null);

		// Получаем список типов окон
		ArrayList<String> screenTypes = new ArrayList<String>();
		for (MenuTree.MenuScreenType type : MenuTree.MenuScreenType.values()) {
			screenTypes.add(type.toString());
		}

		ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, screenTypes);
		mScreenTypeSpinner.setAdapter(ad);
		ad.notifyDataSetChanged();
	}

	/** 
	 * Обработка нажатия на радиокнопку 
	 */
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.radio0) {
			mScreenTypeSpinner.setEnabled(false);
			mScreenTypeSpinner.setClickable(false);
		} else if (checkedId == R.id.radio1) {
			mScreenTypeSpinner.setEnabled(true);
			mScreenTypeSpinner.setClickable(true);
		}
		mScreenTypeSpinner.invalidate();
	}

	/**
	 * Кнопка "Назад"
	 */
	private OnClickListener mReturnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("Вы действительно хотите выйти?")
					.setPositiveButton("Выйти",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									
									//Очищаем tempNode
									mApplication.mTree.tempNode = null;
									mApplication.mTree.tempParentNode = null;
									
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

	/**
	 * Проверка полей на валидность
	 * @return TRUE если поля заполнены правильно
	 */
	private Boolean validateFields() {
		// Имя пункта меню
		EditText menuItemName = (EditText) findViewById(R.id.name_value);
		// Пункт - меню
		RadioButton typeMenu = (RadioButton) findViewById(R.id.radio0);
		// Пункт - конечная точка
		RadioButton typePoint = (RadioButton) findViewById(R.id.radio1);
		
		// Провера имени пункта меню
		String itemName = menuItemName.getText().toString();
		if (TextUtils.isEmpty(itemName) ||
				 (itemName.trim().length() == 0)) {
			menuItemName.requestFocus();
			Toast.makeText(mContext, "Имя пункта меню введено некорректно!", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		// Проверка на выбор родителя
		if(mEditMode) {
			if (menuSpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
				Toast.makeText(mContext, "Не выбрано место добавления пункта меню", Toast.LENGTH_SHORT).show();
				return false; 
			}
		} else {
			if (mApplication.mTree.tempParentNode == null) {
				Notifications.showError(mContext, "Программная ошибка (родительский узел не существует)");
				return false;
			}
		}

		// Проверка на тип пункта меню
		if ((!typeMenu.isChecked()) && (!typePoint.isChecked())) {
			Toast.makeText(mContext, "Не выбран тип пункта меню.", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		// Проверка на выбор типа конечного пункта
		if ((mScreenTypeSpinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION)
				&& (typePoint.isChecked())) {
			Toast.makeText(mContext, "Не выбран тип конечного пункта меню.", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
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
	 * Обработка нажатия кнопки "Подтвердить" Необходимо определить: 1) Все ли
	 * пункты были заполнены (название, тип, куда добавить) 2) Если тип
	 * "Пункт меню", тогда добавить в дерево новую ветку и обновить дерево
	 */
	private OnClickListener mAddListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// Если поля не прошли валидацию
			if (!validateFields()) return;
			
			// Имя пункта меню
			EditText menuItemName = (EditText) findViewById(R.id.name_value);
			// Пункт - меню
			RadioButton typeMenu = (RadioButton) findViewById(R.id.radio0);
			// Пункт - конечная точка
			RadioButton typePoint = (RadioButton) findViewById(R.id.radio1);
			
			// Название нового узла
			mApplication.mTree.tempNode.nodeTitle = menuItemName.getText().toString();
						
			// Устанавливаем тип пункта меню
			if (typeMenu.isChecked()) {
				// Если выбрано меню
				
				// Если пользователь выбрал меню, а была конечная точка
				if (mApplication.mTree.tempNode.nodeType == MenuTree.NodeType.NODE_LEAF) {
					mApplication.mTree.tempNode.childNodes = new ArrayList<MenuTreeNode>();
					mApplication.mTree.tempNode.paramsMap = new HashMap<String, String>();
				}
				mApplication.mTree.tempNode.nodeType = MenuTree.NodeType.NODE_MENU;
			} else if (typePoint.isChecked()) {
				
				// Если пользователь выбрал конечную точку, а было меню
				if (mApplication.mTree.tempNode.nodeType == MenuTree.NodeType.NODE_MENU) {
					mApplication.mTree.tempNode.childNodes = new ArrayList<MenuTreeNode>();
					mApplication.mTree.tempNode.paramsMap = new HashMap<String, String>();
				}
				
				mApplication.mTree.tempNode.nodeType = MenuTree.NodeType.NODE_LEAF;
			}
			
			ImageView mSelectedImage = (ImageView) findViewById(R.id.point_image);

			// Строковые значения (сообщения и надписи)
			HashMap<String, String> mParamsMap = mApplication.mTree.tempNode.paramsMap;
			if (mParamsMap != null) {
				mParamsMap.put("GridImage", mSelectedImage.getTag().toString());
			} else {
				mParamsMap = new HashMap<String, String>();
				mParamsMap.put("GridImage", mSelectedImage.getTag().toString());
			}		
			mApplication.mTree.tempNode.paramsMap = mParamsMap;
			
			MenuTreeNode parentNode;
			
			if (mEditMode) {
				parentNode = mApplication.mTree
						.GetNodeForSpinnerPosition(menuSpinner
								.getSelectedItemPosition());
			} else {
				parentNode = mApplication.mTree.tempParentNode;
			}
									
			// Обработка меню или конечного пункта
			if (mApplication.mTree.tempNode.nodeType == MenuTree.NodeType.NODE_MENU) { // Меню
				
				// Если режим редактирования - нужно удалить связь со старым родителем
				if (mEditMode) {
					mApplication.mTree.tempNode.parentNode.childNodes.remove(mApplication.mTree.tempNode);
					mApplication.mTree.tempNode.parentNode = null;
				}
								
				// Создание связи между узлом и родителем
				parentNode.childNodes.add(mApplication.mTree.tempNode);
				mApplication.mTree.tempNode.parentNode = parentNode;
				
				// Перезапись дерева
				try {
					mApplication.mProcessor.saveMenuTreeToInternalStorage();
				} catch (Exception e) {
					Logging.v(getResources().getString(R.string.exception_reload_menu_tree));
					e.printStackTrace();
				}

				((Activity) mContext).finish();
			} else if (mApplication.mTree.tempNode.nodeType == MenuTree.NodeType.NODE_LEAF) { // Конечный пункт				
				
				// Открытие нового окна с настройками
				Intent intent = new Intent();
				intent.putExtra("isEdited", mEditMode);
				
				if (mEditMode) {
					mApplication.mTree.tempNode.parentNode.childNodes.remove(mApplication.mTree.tempNode);
					mApplication.mTree.tempNode.parentNode = null;
				}
				
				// Создание связи между узлом и родителем
				parentNode.childNodes.add(mApplication.mTree.tempNode);
				mApplication.mTree.tempNode.parentNode = parentNode;
								
				// Тип конечного окна
				int position = mScreenTypeSpinner.getSelectedItemPosition();

				if (position == 0) { // Окно прямого добавления значения
					mApplication.mTree.tempNode.screenType = MenuScreenType.SetIntValue;
					intent.setClass(mContext, AddMenuItemSendValue.class);
				} else if (position == 1) { // Ввод пароля
					mApplication.mTree.tempNode.screenType = MenuScreenType.SetPasswordValue;
					intent.setClass(mContext, AddMenuItemSendPassword.class);
				} else if (position == 2) { // Ввод булевого значения
					mApplication.mTree.tempNode.screenType = MenuScreenType.SetBooleanValue;	
					intent.setClass(mContext, AddMenuItemSendBool.class);
				} else if (position == 3) { // Окно отсылки сообщения
					mApplication.mTree.tempNode.screenType = MenuScreenType.SendMessage;	
					intent.setClass(mContext, AddMenuItemSendMessage.class);
				} else if (position == 4) { // Ввод диапазона числовых значений
					mApplication.mTree.tempNode.screenType = MenuScreenType.SetIntRangeValue;
					intent.setClass(mContext,AddMenuItemSendRangeIntValue.class);
				}
				mContext.startActivity(intent);
			}
		} // onClick
	}; // addListener
}
