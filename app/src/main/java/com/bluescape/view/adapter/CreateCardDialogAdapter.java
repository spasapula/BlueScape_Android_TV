package com.bluescape.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bluescape.AppSingleton;
import com.bluescape.R;
import com.bluescape.collaboration.util.NetworkTask;

import java.util.ArrayList;
import java.util.List;

public class CreateCardDialogAdapter extends BaseAdapter {
	private final Context mContext;
	private final List<String> cardTemplatesList;

	// Constructor
	public CreateCardDialogAdapter(Context mContext) {
		this.mContext = mContext;
		this.cardTemplatesList = new ArrayList<>(AppSingleton.getInstance().getApplication().getColorToUrlMap().values());
	}

	public int getCount() {
		return cardTemplatesList.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View gridView;
		Bitmap bgBitmap;
		ImageView imageView;
		if (convertView == null) {
			gridView = inflater.inflate(R.layout.create_card_item, null);
			imageView = (ImageView) gridView.findViewById(R.id.createCardGVIN);
			bgBitmap = NetworkTask.getBitmapFromBaseName(cardTemplatesList.get(position), null);
			imageView.setImageBitmap(bgBitmap);

		} else {
			gridView = convertView;
		}

		return gridView;

	}

}