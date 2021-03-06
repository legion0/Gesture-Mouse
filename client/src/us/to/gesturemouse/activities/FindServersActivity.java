package us.to.gesturemouse.activities;

import java.util.ArrayList;
import java.util.List;

import us.to.gesturemouse.infra.DeviceListDisplayAdapter;
import us.to.gesturemouse.infra.RemoteDeviceInfo;
import us.to.gesturemouse.infra.Tools;
import us.to.gesturemouse.threads.FindServersTask;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.gesturemouseclient.R;

public class FindServersActivity extends Activity implements OnClickListener {

	private ArrayAdapter<RemoteDeviceInfo> adapter;

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

		// Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/BigAppleNF.ttf");
		// headLine = (TextView) findViewById(R.id.headLine);
		// headLine.setTypeface(tf);

		deviceList = new ArrayList<RemoteDeviceInfo>();
		ListView deviceListView = (ListView) findViewById(R.id.deviceList);
		adapter = new DeviceListDisplayAdapter(this, deviceList, this);
		deviceListView.setAdapter(adapter);

		progressBarContainer = findViewById(R.id.findServersProgressBar);
		retryBtn = (ImageView) findViewById(R.id.findServersBtn);
		stopProgressBar();

		retryBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startFindServers();
			}
		});

		// boolean printKeyMap = true;
		// if (printKeyMap) {
		// for (int key_code=0; key_code < KeyEvent.getMaxKeyCode(); key_code++) {
		// String androidName = KeyEvent.keyCodeToString(key_code);
		// String pureName, winName;
		// Integer winKeyCode;
		// if (androidName != null && androidName.startsWith("KEYCODE_")) {
		// pureName = androidName.replace("KEYCODE_SOFT_", "");
		// pureName = pureName.replace("KEYCODE_DPAD_", "");
		// pureName = pureName.replace("KEYCODE_", "");
		// winName = "VK_OEM_" + pureName;
		// winKeyCode = KeyMap.KEY_MAP.get(winName);
		// if (winKeyCode != null) {
		// Log.w("", androidName+" : "+winName);
		// } else {
		// winName = "VK_" + pureName;
		// winKeyCode = KeyMap.KEY_MAP.get(winName);
		// if (winKeyCode != null) {
		// Log.w("", androidName+" : "+winName);
		// }
		// }
		// }
		// }
		// }

	}

	@Override
	protected void onStart() {
		startFindServers();
		super.onStart();
	}

	private void startFindServers() {
		// Check for wifi first
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		final Runnable openWifiSettings = new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		};
		if (!mWifi.isAvailable()) {
			Tools.showErrorModal(this, "Error", "Please enable wifi in settings.", "Open WIFI Settings", openWifiSettings);
			return;
		} else if (!mWifi.isConnected()) {
			Tools.showErrorModal(this, "Error", "Please connect to the same wifi network as your PC / Laptop.", "Open WIFI Settings", openWifiSettings);
			return;
		}
		startProgressBar();
//		Logger.printLog("initialPcConnection", "start");
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
		RemoteDeviceInfo remoteDeviceInfo = (RemoteDeviceInfo) v.getTag();
		if (remoteDeviceInfo != null) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("device", remoteDeviceInfo);
			startActivity(intent);
		}
	}

	public void finshedServerSearch() {
		stopProgressBar();
		if (adapter.getCount() == 0) {
			Tools.showErrorModal(this, "Error",
					"Did not find any device...\nPlease verify your PC / Laptop server is on and both your phone and PC / Laptop are connected to the same network.");
		}
	}

}
