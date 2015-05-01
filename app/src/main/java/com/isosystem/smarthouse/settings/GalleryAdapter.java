package com.isosystem.smarthouse.settings;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.isosystem.smarthouse.R;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter {

	private ArrayList<String> mImages;
	private Context mContext;

	public GalleryAdapter(ArrayList<String> images, Context context) {
		mImages = images;
		mContext = context;
	}

	public int getCount() {
		return mImages.size();
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
			v = vi.inflate(R.layout.gallery_item, null);
		}
		ImageView image = (ImageView) v.findViewById(R.id.gallery_item_image);		
		image.setImageBitmap(BitmapFactory.decodeFile(mImages.get(position)));

		return v;
	}
}