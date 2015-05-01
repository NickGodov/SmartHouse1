package com.isosystem.smarthouse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.isosystem.smarthouse.R;
import com.isosystem.smarthouse.data.MenuTree.MenuScreenType;
import com.isosystem.smarthouse.data.MenuTree.NodeType;
import com.isosystem.smarthouse.data.MenuTreeNode;

import java.util.ArrayList;
import java.util.HashMap;

public class MenuItemShowDialog extends DialogFragment {

	MenuTreeNode node;
	Context mContext;
	ListView list;

	public MenuItemShowDialog (Context context, MenuTreeNode menuItem) {
		this.node = menuItem;
		this.mContext = context;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		
		list = new ListView(mContext);
		list.setPadding(10, 20, 10, 20);
		ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
		
		/** Добавляем данные */
		
		// Имя
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("name", "Имя узла");
		map.put("value", node.nodeTitle);
		mylist.add(map);
		
		// Имя родительского узла
		map = new HashMap<String, String>();
		map.put("name", "Имя родительского узла");
		map.put("value", node.parentNode.nodeTitle);
		mylist.add(map);
		
		// Тип узла
		map = new HashMap<String, String>();
		map.put("name", "Тип узла");
		map.put("value", node.nodeType.name());
		mylist.add(map);
		
		// Количество дочерних узлов
		map = new HashMap<String, String>();
		map.put("name", "Количество дочерних узлов");
		map.put("value", String.valueOf(node.childNodes.size()));
		mylist.add(map);
		
		if (node.nodeType == NodeType.NODE_LEAF) {
			// Тип конечного узла
			map = new HashMap<String, String>();
			map.put("name", "Тип конечного узла");
			map.put("value", node.screenType.name());
			mylist.add(map);

			// Параметры
			
			map = new HashMap<String, String>();
			map.put("name", "Текст заголовка");
			map.put("value", node.paramsMap.get("HeaderText"));
			mylist.add(map);

			map = new HashMap<String, String>();
			map.put("name", "Текст поясняющей надписи");
			map.put("value", node.paramsMap.get("DescriptionText"));
			mylist.add(map);
							
			if (node.screenType == MenuScreenType.SetIntValue) {
				
				map = new HashMap<String, String>();
				map.put("name", "Путь к изображению");
				map.put("value", node.paramsMap.get("SelectedImage"));
				mylist.add(map);

				map = new HashMap<String, String>();
				map.put("name", "Сообщение при вводе невалидного значения");
				map.put("value", node.paramsMap.get("InvalidValueText"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Формула для обработки входящего значения");
				map.put("value", node.paramsMap.get("IncomingValueFormula"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Количество знаков после запятой");
				map.put("value", node.paramsMap.get("FractionDigits"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Формула для обработки исходящего значения");
				map.put("value", node.paramsMap.get("OutgoingValueFormula"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Булевая формула для валидации значения");
				map.put("value", node.paramsMap.get("OutgoingValueValidation"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Запрос текущего значения от контроллера");
				map.put("value", node.paramsMap.get("GiveMeValueMessage"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Префикс для отправки введенного значения");
				map.put("value", node.paramsMap.get("OutgoingValueMessage"));
				mylist.add(map);
				
			} else if (node.screenType == MenuScreenType.SetBooleanValue) {
				
				map = new HashMap<String, String>();
				map.put("name", "Путь к изображению");
				map.put("value", node.paramsMap.get("SelectedImage"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Надпись для входящего значения 1");
				map.put("value", node.paramsMap.get("IncomingTrueText"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Надпись для входящего значения 0");
				map.put("value", node.paramsMap.get("IncomingFalseText"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Надпись для исходящего значения 1");
				map.put("value", node.paramsMap.get("OutgoingTrueText"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Надпись для исходящего значения 0");
				map.put("value", node.paramsMap.get("OutgoingFalseText"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Запрос текущего значения от контроллера");
				map.put("value", node.paramsMap.get("GiveMeValueMessage"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Префикс для отправки введенного значения");
				map.put("value", node.paramsMap.get("OutgoingValueMessage"));
				mylist.add(map);
				
			} else if (node.screenType == MenuScreenType.SetPasswordValue) {
				
				map = new HashMap<String, String>();
				map.put("name", "Путь к изображению");
				map.put("value", node.paramsMap.get("SelectedImage"));
				mylist.add(map);
				
				map = new HashMap<String, String>();
				map.put("name", "Префикс для отправки введенного значения");
				map.put("value", node.paramsMap.get("OutgoingValueMessage"));
				mylist.add(map);
			} else if (node.screenType == MenuScreenType.SendMessage) {
				
				map = new HashMap<String, String>();
				map.put("name", "Отправляемое сообщение");
				map.put("value", node.paramsMap.get("OutgoingValueMessage"));
				mylist.add(map);
			}
		}

		SimpleAdapter mSchedule = new SimpleAdapter(mContext, mylist, R.layout.menu_item_show_layout,
		            new String[] {"name", "value"}, new int[] {R.id.menu_item_name, R.id.menu_item_value});

		list.setAdapter(mSchedule);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(list)
				.setPositiveButton("OK", positiveButtonListener);
		return builder.create();
	} // onCreate

	private DialogInterface.OnClickListener positiveButtonListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			MenuItemShowDialog.this.dismiss();
		}
	}; // end listener
} // end dialog class