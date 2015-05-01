package com.isosystem.smarthouse;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.isosystem.smarthouse.data.AlarmMessage;
import com.isosystem.smarthouse.notifications.Notifications;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainMessagesAdapter extends BaseAdapter {
	
    // Список сообщений
	ArrayList<AlarmMessage> mAlarmList;
	Context mContext;

	public MainMessagesAdapter(Context context, ArrayList<AlarmMessage> list) {
		mAlarmList = list;
		this.mContext = context;
	}

	public int getCount() {
		return mAlarmList.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(this.mContext);
			v = vi.inflate(R.layout.messages_activity_list_item, null);
		}

        // Получение i-го сообщения из массива
		AlarmMessage alarm = mAlarmList.get(position);
        // Тип сообщения
		Notifications.MessageType mType = alarm.msgType;
        // Текст сообщения
		String msg = alarm.msgText;
        // Дата регистрации сообщения
		Long time = alarm.msgTime;

        // Вычисление даты регистрации сообщений в нужном формате
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm:ss");
		Date resultdate = new Date(time);

        // В зависимости от типа сообщений изменение цвета плашки
		GridLayout grid = (GridLayout) v.findViewById(R.id.messages_activity_layout);	
		grid.setBackground(mContext.getResources().getDrawable(mType.getColor()));	

		Typeface font = Typeface.createFromAsset(mContext.getAssets(),
				"myfont.ttf");

        // Установка даты регистрации сообщения
		TextView mHeaderText = (TextView) v
				.findViewById(R.id.messages_activity_list_item_header_text);
		mHeaderText.setTypeface(font);
		mHeaderText.setTextSize(18.0f);
		mHeaderText.setText(sdf.format(resultdate));
		mHeaderText.setTextColor(Color.parseColor("#ffffff"));

        // Картинка для плашки
		ImageView mImage = (ImageView) v
				.findViewById(R.id.messages_activity_list_item_image);
		mImage.setImageBitmap(BitmapFactory.decodeResource(v.getResources(),
				mType.getIcon()));

        // Установка текста сообщения
		TextView mMessageText = (TextView) v
				.findViewById(R.id.messages_activity_list_item_message_text);
		mMessageText.setText(msg);
		mMessageText.setTextSize(25.0f);
		mMessageText.setTypeface(font);
		mMessageText.setTextColor(Color.parseColor("#ffffff"));

		return v;
	}
}