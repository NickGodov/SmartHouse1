package com.isosystem.smarthouse.data;

import com.isosystem.smarthouse.notifications.Notifications.MessageType;

import java.io.Serializable;

public class AlarmMessage implements Serializable {
    public String msgText ="";
	public MessageType msgType = null;
	public long msgTime=0;
	
	public AlarmMessage (String message_text, MessageType mType) {
		this.msgText = message_text;
		this.msgType = mType;
		this.msgTime = System.currentTimeMillis();
	}
}