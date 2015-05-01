package com.isosystem.smarthouse.settings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageGalleryAdapter extends BaseAdapter {
	
	private Context c;
	
	private ArrayList<String> pics;
	
	public ImageGalleryAdapter (Context context,ArrayList<String> p) {
		this.c = context;
		this.pics = p;
	}
	
	public int getCount() {
		return pics.size(); // Сделать нормально
	}

	public Object getItem (int position) {
		return position;
	}
	
	public long getItemId (int position) {
		return position;
	}
	
	public View getView (int position, View convertView, ViewGroup parent) {
		
		ImageView image = new ImageView(c);
		image.setLayoutParams(new Gallery.LayoutParams(96,96));
		image.setScaleType(ImageView.ScaleType.FIT_XY);
		
		Bitmap b = BitmapFactory.decodeFile(pics.get(position));
		image.setImageBitmap(b);
		return image;
	}	
	
	
}