package com.isosystem.smarthouse.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.util.HashMap;

@SuppressWarnings("deprecation")
public class AddMenuItemSendMessage extends Activity {
	MyApplication mApplication;
	Context mContext;
	
	/** Режим окна (редактирование\создание) */
	boolean mEditMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_mainmenu_add_send_message);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		mApplication = (MyApplication) getApplicationContext();
		mContext = this;

		// Кнопка добавить
		Button addBtn = (Button) findViewById(R.id.btn_ok);
		addBtn.setOnClickListener(mAddListener);

		// Кнопка отменить
		Button backBtn = (Button) findViewById(R.id.btn_cancel);
		backBtn.setOnClickListener(mBackListener);

		// Установка подсказок для кнопок "Подсказка"
		ImageButton mTooltipButton = (ImageButton) findViewById(R.id.button_help_header);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_description);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);

		mTooltipButton = (ImageButton) findViewById(R.id.button_help_outgoing_prefix);
		mTooltipButton.setOnClickListener(tooltipsButtonListener);
		
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
		
		// Отправка сообщения
		EditText mOutgoingValueMessage = (EditText) findViewById(R.id.outgoing_prefix_text);
		if (pMap.get("OutgoingValueMessage")!=null)
			mOutgoingValueMessage.setText(pMap.get("OutgoingValueMessage"));
	}
	
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
				tooltip = "Текст, который будет показываться в качестве заголовка диалога, формат - строка";
				break;
			// Описание
			case R.id.button_help_description:
				tooltip = "Текст, который будет показываться под заголовком, формат - строка";
				break;
			case R.id.button_help_outgoing_prefix:
				tooltip = "Сообщение, которое будет выслано контроллеру.";
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
