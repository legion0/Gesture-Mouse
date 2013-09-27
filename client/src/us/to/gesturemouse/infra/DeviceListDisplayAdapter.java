package us.to.gesturemouse.infra;

import java.util.List;

import us.to.gesturemouse.activities.FindServersActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gesturemouseclient.R;

public class DeviceListDisplayAdapter extends ArrayAdapter<RemoteDeviceInfo> {

	private OnClickListener listener;

	public DeviceListDisplayAdapter(FindServersActivity activity, List<RemoteDeviceInfo> deviceList, OnClickListener listener) {
		super(activity, android.R.layout.activity_list_item, deviceList);
		this.listener = listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RemoteDeviceInfo item = getItem(position);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.find_servers_row, null);
		TextView deviceName = (TextView)view.findViewById(R.id.deviceName);
		deviceName.setTag(item);
		deviceName.setText(item.getName());
		deviceName.setOnClickListener(listener);
		ImageView deleteBtn = (ImageView)view.findViewById(R.id.deleteRemoteBtn);
		deleteBtn.setTag(item);
		deleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				remove((RemoteDeviceInfo)v.getTag());
			}
		});
		return view;

	}

}
