package com.example.gesturemouseclient;

import java.util.List;

import Threads.ApplicationListenerThread;
import Threads.BackgroundWorkManager;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.interfaces.ApplicationListener;

public class MainActivity extends Activity implements SensorEventListener, ApplicationListener {

	private DeviceItem remoteDeviceInfo;
	private TextView pcConnectedName;
	private TextView appConnectedName;
	private boolean volumeDownIsPressed = false;
	private boolean volumeUpIsPressed = false;
	private String runningApp;
	private SensorManager sensorManager;
	private BackgroundWorkManager backgroundWorkManager;
	private ApplicationListenerThread applicationListenerThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		// TODO: Error checks
		sensorManager.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);

		setContentView(R.layout.activity_main);
		Intent intent = getIntent();
		remoteDeviceInfo = ((DeviceItem) intent.getExtras().get("device"));
		backgroundWorkManager = new BackgroundWorkManager(remoteDeviceInfo);

		pcConnectedName = (TextView) findViewById(R.id.connectedPcName);
		appConnectedName = (TextView) findViewById(R.id.connectedAppName);

		pcConnectedName.setText(remoteDeviceInfo.getMachineName());

		TcpInitConnection tcpConnection = new TcpInitConnection(remoteDeviceInfo, this);
		tcpConnection.execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
		if (sensorList == null || sensorList.size() < 1) {
			return; // TODO: error for no sensor
		}
		// sm.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
		if (remoteDeviceInfo.isConnected()) {
			applicationListenerThread = new ApplicationListenerThread(remoteDeviceInfo, this);
			applicationListenerThread.execute();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
		backgroundWorkManager.suspend();
		if (applicationListenerThread != null) {
			applicationListenerThread.cancel(true);
			applicationListenerThread = null;
		}
	}

	protected void onStop() {
		super.onStop();
		sensorManager.unregisterListener(this);
		backgroundWorkManager.stop();
		if (applicationListenerThread != null) {
			applicationListenerThread.cancel(true);
			applicationListenerThread = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sensorManager.unregisterListener(this);
		backgroundWorkManager.stop();
		if (applicationListenerThread != null) {
			applicationListenerThread.cancel(true);
			applicationListenerThread = null;
		}
	}

	public void onConnectionToRemoteDevice() {
		Logger.printLog("main activety : ", "setControlSession");
		backgroundWorkManager.start();
		applicationListenerThread = new ApplicationListenerThread(remoteDeviceInfo, this);
		applicationListenerThread.execute();
	}

	// public boolean onKeyLongPress(int keyCode, KeyEvent event) {
	// super.onKeyLongPress(keyCode, event);
	// if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
	// {
	//
	// return true;
	// }else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
	// {
	//
	// return true;
	// }
	// return false;
	// }

	// TODO: remember to check the state of the device, it's possible we dont want to update since we're in anotre state.
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (!volumeDownIsPressed) {
				volumeDownIsPressed = true;
				backgroundWorkManager.sendKey(2);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (!volumeUpIsPressed) {
				volumeUpIsPressed = true;
				backgroundWorkManager.sendKey(0);
				return true;
			}
		}
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		super.onKeyUp(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (volumeDownIsPressed) {
				volumeDownIsPressed = false;
				backgroundWorkManager.sendKey(3);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (volumeUpIsPressed) {
				volumeUpIsPressed = false;
				backgroundWorkManager.sendKey(1);
				return true;
			}
		}
		return false;
	}

	// TODO: remember to check the state of the device, it's possible we dont want to update since we're in anotre state.
	public void onSensorChanged(SensorEvent event) {
		Logger.printLog("onSensorChanged", Integer.toString(event.sensor.getType()));
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

			float[] rotationMatrix = new float[9];
			SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

			float[] newValues = new float[3];
			SensorManager.getOrientation(rotationMatrix, newValues);

			float x = newValues[0];
			float y = newValues[1];
			float z = newValues[2];

			Logger.printLog("onSensorChanged", "sendSample(" + x + "," + y + "," + z + ")");
			backgroundWorkManager.sendSample(newValues);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Nothing to do here.
	}

	@Override
	public void onApplicationChanged(String applicationName) {
		if (runningApp == null || runningApp != applicationName) {
			runningApp = applicationName;
			appConnectedName.setText(runningApp);
		}
	}

}
