package com.example.gesturemouseclient.activities;

import java.util.ArrayList;
import java.util.List;

import Threads.FindServersTask;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.infra.DeviceListDisplayAdapter;
import com.example.gesturemouseclient.infra.KeyMap;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.Tools;

public class FindServersActivity extends Activity implements OnClickListener {

	private ArrayAdapter<RemoteDeviceInfo> adapter;
	private String deviceName;

	private ImageView retryBtn;
	private FindServersTask findServer;
	private List<RemoteDeviceInfo> deviceList;
	private View progressBarContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_server);
		
		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

//		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/BigAppleNF.ttf");
//		headLine = (TextView) findViewById(R.id.headLine);
//		headLine.setTypeface(tf);

		// TODO: check the true device name:
		deviceName = android.os.Build.MODEL;
		Logger.printLog("onCreate", "host name: " + deviceName);
		deviceName = android.os.Build.USER;
		Logger.printLog("onCreate", "host name: " + deviceName);

		deviceList = new ArrayList<RemoteDeviceInfo>();
		ListView deviceListView = (ListView) findViewById(R.id.deviceList);
		adapter = new DeviceListDisplayAdapter(this, deviceList, this);
		deviceListView.setAdapter(adapter);

		progressBarContainer = findViewById(R.id.findServersProgressBar);
		retryBtn = (ImageView) findViewById(R.id.findServersBtn);

		retryBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startFindServers();
			}
		});

	}
	
	@Override
	protected void onStart() {
		startFindServers();
		super.onStart();
	}

	private void startFindServers() {
		startProgressBar();
		Logger.printLog("initialPcConnection", "start");
		findServer = new FindServersTask(this);
		findServer.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void stopProgressBar() {
		progressBarContainer.setVisibility(View.GONE);
		retryBtn.setVisibility(View.VISIBLE);
		if (adapter.getCount() == 0) {
			Tools.showErrorModal(this, "Error", "Did not find any device...\nPlease verify your pc server is on " +
					"and also verify you are connected to the same wifi as your pc.");
		}
	}

	public void startProgressBar() {
		progressBarContainer.setVisibility(View.VISIBLE);
		retryBtn.setVisibility(View.GONE);
	}

	public void addDevice(RemoteDeviceInfo device) {
		boolean found = false;
		for (RemoteDeviceInfo dev : deviceList) {
			if (Tools.equals(dev.getName(), device.getName())) {
				found = true;
				break;
			}
		}
		if (!found) {
			adapter.add(device);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("device", (RemoteDeviceInfo)v.getTag());
		startActivity(intent);
	}

}
