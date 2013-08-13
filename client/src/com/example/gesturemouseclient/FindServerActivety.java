package com.example.gesturemouseclient;





import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
	private TextView searchForDevice;
	
	private DeviceItem device;
	private Button retryBtn;
	private FindServer findServer;
	
	

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
		
		searchForDevice = (TextView) findViewById(R.id.searchingForDevicesText);
		retryBtn = (Button) findViewById(R.id.retryBtn);
		
		
		retryBtn.setVisibility(View.INVISIBLE);
		retryBtn.setClickable(false);
		
		retryBtn.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
				startFindServer();
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
		startFindServer();
			
	}
	

	


	private void startFindServer() {
		startProgressBar();
		Logger.printLog("initialPcConnection","start");
		findServer = new FindServer(this);
		retryBtn.setVisibility(View.INVISIBLE);
		searchForDevice.setText("Searching for devices...");
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
		if(adapter.getCount() == 0)
		{
			retryBtn.setVisibility(View.VISIBLE);
			retryBtn.setClickable(true);
			searchForDevice.setText("Did not find any device...\nPlease verify your pc server is on.");
		}else{
			searchForDevice.setVisibility(View.INVISIBLE);
			retryBtn.setVisibility(View.INVISIBLE);
			retryBtn.setClickable(false);
			searchForDevice.setText("Searching for devices...");
		}
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
