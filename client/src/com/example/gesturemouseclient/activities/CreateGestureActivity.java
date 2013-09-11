package com.example.gesturemouseclient.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;
import org.wiigee.logic.GestureModel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.dal.GestureDAL;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;

public class CreateGestureActivity extends Activity implements SensorEventListener {

	private AndroidWiigee andgee;
	private Button learnGestureBtn;
	private SensorManager sensorManager;
	protected TextView lblStatus;
	private Button saveGestureBtn;
	private GestureDAL gesture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_gesture);
		gesture = new GestureDAL(null, null, null);
		Intent intent = getIntent();
		final int appId = intent.getIntExtra("app_id", -1);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		initAll();

		final EditText gestureNameEditText = (EditText) findViewById(R.id.gestureName);
		gestureNameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					gesture.setName(gestureNameEditText.getText().toString());
				}
			}
		});

		Button setActionBtn = (Button) findViewById(R.id.setActionBtn);
		final Intent setActionIntent = new Intent(this, CreateActionActivity.class);
		setActionBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(setActionIntent, Params.PICK_ACTION_REQUEST);
			}
		});

		saveGestureBtn = (Button) findViewById(R.id.saveGestureBtn);
		final Context context = getApplicationContext();
		saveGestureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Logger.printLog("Create Gesture", "save gesture.");
				int id = andgee.getDevice().getProcessingUnit().saveLearningAsGesture();
				GestureModel gestureModel = andgee.getDevice().getProcessingUnit().getClassifier().getGestureModel(id);
				gesture.setModel(gestureModel);
				gesture.save(context);
				gesture.addToApplication(context, appId);
				andgee.getDevice().getProcessingUnit().getClassifier().clear();
				gestureNameEditText.getText().clear();
				gesture = new GestureDAL(null, null, null);
			}
		});
	}

	private void initAll() {
		// method order is important.
		initTextAttributes();
		initSensors();
		initAndgee();
		initLearnGesture();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Params.PICK_ACTION_REQUEST) {
			if (resultCode == RESULT_OK) {
				Logger.printLog("Create Gesture", "save to db");

				String actionStr = (String) data.getExtras().get("action");
				List<Integer> action = new ArrayList<Integer>();
				String[] actionSplited = actionStr.split(",");
				for (int i = 0; i < actionSplited.length; i++) {
					action.add(Integer.parseInt(actionSplited[i]));
				}
				gesture.setAction(action);
			}
		}
	}

	private void initTextAttributes() {
		lblStatus = (TextView) findViewById(R.id.recognizeGestureTxt);
	}

	private void initSensors() {
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		// TODO: Error checks
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
	}

	private void initLearnGesture() {
		learnGestureBtn = (Button) findViewById(R.id.recordGestureBtn);

		learnGestureBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Logger.printLog("Create Gesture Activity", "start learning");
					andgee.getDevice().getProcessingUnit().startLearning();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					Logger.printLog("Create Gesture Activity", "stop learning");
					andgee.getDevice().getProcessingUnit().endLearning();
				}
				return true;

			}
		});

	}

	private void initAndgee() {
		andgee = new AndroidWiigee();

		andgee.addGestureListener(new GestureListener() {
			@Override
			public void gestureReceived(GestureEvent event) {
				Logger.printLog("Create Gesture Activity", "initAndgee(addGestureListener) event id: " + event.getId() + " pr: " + event.getProbability());
				lblStatus.setText("Gesture id: " + event.getId() + " with Probability: " + event.getProbability());
			}
		});

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		andgee.getDevice().onSensorChanged(event);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		andgee.getDevice().onAccuracyChanged(sensor, accuracy);
	}

	@Override
	protected void onResume() {
		super.onResume();
		List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
		try {
			Logger.printLog("Create Gesture: ", "on resume");
			andgee.getDevice().setAccelerationEnabled(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		Logger.printLog("Create Gesture: ", "on pause");
		sensorManager.unregisterListener(this);
		super.onPause();
	}

}
