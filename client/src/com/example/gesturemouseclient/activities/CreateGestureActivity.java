package com.example.gesturemouseclient.activities;

import java.io.IOException;
import java.util.List;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.infra.Logger;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class CreateGestureActivity extends Activity implements SensorEventListener{

	private AndroidWiigee andgee;
	private Button learnGestureBtn;
	private SensorManager sensorManager;
	private Button recognizeGestureBtn;
	protected TextView lblStatus;
	private Button saveGestureBtn;
	private int id = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_gesture);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		initAll();
	}

	private void initAll() {
		// method order is important.
		initTextAttributes();
		initSensors();
		initAndgee();
		initLearnGesture();
		initRecognizeGesture();
		initSaveGesture();
	}

	private void initSaveGesture() {
		saveGestureBtn = (Button) findViewById(R.id.saveGestureBtn);
		saveGestureBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Logger.printLog("Create Gesture", "save gesture.");
				//andgee.getDevice().getProcessingUnit().saveLearningAsGesture();
				andgee.getDevice().saveGesture(id, "gesture_"+id);
				id++;
			}
		});
	}

	private void initTextAttributes() {
		lblStatus = (TextView) findViewById(R.id.recognizeGestureTxt);
	}

	private void initRecognizeGesture() {
		recognizeGestureBtn = (Button) findViewById(R.id.identifyGestureBtn);

		recognizeGestureBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					andgee.getDevice().getProcessingUnit().startRecognizing();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					andgee.getDevice().getProcessingUnit().endRecognizing();

				}
				return true;

			}
		});		
	}

	private void initSensors() {
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// TODO: Error checks
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
	}

	private void initLearnGesture() {
		learnGestureBtn = (Button) findViewById(R.id.recordGestureBtn);

		learnGestureBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
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
				Logger.printLog("Create Gesture Activity", "initAndgee(addGestureListener) event id: "+event.getId()+" pr: "+event.getProbability());
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
