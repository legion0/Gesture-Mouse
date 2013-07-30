package com.example.gesturemouseclient.infra;

import java.util.List;

import com.example.gesturemouseclient.FindServerActivety;
import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.R.id;
import com.example.gesturemouseclient.R.layout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DeviceListDisplayAdapter extends ArrayAdapter<DeviceItem>{
	
	public DeviceListDisplayAdapter(
			FindServerActivety activity, List<DeviceItem> deviceList) {
		super(activity, android.R.layout.activity_list_item, deviceList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DeviceItem device = getItem(position);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.row_name, null);
		
		TextView deviceName = (TextView)view.findViewById(R.id.deviceName);
		deviceName.setText(device.getMachineName());
	
		return view;
		


	}


}
