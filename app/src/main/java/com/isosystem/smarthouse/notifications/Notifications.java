package com.isosystem.smarthouse.notifications;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.isosystem.smarthouse.R;

public class Notifications {

	public static void showError(Context c, String msg) {
		Typeface font = Typeface.createFromAsset(c.getAssets(), "myfont.ttf");

		LayoutInflater vi;
		vi = LayoutInflater.from(c);
		View v = vi.inflate(R.layout.toast_error, null);
		
		TextView text = (TextView) v.findViewById(R.id.toast_error_text);
		text.setText(msg);
		text.setTextSize(30);
		text.setTextColor(Color.parseColor("white"));
		text.setTypeface(font);
		
		Toast toast = new Toast(c);		
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.FILL_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.setView(v);
		toast.show();
	}

	/**
	 * Выводит на экран пришедшее алармовое сообщение
	 * @param c Контекст
	 * @param msg Сообщение
	 */
	public static void showUsbMessage(Context c, String msg) {
		Typeface font = Typeface.createFromAsset(c.getAssets(), "myfont.ttf");

		LayoutInflater vi;
		vi = LayoutInflater.from(c);
		View v = vi.inflate(R.layout.toast_alarm_message, null);
		
		TextView text = (TextView) v.findViewById(R.id.toast_error_text);
		text.setText(msg);
		text.setTextSize(30);
		text.setTextColor(Color.parseColor("white"));
		text.setTypeface(font);
		
		Toast toast = new Toast(c);		
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.FILL_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.setView(v);
		toast.show();
	}
	
	public static void showPowerSupplyMessage(Context c, String msg) {
		Typeface font = Typeface.createFromAsset(c.getAssets(), "myfont.ttf");

		LayoutInflater vi;
		vi = LayoutInflater.from(c);
		View v = vi.inflate(R.layout.toast_power_supply_message, null);
		
		TextView text = (TextView) v.findViewById(R.id.toast_error_text);
		text.setText(msg);
		text.setTextSize(30);
		text.setTextColor(Color.parseColor("white"));
		text.setTypeface(font);
		
		Toast toast = new Toast(c);		
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.FILL_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.setView(v);
		toast.show();
	}
	
	public static void showControllerAlarmMessage(Context c, String msg) {
		Typeface font = Typeface.createFromAsset(c.getAssets(), "myfont.ttf");

		LayoutInflater vi;
		vi = LayoutInflater.from(c);
		View v = vi.inflate(R.layout.toast_controller_message, null);
		
		TextView text = (TextView) v.findViewById(R.id.toast_error_text);
		text.setText(msg);
		text.setTextSize(30);
		text.setTextColor(Color.parseColor("white"));
		text.setTypeface(font);
		
		Toast toast = new Toast(c);		
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.FILL_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.setView(v);
		toast.show();
	}
	
	public static void showOkMessage(Context c, String msg) {}
	
// Убрано 06.11 временно
/*
	public static void showOkMessage(Context c, String msg) {
		Typeface font = Typeface.createFromAsset(c.getAssets(), "myfont.ttf");

		LayoutInflater vi;
		vi = LayoutInflater.from(c);
		View v = vi.inflate(R.layout.toast_ok, null);
		
		TextView text = (TextView) v.findViewById(R.id.toast_ok_text);
		text.setText(msg);
		text.setTextSize(30);
		text.setTextColor(Color.parseColor("white"));
		text.setTypeface(font);
		
		Toast toast = new Toast(c);		
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.FILL_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.setView(v);
		toast.show();
	}
*/
	
	public static void showPositiveMessage(Context c, String msg) {
		Typeface font = Typeface.createFromAsset(c.getAssets(), "myfont.ttf");

		LayoutInflater vi;
		vi = LayoutInflater.from(c);
		View v = vi.inflate(R.layout.toast_ok, null);
		
		TextView text = (TextView) v.findViewById(R.id.toast_ok_text);
		text.setText(msg);
		text.setTextSize(30);
		text.setTextColor(Color.parseColor("white"));
		text.setTypeface(font);
		
		Toast toast = new Toast(c);		
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.FILL_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.setView(v);
		toast.show();
	}
	
	
	public static void showTooltip(Context c, String msg) {
		Typeface font = Typeface.createFromAsset(c.getAssets(), "myfont.ttf");

		LayoutInflater vi;
		vi = LayoutInflater.from(c);
		View v = vi.inflate(R.layout.toast_tooltip, null);
		
		TextView text = (TextView) v.findViewById(R.id.toast_tooltip_text);
		text.setText(msg);
		text.setTextSize(30);
		text.setTextColor(Color.parseColor("#000023"));
		text.setTypeface(font);
		
		Toast toast = new Toast(c);		
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.FILL_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
		toast.setView(v);
		toast.show();
	}
	

	public enum MessageType {
		USBConnectionMessage("USB-соединение", R.drawable.usb, R.drawable.messages_activity_list_item_0,0),
		ControllerMessage("Важное сообщение",R.drawable.message, R.drawable.messages_activity_list_item_1,1),
		PowerSupplyMessage("Подключение к электросети",R.drawable.plug,R.drawable.messages_activity_list_item_0,2);

		private int icon;
		private String title;
		private int color;
		private int ordinal;

		private MessageType(String t, int i, int c,int o) {
			title = t;
			icon = i;
			color = c;
			ordinal = o;
		}

		public int getColor() {
			return color;
		}

		public int getIcon() {
			return icon;
		}

		public String getTitle() {
			return title;
		}
		
		public int getOrdinal() {
			return ordinal;
		}
	}
}