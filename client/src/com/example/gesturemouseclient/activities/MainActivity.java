package com.example.gesturemouseclient.activities;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;

import Threads.ApplicationListenerThread;
import Threads.BackgroundWorkManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.TcpInitConnection;
import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.dal.GestureDAL;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.interfaces.ApplicationListener;

public class MainActivity extends Activity implements SensorEventListener, ApplicationListener {

	enum State {
		MOUSE, GESTURE
	};

	private State state = State.MOUSE;

	private boolean isRunning = true;

	private RemoteDeviceInfo remoteDeviceInfo;
	private TextView pcConnectedName;
	private TextView appConnectedName;
	private boolean volumeDownIsPressed = false;
	private boolean volumeUpIsPressed = false;
	private ApplicationDAL runningApp;
	private SensorManager sensorManager;
	private BackgroundWorkManager backgroundWorkManager;
	private ApplicationListenerThread applicationListenerThread;

	private Button gestureBtn;

	private Button goToMouseBtn;

	private Button learnGestureBtn;

	private AndroidWiigee andgee;

	private Set<ApplicationDAL> applications;

	private Map<Integer, Integer> classifierIdMap;

	private TcpInitConnection tcpConnection;

	private boolean wasStartedOnce = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.printLog("onCreate", "the app is created !");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		Intent intent = getIntent();
		remoteDeviceInfo = ((RemoteDeviceInfo) intent.getExtras().get("device"));

		pcConnectedName = (TextView) findViewById(R.id.connectedPcName);
		appConnectedName = (TextView) findViewById(R.id.connectedAppName);

		pcConnectedName.setText(remoteDeviceInfo.getMachineName());

		runningApp = new ApplicationDAL(null, null, "unknown");
		initGestureMode();

