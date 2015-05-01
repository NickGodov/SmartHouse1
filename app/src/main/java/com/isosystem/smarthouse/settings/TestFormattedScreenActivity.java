package com.isosystem.smarthouse.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.data.FormattedScreen;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.util.Random;

public class TestFormattedScreenActivity extends Activity {
	Context mContext;
	MyApplication mApplication;

	MessageDispatcher mDispatcher;

	FormattedScreen mScreen;
	Button mBackButton;

	float mFontSize = 30;
	int mLinesCount = 9;
	int mLineSize = 43;

	LinearLayout mLinearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_test_formscreen);
		// Разворачиваем на полный экран
        if (getActionBar() != null) {
            getActionBar().hide();
        }
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		decorView.setSystemUiVisibility(8);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mContext = this;
		mApplication = (MyApplication) getApplicationContext();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);

		ImageButton mBackButton = (ImageButton) findViewById(R.id.frm_backbutton);
		mBackButton.setOnClickListener(mBackListener);

		ImageButton mAddMessageButton = (ImageButton) findViewById(R.id.frm_test_add_message);
		mAddMessageButton.setOnClickListener(mAddMessageListener);

		try {
			mFontSize = Float.parseFloat(prefs.getString(
					"formatted_screen_font_size", "30"));
		} catch (Exception e) {
			// Logging.v("Ошибка при попытке считать значение периода обновления буфера из preferences");
			e.printStackTrace();
		}
		try {
			mLinesCount = Integer.parseInt(prefs.getString(
					"formatted_screen_lines_count", "9"));
		} catch (Exception e) {
			// Logging.v("Ошибка при попытке считать значение периода обновления буфера из preferences");
			e.printStackTrace();
		}
		try {
			mLineSize = Integer.parseInt(prefs.getString(
					"formatted_screen_line_size", "43"));
		} catch (Exception e) {
			// Logging.v("Ошибка при попытке считать значение периода обновления буфера из preferences");
			e.printStackTrace();
		}

		mLinearLayout = (LinearLayout) findViewById(R.id.LinearLayout1);

		setLines(mLinesCount);
		setExampleText();
	}

	/**
	 * Установка текстовых строк и добавление их на экран
	 * 
	 * @param lines
	 *            количество строк
	 */
	private void setLines(int lines) {
		Typeface font = Typeface.createFromAsset(getAssets(), "PTM75F.ttf");
		LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
		View tempView = mLinearLayout.getChildAt(0);
		mLinearLayout.removeViewAt(0);

		for (int i = 0; i < lines; i++) {
			TextView textview = new TextView(this);
			textview.setMaxLines(1);
			textview.setSingleLine();
			textview.setTypeface(font);
			textview.setTextColor(Color.WHITE);
			textview.setTextSize(mFontSize);
			textview.setPadding(15, 0, 0, 0);
			textview.setLayoutParams(params);

			// Добавление текстового поля
			mLinearLayout.addView(textview);
		}

		// Добавление кнопки
		mLinearLayout.addView(tempView);
	}

	/**
	 * Вывод тестовых сообщений на экран
     */
	private void setExampleText() {
		TextView textView = (TextView) mLinearLayout.getChildAt(0);
		for (int i = 1; i <= mLineSize; i++)
			textView.setText(textView.getText() + String.valueOf(i % 10));

		for (int i = 1; i < mLinesCount; i++) {
			String example_string = String.valueOf(i) + " Показатель "
					+ String.valueOf(i) + ": "
					+ String.valueOf(new Random().nextInt(100000));
			textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText(example_string);
		}
	}

	/**
	 *  Возврат в настройки
     */
	private OnClickListener mBackListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(mContext,
					ApplicationPreferencesActivity.class);
			mContext.startActivity(intent);
		}
	};

	/**
	 *  Добавление сообщения
     */
	private OnClickListener mAddMessageListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final EditText messageInput = new EditText(mContext);

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("Введите сообщения для форматированного вывода")
					.setView(messageInput)
					.setPositiveButton("Добавить сообщение",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									String message = messageInput.getText()
											.toString();
									processMessage(message);
									dialog.cancel();
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

	/** Очистка всех строк */
	private void clearAll() {
		TextView textView;
		for (int i = 0; i < mLinesCount; i++) {
			textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText("");
		}
	}

	/**
	 * Очистка строки
	 * 
	 * @param i
	 *            номер строки
	 */
	private void clearString(int i) {
		if (i > mLinesCount)
			i = mLinesCount;

		try {
			TextView textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText("");
		} catch (Exception e) {
			Logging.v("Исключение при попытке очистить строку "
					+ String.valueOf(i));
			e.printStackTrace();
		}
	}

	/**
	 * Добавить сообщение в строку с начала
	 * 
	 * @param i
	 *            номер строки
	 * @param msg
	 *            сообщение
	 */
	private void addMessageToString(int i, String msg) {
		if (i > mLinesCount)
			i = mLinesCount;

		try {
			clearString(i);
			TextView textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText(msg);
		} catch (Exception e) {
			Logging.v("Исключение при попытке добавить текст в строку "
					+ String.valueOf(i));
			e.printStackTrace();
			// Notifications
			// .showError(getApplicationContext(),
			// "Ошибка при обработке сообщения от контроллера");
		}
	}

	/**
	 * Добавить сообщение в строку с определенной позиции
	 * 
	 * @param stringNumber
	 *            номер строки
	 * @param position
	 *            номер позиции
	 * @param msg
	 *            сообщение
	 */
	private void addMessageToStringFromPosition(int stringNumber, int position,
			String msg) {

		Logging.v("Строка: " + String.valueOf(stringNumber));
		Logging.v("Позиция: " + String.valueOf(position));
		Logging.v("Текст: " + msg);

		if (stringNumber > mLinesCount)
			stringNumber = mLinesCount;

		TextView textView = (TextView) mLinearLayout.getChildAt(stringNumber);

		// Создаем стрингбилдер старого сообщения
		String oldMsg = textView.getText().toString();

		Logging.v("Старое сообщение: " + oldMsg);

		StringBuilder builder = new StringBuilder(oldMsg);

		// Высчитываем итоговую длину строки
		int finalStringLength = position + msg.length();

		Logging.v("Финальная длина строки: "
				+ String.valueOf(finalStringLength));

		// Если итоговая длина строки больше, чем старая строка
		// добиваем нужное количество пробелов
		while (builder.length() < finalStringLength) {
			builder.append(' ');
		}

		// Меняем нужные символы в строке
		builder.replace(position, position + msg.length(), msg);

		Logging.v("Итоговая строка: " + builder.toString());

		// Добавляем новую строку в нужное место
		textView.setText(builder.toString());
	}

	/**
	 * Обработка полученного сообщения
	 * 
	 * @param msg
	 *            Сообщение
	 */
	private void processMessage(String msg) {
		String original_message = msg;
		// Отлов неправильных форматов сообщения
		if (msg.trim().length() == 0
				|| msg.length() < 2
				|| msg.charAt(0) != '@'
				|| (msg.charAt(1) != 'E' && msg.charAt(1) != 'C' && msg
						.charAt(1) != 'P')) {
			if (Globals.DEBUG_MODE) {
				Notifications.showError(this, "Неправильный формат сообщения: "
						+ original_message);
			}
			return;
		}

		// Сообщение @E - стереть экран
		if (msg.charAt(1) == 'E') {
			clearAll();
		} else if (msg.charAt(1) == 'C') {
			// Сообщение вида @C[N][пробел][текст]
			// Необходимо распарсить [N] и [текст]

			// Удаление "@C" из строки
			msg = msg.substring(2);

			// [N] в string
			String stringNumber = "";

			// Парсинг [N]
			for (int i = 0; i < msg.length()
					&& Character.isDigit(msg.charAt(0));) {
				stringNumber = stringNumber + msg.charAt(0);
				msg = msg.substring(1);
			}

			// [N] в int
			int parsedStringNumber = -1;

			try {
				parsedStringNumber = Integer.parseInt(stringNumber);
			} catch (Exception e) {
				if (Globals.DEBUG_MODE) {
					Notifications.showError(this,
							"Неправильный формат сообщения: "
									+ original_message);
				}
				e.printStackTrace();
				return;
			}

			if (parsedStringNumber == -1) {
				Notifications.showError(this, "Неправильный формат сообщения: "
						+ original_message);
				return;
			}

			// Парсинг [текст]
			if (msg.length() > 0) {
				// Сообщение вида @CN[пробел][текст]
				// Удаление [пробел]
				msg = msg.substring(1);
				// Запись [текст] в строку [N]
				addMessageToString(parsedStringNumber, msg);
			} else {
				// Команда вида @CN
				// Стирание строки N
				clearString(parsedStringNumber);
			}
		} else if (msg.charAt(1) == 'P') {
			// Сообщение вида @P[X],[N][пробел][текст]
			// Необходимо распарсить [X],[N] и [текст]

			// Удаление "@P" из строки
			msg = msg.substring(2);

			// Считывание [X]

			// [X] в string
			String positionNumber = "";

			// Парсинг [X]
			for (int i = 0; i < msg.length()
					&& Character.isDigit(msg.charAt(0));) {
				positionNumber = positionNumber + msg.charAt(0);
				msg = msg.substring(1);
			}

			// [X] в int
			int parsedPositionNumber = -1;

			try {
				parsedPositionNumber = Integer.parseInt(positionNumber);
			} catch (Exception e) {
				if (Globals.DEBUG_MODE) {
					Notifications.showError(this,
							"Неправильный формат сообщения: "
									+ original_message);
				}
				e.printStackTrace();
				return;
			}

			// Считывание [N]

			// Удаление "," из строки
			msg = msg.substring(1);

			// [N] в string
			String stringNumber = "";

			// Парсим номер строки
			for (int i = 0; i < msg.length()
					&& Character.isDigit(msg.charAt(0));) {
				stringNumber = stringNumber + msg.charAt(0);
				msg = msg.substring(1);
			}

			// [N] в int
			int parsedStringNumber = -1;

			try {
				parsedStringNumber = Integer.parseInt(stringNumber);
			} catch (Exception e) {
				if (Globals.DEBUG_MODE) {
					Notifications.showError(this,
							"Неправильный формат сообщения: "
									+ original_message);
				}
				e.printStackTrace();
				return;
			}

			if (parsedStringNumber == -1) {
				// Logging.v("Ошибка при попытке парсинга строки " +
				// stringNumber);
				return;
			}

			// Парсинг [текст]
			if (msg.length() > 0) {
				// Удаление [пробел]
				msg = msg.substring(1);
				// Запись [текст] в строку [N] в позицию [X]
				addMessageToStringFromPosition(parsedStringNumber,
						parsedPositionNumber, msg);
			} else {
				if (Globals.DEBUG_MODE) {
					Notifications.showError(this,
							"Неправильный формат сообщения: "
									+ original_message);
				}
			}
		} // end of if-elseif
	} // end of processmessage
}