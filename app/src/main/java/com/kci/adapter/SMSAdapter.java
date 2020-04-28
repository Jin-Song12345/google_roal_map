package com.kci.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kci.smsreceiver.R;
import com.kci.structure.POSITION;

public class SMSAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<POSITION> PosList;

	private int single_Row_Layout;

	public SMSAdapter(Context context, int single_Row_Layout,
			ArrayList<POSITION> PosList) {

		this.context = context;
		this.single_Row_Layout = single_Row_Layout;
		this.PosList = PosList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return PosList == null ? 0 : PosList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return PosList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		POSITION pos = PosList.get(position);

		final ViewHolder viewHolder;
		if (convertView == null) {

			viewHolder = new ViewHolder();

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			convertView = inflater.inflate(R.layout.sms_data_view, parent, false);


			viewHolder.sms_sender = (TextView) convertView
					.findViewById(R.id.sms_sender);

			viewHolder.sms_content = (TextView) convertView
					.findViewById(R.id.sms_content);

			viewHolder.sms_datetime = (TextView) convertView
					.findViewById(R.id.sms_datetime);
			viewHolder.sms_Address = (TextView) convertView
								.findViewById(R.id.sms_Address);

			convertView.setTag(viewHolder);

		} else {

			viewHolder = (ViewHolder) convertView.getTag();

		}

		viewHolder.sms_sender.setText("Phone : " + pos.mSender);

		viewHolder.sms_content.setText("Address : " + pos.mContent);
		viewHolder.sms_datetime.setText("DateTime : " + pos.mDate+":"+pos.mTime);
		viewHolder.sms_Address.setText("Location : " + pos.mPlaceAddresse);

		return convertView;
	}

	protected class ViewHolder {

		private TextView sms_content;
		private TextView sms_sender;
		private TextView sms_datetime;
		private TextView sms_Address;

	}
}
