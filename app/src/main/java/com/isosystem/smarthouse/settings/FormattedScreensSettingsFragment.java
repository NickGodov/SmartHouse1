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

public class FormattedScreensSettingsFragment extends Fragment {
	Context mContext;
	View rootView;
	ListView lv;
	MyApplication mApplication;
	LayoutInflater mInflater;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mInflater = inflater;
		rootView = inflater.inflate(R.layout.fragment_settings_formscreens,
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
		ListView mListView = (ListView) rootView
				.findViewById(R.id.formscreens_list);
		FormattedScreensSettingsAdapter adapter = new FormattedScreensSettingsAdapter(
				mContext, this);
		mListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

	}

	@Override
	public void onResume() {
		super.onResume();
		generateListView();
	}
}