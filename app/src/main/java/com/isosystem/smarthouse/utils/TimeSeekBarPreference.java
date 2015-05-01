package com.isosystem.smarthouse.utils;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TimePicker;

import com.isosystem.smarthouse.R;

public final class TimeSeekBarPreference extends DialogPreference implements
		OnSeekBarChangeListener {

	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

	// Attribute names
	private static final String ATTR_DEFAULT_VALUE = "defaultValue";

	// Real defaults
	private final String mDefaultValue;

	private TimePicker mStartTimePicker;
	private TimePicker mEndTimePicker;

	// Current value

	public TimeSeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Read parameters from attributes
		mDefaultValue = attrs.getAttributeValue(ANDROID_NS, ATTR_DEFAULT_VALUE);
	}

	@Override
	protected View onCreateDialogView() {

		// Get current value from preferences
        String mCurrentValue = getPersistedString(mDefaultValue);
		String[] time = mCurrentValue.split("-");
		String[] startTime = time[0].split(":");
		String[] endTime = time[1].split(":");

		// Inflate layout
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.time_dialog_slider, null);

		mStartTimePicker = (TimePicker) view.findViewById(R.id.tp_begin);
		mStartTimePicker.setIs24HourView(true);
		mStartTimePicker.setCurrentHour(Integer.parseInt(startTime[0]));
		mStartTimePicker.setCurrentMinute(Integer.parseInt(startTime[1]));

		mEndTimePicker = (TimePicker) view.findViewById(R.id.tp_end);
		mEndTimePicker.setIs24HourView(true);
		mEndTimePicker.setCurrentHour(Integer.parseInt(endTime[0]));
		mEndTimePicker.setCurrentMinute(Integer.parseInt(endTime[1]));

		return view;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		// Return if change was cancelled
		if (!positiveResult) {
			return;
		}

		// Persist current value if needed
		if (shouldPersist()) {
			String resultTime = mStartTimePicker.getCurrentHour().toString()
					+ ":" + mStartTimePicker.getCurrentMinute().toString();
	
			resultTime += "-" + mEndTimePicker.getCurrentHour().toString() + ":"
					+ mEndTimePicker.getCurrentMinute().toString();

			persistString(resultTime);
		}
	}

	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {

	}

	public void onStartTrackingTouch(SeekBar seek) {
		// Not used
	}

	public void onStopTrackingTouch(SeekBar seek) {
		// Not used
	}
}
