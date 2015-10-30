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
 * <p>�������� ���������������� ������. ������ �������� ����������, ����� ������������ �����
 * �� ������ ��� �� ����� ������ ���� ���������������� ������
 * (��. {@link com.isosystem.smarthouse.MainFormattedScreensFragment}).</p>
 * <p>� �������� extras ������� �������� ���������� ����� ���������� ���� ����������������
 * ������ �� ������, ������� ��������� � {@link com.isosystem.smarthouse.data.FormattedScreens}.
 * ���� ����� ���������� ���� ������, ����� �������� ������ {@link #mScreen} ���� ����������������
 * ������, ������� �������� ��������� ��� ������ �������� � ���������
 * ��� ��������� �������� ������ ��� ���������������� ������.</p>
 * <p>��� ������ ������� ���������������� ������ �������������� ������� {@link #mReceiver}.
 * ������� ��������� ����������������� ��������� � �������� ���������������� ������.
 * ����� �����, ������� ���������� � ����� {@link #processMessage(String)}, ��� ����������
 * ��������� ������� � ����� ��������� �� �����</p>
 *
 * <p>� ���������� ����������
 * (��. {@link com.isosystem.smarthouse.settings.ApplicationPreferencesActivity}) �����
 * ���������� ������ ������, ���������� ����� � ���� � ����� ������, ������� ����������
 * �� ������. ��� �������� ��������� ��������������� ����� ��� ������ ������.
 * ��� ������ ��������, �� �������� �����������:
 *     <ul><li>{@link #mFontSize} - ������ ������</li>
 *     <li>{@link #mLinesCount} - ���������� ����� � ����</li>
 *     </ul>
 * <br>���� � ������� ���������������� ������ ����� ������ ��������� �������� � ���������� ����������
 * �����, ���������� ��������� � ��������� ������.
 * ���� � ������� ���������������� ������ ������� ������ � ������ ��������� ���������� ��������,
 * ������� ���������� �� ������ (��������, ��� ������ ������� ������, �� ������ ����������� 45
 * ��������, � ������� �������� ����� ����� � 60�� �������), �� ��� ����� �� ������������ �
 * ���������� ����� �������� �� ��������� ������.
 * </p>
 *
 * @see com.isosystem.smarthouse.MainFormattedScreensAdapterGrid
 * @see com.isosystem.smarthouse.MainFormattedScreensAdapterList
 * @see com.isosystem.smarthouse.MainFormattedScreensFragment
 */
public class FormattedScreensActivity extends Activity {
	MyApplication mApplication;
	Context mContext;

    /** ��������� ��� ����� ���� */
    LinearLayout mLinearLayout;

	ScrollView mScrollView;
    /** ������� �������� ���� */
	Button mBackButton;

    /** ������� ��� ��������� ������ ���������������� ������ */
	FormScreenMessageReceiver mReceiver;
    /** ������ �������� ���� ���������������� ������ �� ������ ���� */
	FormattedScreen mScreen;

    /** ������ ���������� ��� �������� ��������� ����������� */
    MessageDispatcher mDispatcher;

    /** ��������� ������ ������ ��� ��������� */
	float mFontSize = 30;
    /** ��������� ���������� ����� ��� ��������� */
	int mLinesCount = 9;

	Boolean mScrollableWindow = false;

    /**
     * ��� ������ ��������:
     * <br>1) ��������������� ������������� ����� � ������ �� ���������� ������ �� ����-����
     * <br>2) ����������� ��������� (���������� �����, ������ ������, ���������� �������� � ������)
     * <br>3) �������������� {@link #mReceiver} ��� ��������� ������ ���������������� ������
     * <br>4) �� extras ����������� ����� ����, ���������� ��������� �������
     * {@link com.isosystem.smarthouse.data.FormattedScreen}
     * <br>5) ��������������� ��������� ��� ������ "X"
     * <br>6) � ������� {@link #mDispatcher} ���������� ��������� �����������, ����� ��� �����
     * �������� ������� ���������������� ������ ��� ������������� ����
     *
     * @param savedInstanceState
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mContext = this;
        mApplication = (MyApplication) getApplicationContext();

        // ��������� ���������� ������
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // ���������� action � status bar
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


//		// ��������� ������ ������
//		try {
//			mFontSize = Float.parseFloat(prefs.getString(
//					"formatted_screen_font_size", "30"));
//		} catch (Exception e) {
//			Logging.v("���������� ��� ������� ������� �������� ������� ���������� ������ �� preferences");
//			e.printStackTrace();
//		}
//		Logging.v("������ ������:[" + mFontSize +"]");

//		// ���������� ����� � ����
//		try {
//			mLinesCount = Integer.parseInt(prefs.getString(
//					"formatted_screen_lines_count", "9"));
//		} catch (Exception e) {
//			Logging.v("���������� ��� ������� ������� �������� ������� ���������� ������ �� preferences");
//			e.printStackTrace();
//		}
//		Logging.v("���������� �����:[" + mLinesCount + "]");

		// ���������� ������ ���� ���������������� ������ �� extras
		// ��� ����������, ����� ����� ������� ��� ������ � ��� ��������� ���������������� ������
		int position = -1;
		try {
			position = getIntent().getIntExtra("formScreenIndex", -1);
		} catch (Exception e) {
			Logging.v("���������� ��� ������� ����� ����� ���� �� extras");
			e.printStackTrace();
			finish();
			overridePendingTransition(R.anim.flipin,R.anim.flipout);
		}
		Logging.v("����� ���� ���������������� ������:[" + position +"]");

		// ��������� ������� ���� �� ������ ����
		try {
			mScreen = ((MyApplication) getApplicationContext()).mFormattedScreens.mFormattedScreens
					.get(position);
		} catch (Exception e) {
			Logging.v("���������� ��� ������� ����� ���� ����� "
					+ String.valueOf(position)
					+ " . ��������, ����� ������� ���.");
			e.printStackTrace();
			finish();
			overridePendingTransition(R.anim.flipin,R.anim.flipout);
		}

		// ����� ���-������� ���������� ���� ������
		HashMap<String, String> pMap = mScreen.paramsMap;

		// ��������� ������� ������ � ��������
		mFontSize = (float)setFontSize(prefs,pMap);

		if (pMap.get("LinesNumber") != null) {
			try {
				mLinesCount = Integer.parseInt(pMap.get("LinesNumber").toString());
			} catch (NumberFormatException e) {
				Logging.v("���������� ��� ������� �������� ���������� �����");
				e.printStackTrace();
			}
		}

		// ��������� ����
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
				Notifications.showError(mContext, "���������� ������ ��� ������� ������� ������");
				Logging.v("���������� ��� ������� �������� ������ �������� �����. ��������: ScrollableWindow");
			}
		}

        // ��������� ����� ��� ����
        setLines(mLinesCount);

        // ����������� ��������
		try {
			mReceiver = new FormScreenMessageReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Globals.BROADCAST_INTENT_FORMSCREEN_MESSAGE);
			filter.addAction(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE);
			registerReceiver(mReceiver, filter);
			Logging.v("������������ ������� FormScreen");
		} catch (Exception e) {
			Logging.v("���������� ��� ������� ���������������� �������");
			e.printStackTrace();
			finish();
			overridePendingTransition(R.anim.flipin,R.anim.flipout);
		}

        // �������� ����
		ImageButton mBackButton = (ImageButton) findViewById(R.id.frm_backbutton);
		mBackButton.setOnClickListener(mBackListener);

        // ��������� ��� �������� ��������� �����������
		mDispatcher = new MessageDispatcher(this);
        // �������� ������� �� ����� ���������������� ������
		mDispatcher.sendGiveMeValueMessage(mScreen.mInputStart,true);

		//setExampleText();
	}

	/**
	 * ��������� ������
	 * 1) ��������� ������ �� ����
	 * 2) ���� ������ �������� �� ������� - ���������� �� �������� ��������� ��������
	 *
	 * @param prefs ���������
	 * @param map ���������
	 * @return ������ ������ � ��������
	 */
	private int setFontSize(SharedPreferences prefs, HashMap<String, String> map) {
		int fontSize = -1;

		// ��������� �� ����
		if (map.get("FontSize") != null) {
			try {
				fontSize = Integer.parseInt(map.get("FontSize").toString());
			} catch (NumberFormatException e) {
				Logging.v("���������� ��� ������� �������� ������� ������");
				e.printStackTrace();
			}
		}

		// ���� ������� �� �������, ����� ��������� �������� �� ��������
		if (fontSize == -1) {
			try {
				fontSize = Integer.parseInt(prefs.getString(
						"formatted_screen_font_size", "30"));
			} catch (Exception e) {
				Logging.v("���������� ��� ������� ������� �������� ������� ���������� ������ �� preferences");
				e.printStackTrace();
			}
		}

		return fontSize;
	}

	/**
     * ����� ��������� � {@link #mLinearLayout} {@link #mLinesCount} TextView.
     * <br>���������� �� {@link #mLinearLayout} ��������� ������ ����� {@link #mBackButton},
     * ����� ���� � ����� ����������� ������ ���������� TextView.
     * <br> ����� �����, {@link #mBackButton} ����������� ������� � {@link #mLinearLayout}.
	 * @param lines ���������� �����
	 */
	private void setLines(int lines) {
		Typeface font = Typeface.createFromAsset(getAssets(), "PTM75F.ttf");
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
        // ������ "�����", ��� ��������� �� �������� � ����������� � �����, ����� ���������� �����
		mLinearLayout = (LinearLayout) findViewById(R.id.LinearLayout1);
		View backButtonView = mLinearLayout.getChildAt(0);
		mLinearLayout.removeViewAt(0);

        // ���������� �����
		for (int i=0;i<lines;i++) {						
			TextView textview = new TextView(this);
			textview.setMaxLines(1);
			textview.setSingleLine();
			textview.setTypeface(font);
			textview.setTextColor(Color.WHITE);
			textview.setTextSize(mFontSize);
			textview.setPadding(15, 0, 0, 0);
			textview.setLayoutParams(params);
			// ���������� ������
			mLinearLayout.addView(textview);
		}
		//���������� ������
		mLinearLayout.addView(backButtonView);
	}

    /**
     * ����� ������������ ������� ���� �����.
     */
	private void clearAll() {
		TextView textView;
		for (int i = 0; i < mLinesCount; i++) {
			textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText("");
		}
	}

	/**
     * ����� ������������ ������� i-�� ������.
     * ���������� ���������� �������� �� ����� ������ (���� i ������
     * {@link #mLinesCount}, ����� i = {@link #mLinesCount}).
	 * @param i ����� ������
	 */
	private void clearString(int i) {
        // ���� ������ ���������� ���������� �����
        // ������� ��������� ������
		if (i > mLinesCount)
			i = mLinesCount;

		Logging.v("������� ������ :[" + i +"]");
		try {
			TextView textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText("");
		} catch (Exception e) {
			Logging.v("���������� ��� ������� �������� ������ "
					+ String.valueOf(i));
			e.printStackTrace();
		}
	}

	/**
	 * ����� ��������� ��������� � ������ i � ������.<br>
     * ���������� ���������� �������� �� ����� ������ (���� i ������
     * {@link #mLinesCount}, ����� i = {@link #mLinesCount}).
     * <br>����� ���������� ����� {@link #clearString(int)},
     * ������� ������� i-�� �����, ����� ���� � ������
     * ����������� ���������
     *
	 * @param i ����� ������
	 * @param msg ���������
	 */
	private void addMessageToString(int i, String msg) {
		if (i > mLinesCount)
			i = mLinesCount;
		
		Logging.v("�����:[" + i +"]. ���������:[" + msg +"]");
		
		try {
			clearString(i);
			TextView textView = (TextView) mLinearLayout.getChildAt(i);
			textView.setText(msg);
		} catch (Exception e) {
			Logging.v("���������� ��� ������� �������� ����� � ������ "
					+ String.valueOf(i));
			e.printStackTrace();
		}
	}

	/**
	 * ���������� ��������� � ������ � ������������ �������.
     * <br>���������� ���������� �������� �� ����� ������ (���� stringNumber ������
     * {@link #mLinesCount}, ����� stringNumber = {@link #mLinesCount}).
     * <br>�����, ���������� ���������, �������� �� ���������� ���������
     * � ���������� ����� ������.
     * <br>���� �� - ����� ������ ���������� ��������� �� ������ �����, ����� �����, �
     * ������ ���������� ���������.
     *
	 * @param stringNumber ����� ������
	 * @param position ����� �������
	 * @param msg ���������
	 */
	private void addMessageToStringFromPosition(int stringNumber, int position,
			String msg) {
		
		Logging.v("������: " + String.valueOf(stringNumber));
		Logging.v("�������: " + String.valueOf(position));
		Logging.v("�����: " + msg);
		
		if (stringNumber > mLinesCount)
			stringNumber = mLinesCount;
		
		TextView textView = (TextView) mLinearLayout.getChildAt(stringNumber);
		
		// ������� ������������ ������� ���������
		String oldMsg = textView.getText().toString();
		
		Logging.v("������ ���������: " + oldMsg);

        // ������������ �������� ������
		StringBuilder builder = new StringBuilder(oldMsg);

		// ����������� �������� ����� ������
		int finalStringLength = position + msg.length();

		Logging.v("��������� ����� ������: " + String.valueOf(finalStringLength));
		
		// ���� �������� ����� ������ ������, ��� ������ ������
		// �������� ������ ���������� ��������
		while (builder.length() < finalStringLength) {
			builder.append(' ');
		}

		// ������ ������ ������� � ������
		builder.replace(position, position + msg.length(), msg);

		Logging.v("�������� ������: " + builder.toString());
		
		// ��������� ����� ������ � ������ �����
		textView.setText(builder.toString());
	}

	/**
	 * ����� ��� ��������� ���������� �������.
     * ��������� ���������� ��������� �������:
     * <ol>
     *     <li>���������� ���������� ���������� �������.</li>
     *     <li>����������� ���� ������� (��� ����� ����������� ������ ������):
     *      <ul>
     *       <li>E - ����� ������ ������� @E ������� ���� �����. ���������� ����� {@link #clearAll()} ��� ������� ���� �����</li>
     *       <li>C - ����� ������ ������� ������ ������ � ������ ������ N. ��� ������� ���� @C[N][������][�����].
     *       ����� ����������: <br>�) �������� @C ;<br>�) ������� [N] ;<br>�) �������� [������] ;<br>�) ������� [�����] . <br>����� �����
     *       [N] � [�����] ���������� � ����� {@link #addMessageToString(int, String)}.</li>
     *       <li>P - ����� ������ ������� ������ ������ � ������ N � ������� X. ��� ������� ���� @P[X],[N][������][�����].
     *       ����� ����������: <br>�) �������� @P ;<br>�) ������� [X] ;<br>�) �������� [,] ;
     *       <br>�) ������� [N] ;<br>�) �������� [������] ;<br>e) ������� [�����] . <br>����� �����
     *       [X],[N] � [�����] ���������� � ����� {@link #addMessageToStringFromPosition(int, int, String)}.</li>
     *      </ul>
     *     </li>
     * </ol>
     *
	 * @param msg ��������� �������
	 */
	private void processMessage(String msg) {
		String original_message = msg;
		// ����� ������������ �������� ���������
		if (msg.trim().length() == 0
				|| msg.length() < 2
				|| msg.charAt(0) != '@'
				|| (msg.charAt(1) != 'E' && msg.charAt(1) != 'C' && msg
						.charAt(1) != 'P')) {
			if (Globals.DEBUG_MODE) {
				Logging.v("��������:[" + original_message + "]");
				Notifications.showError(this, "������������ ������ ���������: "
						+ original_message);
			}
			return;
		}

		// ��������� @E - ������� �����
		if (msg.charAt(1) == 'E') {
			Logging.v("������� ���� �����");
			clearAll();
		} else if (msg.charAt(1) == 'C') {
			// ��������� ���� @C[N][������][�����]
			// ���������� ���������� [N] � [�����]

			// �������� "@C" �� ������
			msg = msg.substring(2);
			
			Logging.v("������� @C:[" + msg + "]");

			// [N] � string
			String stringNumber = "";

			// ������� [N]
			for (int i = 0; i < msg.length()
					&& Character.isDigit(msg.charAt(0));) {
				stringNumber = stringNumber + msg.charAt(0);
				msg = msg.substring(1);
			}
			
			Logging.v("����� ������:[" + stringNumber + "]. ��������: [" + msg +"]");

			// [N] � int
			int parsedStringNumber = -1;

			try {
				parsedStringNumber = Integer.parseInt(stringNumber);
			} catch (Exception e) {
				if (Globals.DEBUG_MODE) {
					Notifications.showError(this,
							"������������ ������ ���������: "
									+ original_message);
				}
				e.printStackTrace();
				return;
			}

			if (parsedStringNumber == -1) {
				Notifications.showError(this, "������������ ������ ���������: "
						+ original_message);
				return;
			}

			// ������� [�����]
			if (msg.length() > 0) {
				// ��������� ���� @CN[������][�����]
				// �������� [������]
				msg = msg.substring(1);
				
				Logging.v("�������� �������:[" + msg +"]");
				
				// ������ [�����] � ������ [N]
				addMessageToString(parsedStringNumber, msg);
			} else {
				// ������� ���� @CN
				// �������� ������ N
				clearString(parsedStringNumber);
			}
		} else if (msg.charAt(1) == 'P') {
			// ��������� ���� @P[X],[N][������][�����]
			// ���������� ���������� [X],[N] � [�����]

			// �������� "@P" �� ������
			msg = msg.substring(2);

			// ���������� [X]

			// [X] � string
			String positionNumber = "";

			// ������� [X]
			for (int i = 0; i < msg.length()
					&& Character.isDigit(msg.charAt(0));) {
				positionNumber = positionNumber + msg.charAt(0);
				msg = msg.substring(1);
			}

			// [X] � int
			int parsedPositionNumber = -1;

			try {
				parsedPositionNumber = Integer.parseInt(positionNumber);
			} catch (Exception e) {
				if (Globals.DEBUG_MODE) {
					Notifications.showError(this,
							"������������ ������ ���������: "
									+ original_message);
				}
				e.printStackTrace();
				return;
			}

			// ���������� [N]

			// �������� "," �� ������
			msg = msg.substring(1);

			// [N] � string
			String stringNumber = "";

			// ������ ����� ������
			for (int i = 0; i < msg.length()
					&& Character.isDigit(msg.charAt(0));) {
				stringNumber = stringNumber + msg.charAt(0);
				msg = msg.substring(1);
			}

			// [N] � int
			int parsedStringNumber = -1;

			try {
				parsedStringNumber = Integer.parseInt(stringNumber);
			} catch (Exception e) {
				if (Globals.DEBUG_MODE) {
					Notifications.showError(this,
							"������������ ������ ���������: "
									+ original_message);
				}
				e.printStackTrace();
				return;
			}

			if (parsedStringNumber == -1) {
				// Logging.v("������ ��� ������� �������� ������ " +
				// stringNumber);
				return;
			}

			// ������� [�����]
			if (msg.length() > 0) {
				// �������� [������]
				msg = msg.substring(1);
				// ������ [�����] � ������ [N] � ������� [X]
				addMessageToStringFromPosition(parsedStringNumber,
						parsedPositionNumber, msg);
			} else {
				if (Globals.DEBUG_MODE) {
					Notifications.showError(this,
							"������������ ������ ���������: "
									+ original_message);
				}
			}
		} // end if-elseif
	} // end method

    /**
     * ��������� ��� {@link #mBackButton}.
     * �������� ���������� ��� ��������, � ���������.
     */
	private OnClickListener mBackListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
			overridePendingTransition(R.anim.flipin,R.anim.flipout);
		}
	};

    /**
     * ������� ��������� ����������� � ����������� ���������������� ������
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
	 * �������������� �������� ���� ���������������� ������: <br />
	 * 1. � ������� ����������� ��������� ���� ����� � ��������� �� �����������; <br />
	 * 2. ���� ����� �������, �������� ��������� ������ � INT; <br />
	 * 3. �������� ��������� � ������������� ������� ���� ���������������� ������. <br />
	 * @param message ��������� �� �����������
	 */
	private void forcedFormattedScreenStart(String message){
		String formScreenNumber = "";
		// ��������� ����� ���� � ���������
		Pattern p = Pattern.compile("[0-9]+");
		Matcher m = p.matcher(message);
		while (m.find()) {
			formScreenNumber = m.group();
		}

		// ������ ����� ���� � �������� ���������
		try {
			int screen_number = Integer.parseInt(formScreenNumber);
			if (screen_number >= 0 && screen_number < mApplication.mFormattedScreens.mFormattedScreens.size()) {
				// ����� ���� ����������, �������� ���������
				mDispatcher = new MessageDispatcher(this);
				mDispatcher.sendGiveMeValueMessage(mApplication.mFormattedScreens.mFormattedScreens.get(screen_number).mCannotOpenWindowMessage,true);
			} else {
				throw new NumberFormatException("format screen number is out of bounds");
			}
		} catch (NumberFormatException e) {
			// ��������� ������ � �������� ������� (�� ������ ����� �����)
			// ��� ������ ������� �� ������� ���������� ���� ������
			Intent i = new Intent();
			String alarmMessage = "�������� ��������� � ���������������� ������";
			mApplication.mAlarmMessages.addAlarmMessage(
					mApplication, alarmMessage,
					Notifications.MessageType.ControllerMessage);
			// ������ ���������
			i.setAction(Globals.BROADCAST_INTENT_ALARM_MESSAGE);
			mApplication.sendBroadcast(i);

			Logging.v("���������� ��� ������� �������� ������ ���� ���������������� ������." +
					"������ ��������: " + formScreenNumber);
			e.printStackTrace();
		}
	}

    /**
     * ����� �������� ��� ��������� ������ ���������������� ������.
     * <br>����� ������� �������, ���������� ����� {@link #processMessage(String)},
     * ������� ������������ ��������� �������.
     */
	class FormScreenMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// �������� ���������. ��� ������ ���� ���������� �������� � ������
			// ����������� ������ ����� �������
			String msg = intent.getStringExtra("message");

			// ���� �������������� �������� ����, �������� ����� � �������� ���
			// extra � ���� ��������� �� �����������
			// ���� ������� ��������� ���������������� ������ - ������������
			if (intent.getAction().equals(Globals.BROADCAST_INTENT_FORCED_FORMSCREEN_MESSAGE)) {
				forcedFormattedScreenStart(msg);
				return;
			} else  if (intent.getAction().equals(Globals.BROADCAST_INTENT_FORMSCREEN_MESSAGE)) {
				try {
					processMessage(msg);
				} catch (Exception e) {
					Notifications.showError(mContext,
							"������������ ������ ��������� " + msg);
					e.printStackTrace();
				}
			}
		}
	} // end BroadcastReceiver
}