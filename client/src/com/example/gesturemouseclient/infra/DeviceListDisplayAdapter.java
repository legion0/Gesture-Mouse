package com.example.gesturemouseclient.infra;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.activities.FindServersActivity;

public class DeviceListDisplayAdapter extends ArrayAdapter<RemoteDeviceInfo> {

	public DeviceListDisplayAdapter(FindServersActivity activity, List<RemoteDeviceInfo> deviceList) {
		super(activity, android.R.layout.activity_list_item, deviceList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RemoteDeviceInfo device = getItem(position);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.row_name, null);

		TextView deviceName = (TextView) view.findViewById(R.id.deviceName);
		deviceName.setText(device.getMachineName());

		return view;

	}

}
