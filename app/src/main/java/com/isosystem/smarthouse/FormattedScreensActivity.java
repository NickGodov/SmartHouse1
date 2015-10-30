package com.isosystem.smarthouse;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystem.smarthouse.connection.MessageDispatcher;
import com.isosystem.smarthouse.data.FormattedScreen;
import com.isosystem.smarthouse.data.MenuTreeNode;
import com.isosystem.smarthouse.logging.Logging;
import com.isosystem.smarthouse.notifications.Notifications;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Активити форматированного вывода. Данное активити вызывается, когда пользователь нажал
 * на плитку или на пункт списка окон форматированного вывода
 * (см. {@link com.isosystem.smarthouse.MainFormattedScreensFragment}).</p>
 * <p>В качестве extras данному активити передается номер выбранного окна форматированного
 * вывода из списка, который находится в {@link com.isosystem.smarthouse.data.FormattedScreens}.
 * Зная номер выбранного окна вывода, можно получить объект {@link #mScreen} окна форматированного
 * вывода, который содержит сообщение для начала передачи и сообщение
 * для окончания передачи команд для форматированного вывода.</p>
 * <p>Для приема команды форматированного вывода регистрируется ресивер {@link #mReceiver}.
 * Ресивер принимает широковещательное сообщение с командой форматированного вывода.
 * После этого, команда передается в метод {@link #processMessage(String)}, где происходит
 * обработка команды и вывод сообщения на экран</p>
 *
 * <p>В настройках приложения
 * (см. {@link com.isosystem.smarthouse.settings.ApplicationPreferencesActivity}) можно
 * установить размер шрифта, количество строк в окне и длину строки, которая помещается
 * на экране. Это позволят настроить форматированный вывод под разные экраны.
 * При старте активити, из настроек считывается:
 *     <ul><li>{@link #mFontSize} - размер шрифта</li>
 *     <li>{@link #mLinesCount} - количество строк в окне</li>
 *     </ul>
 * <br>Если в команде форматированного вывода номер строки превышает заданное в настройках количество
 * строк, информация выводится в последней строке.
 * Если в команде форматированного вывода позиция вывода в строке превышает количество символов,
 * которое помещается на экране (например, при данном размере шрифта, на экране поемещается 45
 * символов, а команда содержит вывод слова с 60го символа), то это никак не регулируется и
 * информация будет показана за пределами экрана.
 * </p>
 *
 * @see com.isosystem.smarthouse.MainFormattedScreensAdapterGrid
 * @see com.isosystem.smarthouse.MainFormattedScreensAdapterList
 * @see com.isosystem.smarthouse.MainFormattedScreensFragment
 */
public class FormattedScreensActivity extends Activity {
	MyApplication mApplication;
	Context mContext;

    /** Контейнер для строк окна */
    LinearLayout mLinearLayout;

	ScrollView mScrollView;
    /** Кнпопка закрытие окна */
	Button mBackButton;

    /** Ресивер для получения команд форматированного вывода */
	FormScreenMessageReceiver mReceiver;
    /** Объект текущего окна форматированного вывода из списка окон */
	FormattedScreen mScreen;

    /** Объект диспетчера для отправки сообщений контроллеру */
    MessageDispatcher mDispatcher;

    /** Дефолтный размер шрифта для сообщений */
	float mFontSize = 30;
    /** Дефолтное количество строк для сообщений */
	int mLinesCount = 9;

	Boolean mScrollableWindow = false;

    /**
     * При старте активити:
     * <br>1) Устанавливается полноэкранный режим и запрет на отключение экрана по тайм-ауту
     * <br>2) Считываются настройки (количество строк, размер шрифта, количество символов в строке)
     * <br>3) Регистрируется {@link #mReceiver} для получения команд форматированного вывода
     * <br>4) из extras считывается номер окна, происходит получение объекта
     * {@link com.isosystem.smarthouse.data.FormattedScreen}
     * <br>5) Устанавливается слушатель для кнопки "X"
     * <br>6) С помощью {@link #mDispatcher} отсылается сообщение контроллеру, чтобы тот начал
     * отсылать команды форматированного вывода для определенного окна
     *
     * @param savedInstanceState
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mContext = this;
        mApplication = (MyApplication) getApplicationContext();

        // Запретить отключение экрана
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Отключение action и status bar
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setSystemUiVisibility(8);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mApplication);


//		// Считываем размер шрифта
//		try {
//			mFontSize = Float.parseFloat(prefs.getString(
//					"formatted_screen_font_size", "30"));
//		} catch (Exception e) {
//			Logging.v("Исключение при попытке считать значение периода обновления буфера из preferences");
//			e.printStackTrace();
//		}
//		Logging.v("Размер шрифта:[" + mFontSize +"]");

//		// Количество строк в окне
//		try {
//			mLinesCount = Integer.parseInt(prefs.getString(
//					"formatted_screen_lines_count", "9"));
//		} catch (Exception e) {
//			Logging.v("Исключение при попытке считать значение периода обновления буфера из preferences");
//			e.printStackTrace();
//		}
//		Logging.v("Количество строк:[" + mLinesCount + "]");

		// Считывание номера окна форматированного вывода из extras
		// Это необходимо, чтобы взять команду для старта и для окончания форматированного вывода
		int position = -1;
		try {
			position = getIntent().getIntExtra("formScreenIndex", -1);
		} catch (Exception e) {
			Logging.v("Исключение при попытке взять номер окна из extras");
			e.printStackTrace();
			finish();
			overridePendingTransition(R.anim.flipin,R.anim.flipout);
		}
		Logging.v("Номер окна форматированного вывода:[" + position +"]");

		// Получение объекта окна из списка окон
		try {
			mScreen = ((MyApplication) getApplicationContext()).mFormattedScreens.mFormattedScreens
					.get(position);
		} catch (Exception e) {
			Logging.v("Исключение при попытке взять окно номер "
					+ String.valueOf(position)
					+ " . Возможно, такой позиции нет.");
			e.printStackTrace();
			finish();
			overridePendingTransition(R.anim.flipin,R.anim.flipout);
		}

		// Берем хеш-таблицу параметров окна вывода
		HashMap<String, String> pMap = mScreen.paramsMap;

		// Установка размера шрифта в пикселях
		mFontSize = (float)setFontSize(prefs,pMap);

		if (pMap.get("LinesNumber") != null) {
			try {
				mLinesCount = Integer.parseInt(pMap.get("LinesNumber").toString());
			} catch (NumberFormatException e) {
				Logging.v("Исключение при попытке парсинга количества строк");
				e.printStackTrace();
			}
		}

		// Прокрутка окна
		if (pMap.get("ScrollableWindow")!=null){
			if (pMap.get("ScrollableWindow").equals("0")){
				mScrollableWindow = false;
				setContentView(R.layout.activity_formscreen);
			} else if (pMap.get("ScrollableWindow").equals("1")) {
				mScrollableWindow = true;
				setContentView(R.layout.activity_formscreen_scrollable);
				mScrollView = (ScrollView) findViewById(R.id.scrollview);
				mScrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
			} else {
				Notifications.showError(mContext, "Внутренняя ошибка при попытке считать данные");
				Logging.v("Исключение при попытке парсинга данных конечной точки. Параметр: ScrollableWindow");
			}
		}

        // Установка строк для окна
        setLines(mLinesCount);

        // Подключение ресивера
		try {
			mReceiver = new FormScreenMessageReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Globals.BROADCAST_INTENT_FORMSCREEN_MESSAGE);
			filter.addAction(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE);
			registerReceiver(mReceiver, filter);
			Logging.v("Регистрируем ресивер FormScreen");
		} catch (Exception e) {
			Logging.v("Исключение при попытке зарегистрировать ресивер");
			e.printStackTrace();
			finish();
			overridePendingTransition(R.anim.flipin,R.anim.flipout);
		}

        // Закрытие окна
		ImageButton mBackButton = (ImageButton) findViewById(R.id.frm_backbutton);
		mBackButton.setOnClickListener(mBackListener);

        // Диспетчер для отправки сообщения контроллеру
		mDispatcher = new MessageDispatcher(this);
        // Отправка команды на старт форматированного вывода
		mDispatcher.sendGiveMeValueMessage(mScreen.mInputStart,true);

		//setExampleText();
	}

	/**
	 * Установка шрифта
	 * 1) Получение данных из хеша
	 * 2) Если данные получить не удалось - вытягиваем из настроек дефолтные значения
	 *
	 * @param prefs настройки
	 * @param map параметры
	 * @return размер шрифта в пикселях
	 */
	private int setFontSize(SharedPreferences prefs, HashMap<String, String> map) {
		int fontSize = -1;

		// Считываем из хеша
		if (map.get("FontSize") != null) {
			try {
				fontSize = Integer.parseInt(map.get("FontSize").toString());
			} catch (NumberFormatException e) {
				Logging.v("Исключение при попытке парсинга размера шрифта");
				e.printStackTrace();
			}
		}

		// Если считать не удалось, берем дефолтные значения из настроек
		if (fontSize == -1) {
			try {
				fontSize = Integer.parseInt(prefs.getString(
						"formatted_screen_font_size", "30"));
			} catch (Exception e) {
				Logging.v("Исключение при попытке считать значение периода обновления буфера из preferences");
				e.printStackTrace();
			}
		}

		return fontSize;
	}

	/**
     * Метод добавляет в {@link #mLinearLayout} {@link #mLinesCount} TextView.
     * <br>Изначально из {@link #mLinearLayout} изымается кнопка назад {@link #mBackButton},
     * после чего в цикле добавляются нужное количество TextView.
     * <br> После этого, {@link #mBackButton} добавляется обратно в {@link #mLinearLayout}.
	 * @param lines количество строк
	 */
	private void setLines(int lines) {
		Typeface font = Typeface.createFromAsset(getAssets(), "PTM75F.ttf");
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
        // Кнопка "назад", она изымается из разметки и вставляется в конец, после добавления строк
		mLinearLayout = (LinearLayout) findViewById(R.id.LinearLayout1);
		View backButtonView = mLinearLayout.getChildAt(0);
		mLinearLayout.removeViewAt(0);

        // Добавление строк
		for (int i=0;i<lines;i++) {						
			TextView textview = new TextView(this);
			textview.setMaxLines(1);
			textview.setSingleLine();
			textview.setTypeface(font);
			textview.setTextColor(Color.WHITE);
			textview.setTextSize(mFontSize);
			textview.setPadding(15, 0, 0, 0);
			textview.setLayoutParams(params);
			// Добавление строки
			mLinearLayout.addView(textview);
		}
		//Добавление кнопки
		mLinearLayout.addView(backButtonView);
	}

    /**
     * Метод осуществляет очистку всех строк.
     */
	private void clearAll() {
		TextView textView;
		for (int i = 0; i < mLinesCount; i++) {
			textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText("");
		}
	}

	/**
     * Метод осуществляет очистку i-ой строки.
     * Изначально происходит проверка на номер строки (если i больше
     * {@link #mLinesCount}, тогда i = {@link #mLinesCount}).
	 * @param i номер строки
	 */
	private void clearString(int i) {
        // Если индекс превыешает количество строк
        // берется последняя строка
		if (i > mLinesCount)
			i = mLinesCount;

		Logging.v("Очистка строки :[" + i +"]");
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
	 * Метод добавляет сообщение в строку i с начала.<br>
     * Изначально происходит проверка на номер строки (если i больше
     * {@link #mLinesCount}, тогда i = {@link #mLinesCount}).
     * <br>После вызывается метод {@link #clearString(int)},
     * который очищает i-ую стоку, после чего в строку
     * добавляется сообщение
     *
	 * @param i номер строки
	 * @param msg сообщение
	 */
	private void addMessageToString(int i, String msg) {
		if (i > mLinesCount)
			i = mLinesCount;
		
		Logging.v("Номер:[" + i +"]. Сообщение:[" + msg +"]");
		
		try {
			clearString(i);
			TextView textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText(msg);
		} catch (Exception e) {
			Logging.v("Исключение при попытке добавить текст в строку "
					+ String.valueOf(i));
			e.printStackTrace();
		}
	}

	/**
	 * Добавления сообщение в строку с определенной позиции.
     * <br>Изначально происходит проверка на номер строки (если stringNumber больше
     * {@link #mLinesCount}, тогда stringNumber = {@link #mLinesCount}).
     * <br>Далее, необходимо определит, приведет ли добавление сообщения
     * к увеличению длины строки.
     * <br>Если да - тогда строка добивается пробелами до нужной длины, после этого, в
     * строку внедряется сообщение.
     *
	 * @param stringNumber номер строки
	 * @param position номер позиции
	 * @param msg сообщение
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

        // Стрингбилдер итоговой строки
		StringBuilder builder = new StringBuilder(oldMsg);

		// Высчитываем итоговую длину строки
		int finalStringLength = position + msg.length();

		Logging.v("Финальная длина строки: " + String.valueOf(finalStringLength));
		
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
	 * Метод для обработки полученной команды.
     * Обработка происходит следующим образом:
     * <ol>
     *     <li>Изначально отсекаются невалидные команды.</li>
     *     <li>Определение типа команды (для этого считывается второй символ):
     *      <ul>
     *       <li>E - тогда пришла команда @E очистки всех строк. Вызывается метод {@link #clearAll()} для очистки всех строк</li>
     *       <li>C - тогда пришла команда вывода текста с начала строки N. Это команда вида @C[N][пробел][текст].
     *       Тогда происходит: <br>а) удаление @C ;<br>б) парсинг [N] ;<br>в) удаление [пробел] ;<br>г) парсинг [текст] . <br>После этого
     *       [N] и [текст] передаются в метод {@link #addMessageToString(int, String)}.</li>
     *       <li>P - тогда пришла команда вывода текста в строке N с позиции X. Это команда вида @P[X],[N][пробел][текст].
     *       Тогда происходит: <br>а) удаление @P ;<br>б) парсинг [X] ;<br>в) удаление [,] ;
     *       <br>г) парсинг [N] ;<br>д) удаление [пробел] ;<br>e) парсинг [текст] . <br>После этого
     *       [X],[N] и [текст] передаются в метод {@link #addMessageToStringFromPosition(int, int, String)}.</li>
     *      </ul>
     *     </li>
     * </ol>
     *
	 * @param msg пришедшая команда
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
				Logging.v("Неформат:[" + original_message + "]");
				Notifications.showError(this, "Неправильный формат сообщения: "
						+ original_message);
			}
			return;
		}

		// Сообщение @E - стереть экран
		if (msg.charAt(1) == 'E') {
			Logging.v("Стираем весь экран");
			clearAll();
		} else if (msg.charAt(1) == 'C') {
			// Сообщение вида @C[N][пробел][текст]
			// Необходимо распарсить [N] и [текст]

			// Удаление "@C" из строки
			msg = msg.substring(2);
			
			Logging.v("Удалено @C:[" + msg + "]");

			// [N] в string
			String stringNumber = "";

			// Парсинг [N]
			for (int i = 0; i < msg.length()
					&& Character.isDigit(msg.charAt(0));) {
				stringNumber = stringNumber + msg.charAt(0);
				msg = msg.substring(1);
			}
			
			Logging.v("Номер строки:[" + stringNumber + "]. Осталось: [" + msg +"]");

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
				
				Logging.v("Удаление пробела:[" + msg +"]");
				
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
		} // end if-elseif
	} // end method

    /**
     * Слушатель для {@link #mBackButton}.
     * Вызывает деструктор для активити, с анимацией.
     */
	private OnClickListener mBackListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
			overridePendingTransition(R.anim.flipin,R.anim.flipout);
		}
	};

    /**
     * Отсылка сообщения контроллеру о прекращении форматированного вывода
     */
	@Override
	public void onStop() {
		super.onStop();
		mDispatcher.sendGiveMeValueMessage(mScreen.mInputEnd,true);
		try {
			unregisterReceiver(mReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Принудительное открытие окна форматированного вывода: <br />
	 * 1. С помощью регулярного выражения ищем число в сообщении от контроллера; <br />
	 * 2. Если число найдено, пытаемся перевести строку в INT; <br />
	 * 3. Отсылаем сообщение о невозможности открыть окно форматированного вывода. <br />
	 * @param message сообщение от контроллера
	 */
	private void forcedFormattedScreenStart(String message){
		String formScreenNumber = "";
		// Считываем номер окна в сообщении
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(message);
		while (m.find()) {
			formScreenNumber = m.group();
		}

		// Парсим номер окна и отсылаем сообщение
		try {
			int screen_number = Integer.parseInt(formScreenNumber);
			if (screen_number >= 0 && screen_number < mApplication.mFormattedScreens.mFormattedScreens.size()) {
				// Номер окна корректный, отсылаем сообщение
				mDispatcher = new MessageDispatcher(this);
				mDispatcher.sendGiveMeValueMessage(mApplication.mFormattedScreens.mFormattedScreens.get(screen_number).mCannotOpenWindowMessage,true);
			} else {
				throw new NumberFormatException("format screen number is out of bounds");
			}
		} catch (NumberFormatException e) {
			// Сообщение пришло в неверном формате (не смогли найти число)
			// или индекс выходит за границы количества окон вывода
			Intent i = new Intent();
			String alarmMessage = "Неверное обращение к форматированному выводу";
			mApplication.mAlarmMessages.addAlarmMessage(
					mApplication, alarmMessage,
					Notifications.MessageType.ControllerMessage);
			// Кидаем броадкаст
			i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
			mApplication.sendBroadcast(i);

			Logging.v("Исключение при попытке парсинга номера окна форматированного вывода." +
					"Строка парсинга: " + formScreenNumber);
			e.printStackTrace();
		}
	}

    /**
     * Класс ресивера для получения команд форматированного вывода.
     * <br>После прихода команды, вызывается метод {@link #processMessage(String)},
     * который осуществляет обработку команды.
     */
	class FormScreenMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Получено сообщение. Оно должно быть обработано формулой с нужным
			// количеством знаков после запятой
			String msg = intent.getStringExtra("message");

			// Если принудительное открытие окна, вызываем метод и передаем ему
			// extra в виде сообщение от контроллера
			// Если обычное сообщение форматированного вывода - обрабатываем
			if (intent.getAction().equals(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE)) {
				forcedFormattedScreenStart(msg);
				return;
			} else  if (intent.getAction().equals(Globals.BROADCAST_INTENT_FORMSCREEN_MESSAGE)) {
				try {
					processMessage(msg);
				} catch (Exception e) {
					Notifications.showError(mContext,
							"Неправильный формат сообщения " + msg);
					e.printStackTrace();
				}
			}
		}
	} // end BroadcastReceiver
}