package com.isosystem.smarthouse.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

import com.isosystem.smarthouse.Globals;
import com.isosystem.smarthouse.R;

import java.io.File;
import java.util.ArrayList;

public final class ChooseBackgroundPreference extends DialogPreference {
	private Context mContext;

    //private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/com.isosystem.smarthouse";
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	
	String mCurrentValue;
	String mDefaultValue;
	
	//private static final String DEFAULT_CURRENT_VALUE = "no-image";
	
	Gallery mGallery;
	ArrayList<String> mImages = null;
	ImageView mGalleryPicker;

	public ChooseBackgroundPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		
		mDefaultValue = attrs.getAttributeValue(ANDROID_NS, "defaultValue");
		
	}

	@Override
	protected View onCreateDialogView() {

		// Inflate layout
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_choose_background, null);

		// Изображение выбранной картинки из галереи
		mGalleryPicker = (ImageView) view
				.findViewById(R.id.tile_image);
		mGalleryPicker.setTag("");
		mGalleryPicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(mContext, mGalleryPicker.getTag().toString(), Toast.LENGTH_SHORT).show();
			}
		});
		
		// Галерея изображения для экрана
		mGallery = (Gallery) view.findViewById(R.id.tile_image_gallery);
		mImages = getImages();
		//mGallery.setAdapter(new ImageGalleryAdapter(mContext, mImages));
		mGallery.setAdapter(new ChooseBackgroundAdapter(mContext,getImages()));
		mGallery.setOnItemClickListener(galleryImageSelectListener);
		
		mCurrentValue = getPersistedString(mDefaultValue);
		
		int pos = mImages.indexOf(mCurrentValue);
		
		if (pos!=-1) {
			mGallery.setSelection(pos);

			Bitmap b = BitmapFactory.decodeFile(mImages.get(pos));
			mGalleryPicker.setImageBitmap(b);
			mGalleryPicker.setTag(mImages.get(pos));
		}
		
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
			persistString(mGalleryPicker.getTag().toString());
		}

		// Notify activity about changes (to update preference summary line)
		notifyChanged();
	}

	private ArrayList<String> getImages() {
		ArrayList<String> images = new ArrayList<String>();
		File file = new File(Environment.getExternalStorageDirectory()
				+ File.separator + Globals.EXTERNAL_ROOT_DIRECTORY
				+ File.separator + Globals.EXTERNAL_IMAGES_DIRECTORY);

		if (file.isDirectory()) {
			File[] listFile = file.listFiles();
            for (File f : listFile) {
                String filenameArray[] = f.getName().split("\\.");
                String extension = filenameArray[filenameArray.length - 1];
                if (extension.equals("jpg") || extension.equals("png")
                        || extension.equals("gif")) {
                    images.add(f.getAbsolutePath());
                }
            }
		}
		return images;
	}

	private OnItemClickListener galleryImageSelectListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			Bitmap b = BitmapFactory.decodeFile(mImages.get(position));
			mGalleryPicker.setImageBitmap(b);
			mGalleryPicker.setTag(mImages.get(position));
		}
	};
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
}
