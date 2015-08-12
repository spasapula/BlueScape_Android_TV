package com.bluescape.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.model.widget.LocationMarkerModel;
import com.bluescape.util.DateConverter;

import java.util.List;

public class MarkersAdapter extends BaseAdapter {
	private class ViewHolder {
		TextView markerName, markerCreatedDate;
		ImageView markerImage;
	}

	private List<LocationMarkerModel> markersList;

	final private Context mContext;

	public MarkersAdapter(Context mContext, List<LocationMarkerModel> markersList) {
		this.mContext = mContext;
		this.markersList = markersList;

	}

	@Override
	public int getCount() {
		return markersList.size();
	}

	@Override
	public Object getItem(int position) {
		return markersList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) convertView = getConvertView();

		ViewHolder holder = (ViewHolder) convertView.getTag();

		updateView(position, holder);

		convertView.setId(position);

		return convertView;
	}

	public void updateDealsResult(List<LocationMarkerModel> markersList) {
		this.markersList = markersList;
		notifyDataSetChanged();
	}

	private View getConvertView() {

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ViewHolder holder = new ViewHolder();
		View view;
		view = inflater.inflate(R.layout.location_list_item, null);

		holder.markerName = (TextView) view.findViewById(R.id.markerName);
		holder.markerCreatedDate = (TextView) view.findViewById(R.id.markerCreatedDate);
		holder.markerImage = (ImageView) view.findViewById(R.id.markerImage);

		view.setTag(holder);
		return view;
	}

	private void updateView(int position, final ViewHolder holder) {

		if (markersList != null) {
			try {
				LocationMarkerModel marker = markersList.get(position);

				holder.markerName.setText(marker.getMarkerName());

				holder.markerCreatedDate.setText(DateConverter.timeStampToDate((long) marker.getCreationTime()));

				int color = marker.getColor();
				if (color == AppConstants.MarkerColor.RED.ordinal()) {
					holder.markerImage.setBackgroundResource(R.drawable.red_marker);
				} else if (color == AppConstants.MarkerColor.YELLOW.ordinal()) {
					holder.markerImage.setBackgroundResource(R.drawable.yellow_marker);
				} else if (color == AppConstants.MarkerColor.GREEN.ordinal()) {
					holder.markerImage.setBackgroundResource(R.drawable.green_marker);
				} else if (color == AppConstants.MarkerColor.BLUE.ordinal()) {
					holder.markerImage.setBackgroundResource(R.drawable.blue_marker);

				}
			} catch (Exception ex) {
				ex.printStackTrace();
				AppConstants.LOG(AppConstants.CRITICAL, "MarkersAdapter", "Exception in updateView()");
			}
		}
	}
}