		tcpConnection = new TcpInitConnection(remoteDeviceInfo, this);
		tcpConnection.execute(false);
		classifierIdMap = new LinkedHashMap<Integer, Integer>();
	}

	private void initGestureMode() {

		Logger.printLog("MainActivity", "initGestureMode");
		gestureBtn = (Button) findViewById(R.id.gestureBtn);
		goToMouseBtn = (Button) findViewById(R.id.goToMouseBtn);
		learnGestureBtn = (Button) findViewById(R.id.learnGestureBtn);

		initAndgee();

		toMouseMode();

		// gestureBtn.setOnTouchListener(new OnTouchListener() {
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// if(event.getAction() == MotionEvent.ACTION_DOWN) {
		// increaseSize();
		// } else if (event.getAction() == MotionEvent.ACTION_UP) {
		// resetSize();
		// }
		// }
		// });

		gestureBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					andgee.getDevice().getProcessingUnit().startRecognizing();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					andgee.getDevice().getProcessingUnit().endRecognizing();
				}
				return true;

			}
		});

		// gestureBtn.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Logger.printLog("Gesture onClick Listener", "");
		// //TODO: Listen to gesture here.
		// }
		// });

		goToMouseBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toMouseMode();
				Logger.printLog("Main Activity", "Mouse button: " + goToMouseBtn.getText().toString());
			}
		});
		final Activity this_ = this;

		learnGestureBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (runningApp.getId() != null) {
					Intent intent = new Intent(this_, CreateGestureActivity.class);
					intent.putExtra("app_id", runningApp.getId());
					startActivity(intent);
				} else {
					Intent intent = new Intent(this_, CreateNewApplicationActivity.class);
					intent.putExtra("window_title", runningApp.getWindowTitle());
					intent.putExtra("process_name", runningApp.getProcessName());
					startActivityForResult(intent, Params.PICK_APPLICATION_REQUEST);
				}
			}

		});

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Activity this_ = this;
		if (requestCode == Params.PICK_APPLICATION_REQUEST) {
			if (resultCode == RESULT_OK) {
				int app_id = data.getIntExtra("app_id", -1);
				Intent intent = new Intent(this_, CreateGestureActivity.class);
				intent.putExtra("app_id", app_id);
				startActivity(intent);
			}
		}
	}

	private void initAndgee() {
		andgee = new AndroidWiigee();

		andgee.addGestureListener(new GestureListener() {
			@Override
			public void gestureReceived(GestureEvent event) {
				int gestureId = classifierIdMap.get(event.getId());
				Log.d("Main activity", "Recognized gesture " + gestureId + " with probability " + event.getProbability());
				Log.d("Main activity", "Sending Gesture id: " + gestureId);
				backgroundWorkManager.sendGesture(gestureId);
			}
		});
	}

	@Override
	protected void onStart() {

		applications = ApplicationDAL.loadWithGestures(getApplicationContext());
		Logger.printLog("onStart", "the app is start !");
		super.onStart();

		if (!isRunning) {
			tcpConnection = new TcpInitConnection(remoteDeviceInfo, this);
			tcpConnection.execute(true);
			tcpConnection = new TcpInitConnection(remoteDeviceInfo, this);
			tcpConnection.execute(false);
			this.onConnectionToRemoteDevice();
		}
		isRunning = true;
	}

	@Override
	protected void onResume() {
		Logger.printLog("onResume", "the app is resumed !");
		super.onResume();
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		if (sensor == null) {
			return; // TODO: error for no sensor
		}
		// sm.registerListener(this, sensorList.get(0),
		// SensorManager.SENSOR_DELAY_GAME);
		if (remoteDeviceInfo.isConnected()) {
			applicationListenerThread = new ApplicationListenerThread(remoteDeviceInfo, this);
			applicationListenerThread.execute();
			if (backgroundWorkManager != null) {
				backgroundWorkManager.resume();
			}
		}

		if (state == State.MOUSE) {
			toMouseMode();
		} else {
			toGestureMode();
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

		if(! wasStartedOnce ){
			wasStartedOnce = true;
			// start threads:
			backgroundWorkManager = new BackgroundWorkManager(remoteDeviceInfo);
			backgroundWorkManager.start();
			applicationListenerThread = new ApplicationListenerThread(remoteDeviceInfo, this);
			applicationListenerThread.execute();

			// start sensors:
			List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
			// TODO: Error checks
			sensorManager.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);

			sensorList = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
			// TODO: Error checks
			sensorManager.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
		}
	}

	// TODO: remember to check the state of the device, it's possible we dont
	// want to update since we're in anotre state.
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

	// TODO: remember to check the state of the device, it's possible we dont
	// want to update since we're in anotre state.
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			andgee.getDevice().onSensorChanged(event);
		}

		// Logger.printLog("onSensorChanged",
		// Integer.toString(event.sensor.getType()));
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

			float[] rotationMatrix = new float[9];
			SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

			float[] newValues = new float[3];
			SensorManager.getOrientation(rotationMatrix, newValues);

			// Logger.printLog("onSensorChanged", Arrays.toString(newValues));
			backgroundWorkManager.sendSample(newValues);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		andgee.getDevice().onAccuracyChanged(sensor, accuracy);
	}

	@Override
	public void onApplicationChanged(ApplicationDAL fakeApp) {
		Log.v("Main Activity", "application changed: " + fakeApp.getId());
		if (fakeApp.getId() != null) {
			Log.v("MaintActivity", "searching for app " + fakeApp.getId());
			runningApp = findApp(fakeApp.getId());
			Log.v("MaintActivity", "runningApp="+runningApp.getId());
			toGestureMode();
		} else {
			runningApp = fakeApp;
			toMouseMode();
		}
	}

	private ApplicationDAL findApp(String applicationName) {
		ApplicationDAL application = null;
		for (ApplicationDAL app : applications) {
			if (app.getName().equals(applicationName)) {
				application = app;
				break;
			}
		}
		return application;
	}

	private ApplicationDAL findApp(int id) {
		ApplicationDAL application = null;
		for (ApplicationDAL app : applications) {
			if (app.getId() == id) {
				application = app;
				break;
			}
		}
		return application;
	}

	private void toMouseMode() {
		gestureBtn.setClickable(false);
		goToMouseBtn.setClickable(false);
		//		learnGestureBtn.setClickable(false);
		String displayText = runningApp.getWindowTitle();
		displayText = displayText.substring(0, Math.min(displayText.length(), 20));
		appConnectedName.setText(displayText);
		try {
			if (backgroundWorkManager != null) {
				backgroundWorkManager.resumeFastSampleSenderThread();
			}
			gestureBtn.setVisibility(View.INVISIBLE);
			goToMouseBtn.setVisibility(View.INVISIBLE);
			//			learnGestureBtn.setVisibility(View.INVISIBLE);
			state = State.MOUSE;
			andgee.getDevice().setAccelerationEnabled(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void toGestureMode() {
		gestureBtn.setClickable(true);
		goToMouseBtn.setClickable(true);
		//		learnGestureBtn.setClickable(true);
		String displayText = runningApp.getName();
		appConnectedName.setText(displayText);
		try {
			state = State.GESTURE;
			gestureBtn.setVisibility(View.VISIBLE);
			goToMouseBtn.setVisibility(View.VISIBLE);
			//			learnGestureBtn.setVisibility(View.VISIBLE);
			if (backgroundWorkManager != null) {
				backgroundWorkManager.suspendFastSampleSenderThread();
			}

			andgee.getDevice().setAccelerationEnabled(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		classifierIdMap.clear();
		andgee.getDevice().getProcessingUnit().getClassifier().clear();

		for (GestureDAL gesture : runningApp.getGestures()) {
			int classifierId = andgee.getDevice().getProcessingUnit().getClassifier().addGestureModel(gesture.getModel());
			classifierIdMap.put(classifierId, gesture.getId());
		}
	}
}
