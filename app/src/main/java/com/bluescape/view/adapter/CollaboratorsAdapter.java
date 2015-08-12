package com.bluescape.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluescape.AppConstants;
import com.bluescape.R;
import com.bluescape.model.CollaboratorModel;

import java.util.List;

public class CollaboratorsAdapter extends BaseAdapter {

	private class ViewHolder {
		TextView memberName, userNameTv;
		ImageView memberIv, followIv, colorIv;
	}

	private static final String TAG = CollaboratorsAdapter.class.getSimpleName();
	private List<CollaboratorModel> membersList;

	final private Context mContext;

	int colourIndex = 0;

	public CollaboratorsAdapter(Context mContext, List<CollaboratorModel> membersList) {
		this.mContext = mContext;
		this.membersList = membersList;

	}

	@Override
	public int getCount() {
		return membersList.size();
	}

	@Override
	public Object getItem(int position) {
		return membersList.get(position);
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

	public void updateDealsResult(List<CollaboratorModel> membersList) {
		this.membersList = membersList;
		notifyDataSetChanged();
	}

	private View getConvertView() {

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ViewHolder holder = new ViewHolder();
		View view;
		view = inflater.inflate(R.layout.collaborator_list_item, null);

		holder.memberName = (TextView) view.findViewById(R.id.memberName);
		holder.memberIv = (ImageView) view.findViewById(R.id.memberIv);
		holder.followIv = (ImageView) view.findViewById(R.id.followIv);
		holder.colorIv = (ImageView) view.findViewById(R.id.colorIv);
		holder.userNameTv = (TextView) view.findViewById(R.id.userNameTv);

		view.setTag(holder);
		return view;
	}

	private void updateView(int position, final ViewHolder holder) {

		try {
			CollaboratorModel member = membersList.get(position);
			holder.memberName.setText(member.getName());

			if (member.getClientType().equals("wall")) {
				holder.userNameTv.setVisibility(View.INVISIBLE);
				holder.memberIv.setVisibility(View.VISIBLE);
			} else {
				holder.userNameTv.setText(member.initials());
				holder.userNameTv.setVisibility(View.VISIBLE);
				holder.memberIv.setVisibility(View.INVISIBLE);
			}

			holder.colorIv.setBackgroundColor(Color.parseColor(member.getColor()));

		} catch (Exception ex) {
			AppConstants.LOG(AppConstants.CRITICAL, TAG, "Known Exception usually when debugging and pausing Ex:" + ex);

		}
	}
}