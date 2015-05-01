package com.isosystem.smarthouse.settings;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.isosystem.smarthouse.MyApplication;
import com.isosystem.smarthouse.R;

public class MainMenuSettingsFragment extends Fragment {
	View rootView;
	ListView mListView;
	MyApplication mApplication;
	LayoutInflater mInflater;
	Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_settings_mainmenu,
				container, false);

		mContext = rootView.getContext();
		mApplication = (MyApplication) getActivity().getApplicationContext();

		generateListView();
		return rootView;
	}

	/**
	 * Генерирует список из древовидного меню. Необходимо создать новый список,
	 * зарегистрировать адаптеры для контекстного меню и адаптер для данных,
	 * после чего обновить список
	 */
	public void generateListView() {
		mListView = (ListView) rootView.findViewById(R.id.settingslist1);
		MainMenuSettingsAdapter adapter = new MainMenuSettingsAdapter(mContext,
				this);
		mListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	/**
	 * Если пользователь вернулся сюда кнопкой "назад", необходимо обновить
	 * список
	 */
	@Override
	public void onResume() {
		super.onResume();
		generateListView();
	}
}