package com.example.gesturemouseclient.activities;

import java.util.List;

import Threads.ApplicationListenerThread;
import Threads.BackgroundWorkManager;
import Threads.FastSampleSenderThread;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.TcpInitConnection;
import com.example.gesturemouseclient.R.id;
import com.example.gesturemouseclient.R.layout;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.interfaces.ApplicationListener;

public class MainActivity extends Activity implements SensorEventListener, ApplicationListener {

	enum State {MOUSE, GESTURE};

	private State state = State.MOUSE;

	private boolean isRunning = true;

	private RemoteDeviceInfo remoteDeviceInfo;
	private TextView pcConnectedName;
	private TextView appConnectedName;
	private boolean volumeDownIsPressed = false;
	private boolean volumeUpIsPressed = false;
	private String runningApp;
	private SensorManager sensorManager;
	private BackgroundWorkManager backgroundWorkManager;
	private ApplicationListenerThread applicationListenerThread;

	private Button gestureBtn;

	private Button goToMouseBtn;

	private Button learnGestureBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.printLog("onCreate", "the app is created !");
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		setContentView(R.layout.activity_main);
		Intent intent = getIntent();
		remoteDeviceInfo = ((RemoteDeviceInfo) intent.getExtras().get("device"));

		pcConnectedName = (TextView) findViewById(R.id.connectedPcName);
		appConnectedName = (TextView) findViewById(R.id.connectedAppName);

		pcConnectedName.setText(remoteDeviceInfo.getMachineName());


		initGestureMode();

		TcpInitConnection tcpConnection = new TcpInitConnection(remoteDeviceInfo, this);
		tcpConnection.execute();
	}

	private void initGestureMode() {
		Logger.printLog("MainActivity", "initGestureMode");
		gestureBtn = (Button) findViewById(R.id.gestureBtn);
		goToMouseBtn = (Button) findViewById(R.id.goToMouseBtn);
		learnGestureBtn = (Button) findViewById(R.id.learnGestureBtn);
		
		
		gestureMode(false);
		gestureBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Logger.printLog("Gesture onClick Listener", "");							
			}
		});

	}

	@Override
	protected void onStart() {
		Logger.printLog("onStart", "the app is start !");
		super.onStart();

		if (!isRunning) {
			this.onConnectionToRemoteDevice();
		}
		isRunning = true;
	}

	@Override
	protected void onResume() {
		Logger.printLog("onResume", "the app is resumed !");
		super.onResume();
		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
		if (sensorList == null || sensorList.size() < 1) {
			return; // TODO: error for no sensor
		}
		// sm.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
		if (remoteDeviceInfo.isConnected()) {
			applicationListenerThread = new ApplicationListenerThread(remoteDeviceInfo, this);
			applicationListenerThread.execute();
			if(backgroundWorkManager != null){
				backgroundWorkManager.resume();
			}
		}
		isRunning = true;
	}

	@Override
	protected void onPause() {
		Logger.printLog("onPause", "the app is paused !");
		super.onPause();
		sensorManager.unregisterListener(this);
		backgroundWorkManager.suspend();
		if (applicationListenerThread != null) {
			applicationListenerThread.cancel(true);
			applicationListenerThread = null;
		}
		isRunning = false;
	}

	protected void onStop() {
		Logger.printLog("onStop", "the app is stoped !");
		super.onStop();
		sensorManager.unregisterListener(this);
		backgroundWorkManager.stop();
		if (applicationListenerThread != null) {
			applicationListenerThread.cancel(true);
			applicationListenerThread = null;
		}
		isRunning = false;
	}

	@Override
	protected void onDestroy() {
		Logger.printLog("onDestroy", "the app is destroyed !");
		super.onDestroy();
		sensorManager.unregisterListener(this);
		backgroundWorkManager.stop();
		if (applicationListenerThread != null) {
			applicationListenerThread.cancel(true);
			applicationListenerThread = null;
		}
		isRunning = false;
	}

	public void onConnectionToRemoteDevice() {
		Logger.printLog("onConnectionToRemoteDevice", "");

		// start threads:
		backgroundWorkManager = new BackgroundWorkManager(remoteDeviceInfo);
		backgroundWorkManager.start();
		applicationListenerThread = new ApplicationListenerThread(remoteDeviceInfo, this);
		applicationListenerThread.execute();

		//start sensors:
		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		// TODO: Error checks
		sensorManager.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);

		sensorList = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
		// TODO: Error checks
		sensorManager.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
	}

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
		//		Logger.printLog("onSensorChanged", Integer.toString(event.sensor.getType()));
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

			float[] rotationMatrix = new float[9];
			SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

			float[] newValues = new float[3];
			SensorManager.getOrientation(rotationMatrix, newValues);

			float x = newValues[0];
			float y = newValues[1];
			float z = newValues[2];

			//			Logger.printLog("onSensorChanged", "sendSample(" + x + "," + y + "," + z + ")");
			backgroundWorkManager.sendSample(newValues);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Nothing to do here.
	}

	@Override
	public void onApplicationChanged(String applicationName) {
		Logger.printLog("onApplicationChanged", "");
		if (applicationName == null) {
			appConnectedName.setText("Mouse");
			state = State.MOUSE;
			gestureMode(false);
			backgroundWorkManager.resumeFastSampleSenderThread();
		}else{
			appConnectedName.setText(applicationName);
			state = State.GESTURE;
			gestureMode(true);
			backgroundWorkManager.suspendFastSampleSenderThread();		
		}
	}

	private void gestureMode(boolean activate) {
		Logger.printLog("MainActivity", "gesture mode = "+activate);
		gestureBtn.setClickable(activate);
		goToMouseBtn.setClickable(activate);
		learnGestureBtn.setClickable(activate);
		if (activate) {
			gestureBtn.setVisibility(View.VISIBLE);
			goToMouseBtn.setVisibility(View.VISIBLE);
			learnGestureBtn.setVisibility(View.VISIBLE);
		}else{
			gestureBtn.setVisibility(View.INVISIBLE);
			goToMouseBtn.setVisibility(View.INVISIBLE);
			learnGestureBtn.setVisibility(View.INVISIBLE);
		}
		
	}
}
