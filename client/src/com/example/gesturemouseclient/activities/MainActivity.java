package com.example.gesturemouseclient.activities;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;

import Threads.ApplicationListenerTask;
import Threads.BackgroundWorkManager;
import Threads.TcpInitConnectionTask;
import android.app.Activity;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.dal.GestureDAL;
import com.example.gesturemouseclient.infra.KeyMap;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.Tools;
import com.example.gesturemouseclient.infra.interfaces.ApplicationListener;
import com.example.gesturemouseclient.layouts.KeyboardDetectorRelativeLayout;
import com.example.gesturemouseclient.layouts.KeyboardDetectorRelativeLayout.IKeyboardChanged;

public class MainActivity extends Activity implements SensorEventListener, ApplicationListener, IKeyboardChanged {

	enum State {
		MOUSE, GESTURE
	};

	private RemoteDeviceInfo remoteDeviceInfo;
	private TextView pcConnectedName;
	private TextView appConnectedName;
	private boolean volumeDownIsPressed = false;
	private boolean volumeUpIsPressed = false;
	private ApplicationDAL runningApp;
	private SensorManager sensorManager;
	private BackgroundWorkManager backgroundWorkManager;
	private ApplicationListenerTask applicationListenerThread;

	private ImageView recognizeGestureBtn;

	private ImageView goToMouseBtn;

	private ImageView learnGestureBtn;

	private AndroidWiigee andgee;

	private Set<ApplicationDAL> applications;

	private Map<Integer, Integer> classifierIdMap;

	private TcpInitConnectionTask tcpConnection;
	private ImageView goToGestureBtn;
	private ImageView openKeyboardBtn;
	private InputMethodManager inputMethodManager;
	protected boolean isKeyboardOpen;
	private boolean mouseSuspended;
	private Sensor rotationalVectorSensor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.printLog("onCreate", "the app is created !");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		KeyboardDetectorRelativeLayout layout = new KeyboardDetectorRelativeLayout(getApplicationContext());
		layout.inflate(R.layout.activity_main);
		layout.addKeyboardStateChangedListener(this);
		setContentView(layout);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		Intent intent = getIntent();
		remoteDeviceInfo = ((RemoteDeviceInfo) intent.getExtras().get("device"));

		pcConnectedName = (TextView) findViewById(R.id.connectedPcName);
		appConnectedName = (TextView) findViewById(R.id.connectedAppName);

		pcConnectedName.setText(remoteDeviceInfo.getMachineName());

		runningApp = new ApplicationDAL(null, null, "unknown");

		classifierIdMap = new LinkedHashMap<Integer, Integer>();

		final Activity this_ = this;

		Logger.printLog("MainActivity", "initGestureMode");
		recognizeGestureBtn = (ImageView) findViewById(R.id.gestureBtn);
		goToMouseBtn = (ImageView) findViewById(R.id.goToMouseBtn);
		goToGestureBtn = (ImageView) findViewById(R.id.goToGestureBtn);
		learnGestureBtn = (ImageView) findViewById(R.id.learnGestureBtn);
		openKeyboardBtn = (ImageView) findViewById(R.id.openKeyboardBtn);

		andgee = new AndroidWiigee();

		andgee.addGestureListener(new GestureListener() {
			@Override
			public void gestureReceived(GestureEvent event) {
				if (event.isValid()) {
					int gestureId = classifierIdMap.get(event.getId());
					Log.d("Main activity", "Recognized gesture " + gestureId + " with probability " + event.getProbability());
					Log.d("Main activity", "Sending Gesture id: " + gestureId);
					backgroundWorkManager.sendGesture(gestureId);
				} else {
					// TODO: did not recognize any gesture
				}
			}
		});

