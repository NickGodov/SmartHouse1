package com.isosystem.smarthouse.data;

import java.io.Serializable;
import java.util.HashMap;

public class FormattedScreen implements Serializable {
	
	private static final long serialVersionUID = -7081775343933798513L;
	
	public String mName ="";
	public String mInputStart ="";
	public String mInputEnd ="";
	
	//Список параметров узла
	public HashMap<String, String> paramsMap;
	
	public FormattedScreen (String name, String start, String end, HashMap<String, String> pmap) {
		this.mName = name;
		this.mInputStart = start;
		this.mInputEnd = end;
		this.paramsMap = pmap;
	}
}