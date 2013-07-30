package com.example.gesturemouseclient;




import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.gesturemouseclient.infra.DeviceDeleteListDisplayAdapter;
import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.DeviceListDisplayAdapter;
import com.example.gesturemouseclient.infra.Logger;

@SuppressLint("NewApi")
public class FindServerActivety extends Activity {
	
	
	private ProgressBar progressBar;
	private ArrayAdapter<DeviceItem> adapter;
	private ArrayAdapter<DeviceItem> adapterDelete;
	private String deviceName;
	
	private DeviceItem device;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_server);
		
		//TODO: check the true device name:
		deviceName = android.os.Build.MODEL;
		Logger.printLog("onCreate","host name: "+ deviceName);
		deviceName = android.os.Build.USER;
		Logger.printLog("onCreate","host name: "+ deviceName);
		
		List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		ListView deviceListView = (ListView)findViewById(R.id.deviceList);	
		adapter = new DeviceListDisplayAdapter(this,deviceList);
		deviceListView.setAdapter(adapter);
		
		deviceListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				device = adapter.getItem(position);
				//TODO: go to the next page
				initConnectionToDevice();	
			}
		});
		
		
		// TODO: to remove this part and add it to one list.
		List<DeviceItem> deviceDeleteList = new ArrayList<DeviceItem>();
		ListView deviceDeleteListView = (ListView)findViewById(R.id.deviceDeleteList);	
		adapterDelete = new DeviceDeleteListDisplayAdapter(this,deviceDeleteList);
		deviceDeleteListView.setAdapter(adapterDelete);

		deviceDeleteListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				DeviceItem tempItem = adapterDelete.getItem(position);
				adapterDelete.remove(tempItem);
				adapter.remove(tempItem);
			}
		});
		
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		Logger.printLog("initialPcConnection","start");
				
		final FindServer findServer = new FindServer(this);
		findServer.execute();
			
	}
	


	/**
	 * initialize TCP asynchronous connection.
	 * 
	 * @param device
	 */
	protected void initConnectionToDevice() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("device",device);
		startActivity(intent);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void stopProgressBar()
	{

		progressBar.setVisibility(View.INVISIBLE);
	}
	
	public void startProgressBar()
	{
		progressBar.setVisibility(View.VISIBLE);
	}

	public void addDevice(DeviceItem device) {
		adapter.add(device);
		adapterDelete.add(device);
	}




	
	

	
}
