package com.example.gesturemouseclient.infra;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.activities.FindServerActivity;

public class DeviceDeleteListDisplayAdapter extends ArrayAdapter<RemoteDeviceInfo> {

	public DeviceDeleteListDisplayAdapter(FindServerActivity activity, List<RemoteDeviceInfo> deviceList) {
		super(activity, android.R.layout.activity_list_item, deviceList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.row_delete, null);

		return view;

	}

}