		recognizeGestureBtn.setOnTouchListener(new OnTouchListener() {

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

		goToMouseBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toMouseMode();
			}
		});

		goToGestureBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (runningApp.getId() != null) {
					toGestureMode();
				} else {
					Tools.showErrorModal(this_, "Error", "The current Application has no gestures.");
				}
			}
		});

		learnGestureBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (runningApp.getId() != null) {
					Intent intent = new Intent(this_, CreateActionActivity.class);
					intent.putExtra("app_id", runningApp.getId());
					startActivity(intent);
				} else {
					Intent intent = new Intent(this_, CreateNewApplicationActivity.class);
					intent.putExtra("window_title", runningApp.getWindowTitle());
					intent.putExtra("process_name", runningApp.getProcessName());
					startActivity(intent);
				}
			}

		});

		openKeyboardBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				inputMethodManager.toggleSoftInputFromWindow(openKeyboardBtn.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
			}

		});

		toMouseMode();
	}

	@Override
	protected void onStart() {
		applications = ApplicationDAL.loadWithGestures(getApplicationContext());
		super.onStart();
		tcpConnection = new TcpInitConnectionTask(remoteDeviceInfo, this);
		tcpConnection.execute(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void onConnectionToRemoteDevice() {
		remoteDeviceInfo.setConnected(true);
		Logger.printLog("onConnectionToRemoteDevice", "");
		// start threads:
		backgroundWorkManager = new BackgroundWorkManager(remoteDeviceInfo);
		backgroundWorkManager.start();
		applicationListenerThread = new ApplicationListenerTask(remoteDeviceInfo, this);
		applicationListenerThread.execute();

		// start sensors:
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		// TODO: Error checks
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);

		registerRotationalVector();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	protected void onStop() {
		Logger.printLog("onStop", "the app is stoped !");
		super.onStop();
		if (remoteDeviceInfo.isConnected()) {
			sensorManager.unregisterListener(this);
			backgroundWorkManager.suspend();
			tcpConnection = new TcpInitConnectionTask(remoteDeviceInfo, this);
			tcpConnection.execute(false);
			applicationListenerThread.cancel(true);
			applicationListenerThread = null;
		}
		remoteDeviceInfo.setConnected(false);
	}

	@Override
	protected void onDestroy() {
		Logger.printLog("onDestroy", "the app is destroyed !");
		super.onDestroy();
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

	// TODO: remember to check the state of the device, it's possible we dont
	// want to update since we're in anotre state.
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("keyCode", "" + keyCode);
		super.onKeyDown(keyCode, event);

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (!volumeDownIsPressed) {
				volumeDownIsPressed = true;
				backgroundWorkManager.sendKey(KeyMap.holdKey(KeyMap.VK_RBUTTON));
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (!volumeUpIsPressed) {
				volumeUpIsPressed = true;
				backgroundWorkManager.sendKey(KeyMap.holdKey(KeyMap.VK_LBUTTON));
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return true;
		}
		return false;
	}

	// @Override
	// public void onBackPressed() {
	// super.onBackPressed();
	// // finish();
	// }

	// @Override
	// public void onConfigurationChanged(Configuration newConfig) {
	// super.onConfigurationChanged(newConfig);
	// // Checks whether a hardware keyboard is hidden
	// if (isKeyboardOpen && newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
	// isKeyboardOpen = false;
	// resumeMouse();
	// }
	// }

	@Override
	public void onKeyboardShown() {
		if (!isKeyboardOpen) {
			isKeyboardOpen = true;
			suspendMouse();
		}
	}

	@Override
	public void onKeyboardHidden() {
		if (isKeyboardOpen) {
			isKeyboardOpen = false;
			resumeMouse();
		}
	}

	private void suspendMouse() {
		mouseSuspended = true;
		backgroundWorkManager.suspendFastSampleSenderThread();
		unregisterRotationalVector();
	}

	private void resumeMouse() {
		mouseSuspended = false;
		backgroundWorkManager.resumeFastSampleSenderThread();
		registerRotationalVector();
	}

	private boolean registerRotationalVector() {
		rotationalVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		return sensorManager.registerListener(this, rotationalVectorSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	private void unregisterRotationalVector() {
		sensorManager.unregisterListener(this, rotationalVectorSensor);
	}

	private boolean isMouseSuspended() {
		return mouseSuspended;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		super.onKeyUp(keyCode, event);
		// Log.d("keyCode", KeyEvent.keyCodeToString(keyCode));
		Integer winKeyCode = null;
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (volumeDownIsPressed) {
				volumeDownIsPressed = false;
				backgroundWorkManager.sendKey(KeyMap.releaseKey(KeyMap.VK_RBUTTON));
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (volumeUpIsPressed) {
				volumeUpIsPressed = false;
				backgroundWorkManager.sendKey(KeyMap.releaseKey(KeyMap.VK_LBUTTON));
				return true;
			}
		} else if ((winKeyCode = KeyMap.ANDROID_TO_WINDOWS_KEY_MAP.get(keyCode)) != null) { // try to map to windows key codes
			if (event.isShiftPressed()) {
				int[] keys = new int[] { KeyMap.holdKey(KeyMap.VK_SHIFT), winKeyCode, KeyMap.releaseKey(KeyMap.VK_SHIFT) };
				backgroundWorkManager.sendKeys(keys);
			} else if (event.isAltPressed()) {
				int[] keys = new int[] { KeyMap.holdKey(KeyMap.VK_MENU), winKeyCode, KeyMap.releaseKey(KeyMap.VK_MENU) };
				backgroundWorkManager.sendKeys(keys);
				// } else if (event.isCtrlPressed()) {
				// int[] keys = new int[] {KeyMap.holdKey(KeyMap.VK_CONTROL), winKeyCode, KeyMap.releaseKey(KeyMap.VK_CONTROL)};
				// backgroundWorkManager.sendKeys(keys);
			} else {
				backgroundWorkManager.sendKey(winKeyCode);
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
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && !isMouseSuspended()) {

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
			Log.v("MaintActivity", "runningApp=" + runningApp.getId());
			toGestureMode();
		} else {
			runningApp = fakeApp;
			toMouseMode();
		}
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
		recognizeGestureBtn.setVisibility(View.GONE);
		goToMouseBtn.setVisibility(View.GONE);
		goToGestureBtn.setVisibility(View.VISIBLE);
		// learnGestureBtn.setClickable(false);
		String displayText = runningApp.getWindowTitle();
		displayText = displayText.substring(0, Math.min(displayText.length(), 20));
		appConnectedName.setText(displayText);
		try {
			if (backgroundWorkManager != null) {
				backgroundWorkManager.resumeFastSampleSenderThread();
			}
			andgee.getDevice().setAccelerationEnabled(false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void toGestureMode() {
		recognizeGestureBtn.setVisibility(View.VISIBLE);
		goToMouseBtn.setVisibility(View.VISIBLE);
		goToGestureBtn.setVisibility(View.GONE);
		String displayText = runningApp.getName();
		appConnectedName.setText(displayText);
		try {
			if (backgroundWorkManager != null) {
				backgroundWorkManager.suspendFastSampleSenderThread();
			}

			andgee.getDevice().setAccelerationEnabled(true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		classifierIdMap.clear();
		andgee.getDevice().getProcessingUnit().getClassifier().clear();

		for (GestureDAL gesture : runningApp.getGestures()) {
			int classifierId = andgee.getDevice().getProcessingUnit().getClassifier().addGestureModel(gesture.getModel());
			classifierIdMap.put(classifierId, gesture.getId());
		}
	}
}
