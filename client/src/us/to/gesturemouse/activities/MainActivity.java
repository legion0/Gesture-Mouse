package us.to.gesturemouse.activities;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;

import us.to.gesturemouse.dal.ApplicationDAL;
import us.to.gesturemouse.dal.GestureDAL;
import us.to.gesturemouse.infra.KeyMap;
import us.to.gesturemouse.infra.Params;
import us.to.gesturemouse.infra.RemoteDeviceInfo;
import us.to.gesturemouse.infra.Tools;
import us.to.gesturemouse.infra.interfaces.ApplicationListener;
import us.to.gesturemouse.layouts.KeyboardDetectorRelativeLayout;
import us.to.gesturemouse.layouts.KeyboardDetectorRelativeLayout.IKeyboardChanged;
import us.to.gesturemouse.threads.ApplicationListenerTask;
import us.to.gesturemouse.threads.BackgroundWorkManager;
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

public class MainActivity extends Activity implements SensorEventListener, ApplicationListener, IKeyboardChanged {

	enum State {
		MOUSE, GESTURE
	};

	private static int MOUSE_SENSOR_DELAY = 20000;

	private RemoteDeviceInfo remoteDeviceInfo;
	private TextView pcConnectedName;
	private TextView appConnectedName;
	private boolean volumeDownIsPressed = false;
	private boolean volumeUpIsPressed = false;
	private ApplicationDAL runningApp;
	private SensorManager sensorManager;
	private BackgroundWorkManager backgroundWorkManager;

	private ImageView recognizeGestureBtn;

	private ImageView goToMouseBtn;

	private ImageView learnGestureBtn;

	private AndroidWiigee andgee;

	private Set<ApplicationDAL> applications;

	private Map<Integer, Integer> classifierIdMap;

	private ImageView goToGestureBtn;
	private ImageView openKeyboardBtn;
	private InputMethodManager inputMethodManager;
	protected boolean isKeyboardOpen;

	private Runnable actionOnConnect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		Logger.printLog("onCreate", "the app is created !");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		KeyboardDetectorRelativeLayout layout = new KeyboardDetectorRelativeLayout(this);
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
		
		actionOnConnect = new Runnable() {
			
			@Override
			public void run() {
				toMouseMode();
			}
		};
		backgroundWorkManager = new BackgroundWorkManager(remoteDeviceInfo, this, this, actionOnConnect, this);

//		Logger.printLog("MainActivity", "initGestureMode");
		recognizeGestureBtn = (ImageView) findViewById(R.id.gestureBtn);
		recognizeGestureBtn.setVisibility(View.INVISIBLE);
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
					if (backgroundWorkManager != null) {
						backgroundWorkManager.sendGesture(gestureId);
					}
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
				inputMethodManager.toggleSoftInputFromWindow(openKeyboardBtn.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
			}

		});
	}

	@Override
	protected void onStart() {
		applications = ApplicationDAL.loadWithGestures(this);
		backgroundWorkManager.start();
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	protected void onStop() {
		sensorManager.unregisterListener(this);
		backgroundWorkManager.suspend();
		super.onStop();
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("keyCode", "" + keyCode);
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return true;
		}
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
		if (backgroundWorkManager != null) {
			backgroundWorkManager.suspendFastSampleSenderThread();
		}
		Tools.unregisterMouseSensor(sensorManager, this);
	}

	private void resumeMouse() {
		if (backgroundWorkManager != null) {
			backgroundWorkManager.resumeFastSampleSenderThread();
		}
		Tools.registerMouseSensor(sensorManager, this, MOUSE_SENSOR_DELAY);
		
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

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
		resumeMouse();
		recognizeGestureBtn.setVisibility(View.GONE);
		goToMouseBtn.setVisibility(View.GONE);
		goToGestureBtn.setVisibility(View.VISIBLE);
		// learnGestureBtn.setClickable(false);
		String displayText = runningApp.getWindowTitle();
		displayText = displayText.substring(0, Math.min(displayText.length(), 20));
		appConnectedName.setText(displayText);
		if (backgroundWorkManager != null) {
			backgroundWorkManager.resumeFastSampleSenderThread();
		}
		andgee.getDevice().setAccelerationEnabled(false);
		Tools.unregisterGestureSensor(sensorManager, this);
	}

	private void toGestureMode() {
		suspendMouse();
		Tools.registerGestureSensor(sensorManager, this, SensorManager.SENSOR_DELAY_GAME);
		recognizeGestureBtn.setVisibility(View.VISIBLE);
		goToMouseBtn.setVisibility(View.VISIBLE);
		goToGestureBtn.setVisibility(View.GONE);
		String displayText = runningApp.getName();
		appConnectedName.setText(displayText);
		if (backgroundWorkManager != null) {
			backgroundWorkManager.suspendFastSampleSenderThread();
		}
		andgee.getDevice().setAccelerationEnabled(true);
		classifierIdMap.clear();
		andgee.getDevice().getProcessingUnit().getClassifier().clear();

		for (GestureDAL gesture : runningApp.getGestures()) {
			int classifierId = andgee.getDevice().getProcessingUnit().getClassifier().addGestureModel(gesture.getModel());
			classifierIdMap.put(classifierId, gesture.getId());
		}
	}
}
