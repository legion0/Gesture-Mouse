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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.dal.GestureDAL;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;

public class CreateGestureActivity extends Activity implements SensorEventListener {

	private AndroidWiigee andgee;
	private Button learnGestureBtn;
	private SensorManager sensorManager;
	protected TextView lblStatus;
	private Button saveGestureBtn;
	private GestureDAL gesture;
	private int learnSessions = 0;
	private ProgressBar saveProgressBar;

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

		saveProgressBar = (ProgressBar) findViewById(R.id.saveProgressBar);
		stopProgressBar();

		saveGestureBtn = (Button) findViewById(R.id.saveGestureBtn);
		final Context context = getApplicationContext();
		saveGestureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startProgressBar();
				saveGestureBtn.setClickable(false);

				Logger.printLog("Create Gesture", "save gesture.");
				int classifierGestureId = andgee.getDevice().getProcessingUnit().saveLearningAsGesture();
				GestureModel gestureModel = andgee.getDevice().getProcessingUnit().getClassifier().getGestureModel(classifierGestureId);
				gesture.setModel(gestureModel);
//				gesture.save(context);
				
				SaveAsyncTask save = new SaveAsyncTask();
				save.execute(gesture);



				


				gesture.addToApplication(context, appId);
				andgee.getDevice().getProcessingUnit().getClassifier().clear();
				gestureNameEditText.getText().clear();
				gesture = new GestureDAL(null, null, null);
				//				stopProgressBar();
			}
		});
	}
	
	class SaveAsyncTask extends AsyncTask<GestureDAL, Void, Void>{

		@Override
		protected Void doInBackground(GestureDAL... params) {
			GestureDAL gesture = params[0];
			Context context = getApplicationContext();
			gesture.save(context);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			stopProgressBar();
		}
	}


	private void initAll() {
		// method order is important.
		initSensors();
		initAndgee();
		initLearnGesture();
	}

	public void stopProgressBar() {
		saveProgressBar.setVisibility(View.INVISIBLE);
	}

	public void startProgressBar() {
		saveProgressBar.setVisibility(View.VISIBLE);
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
					learnSessions++;
					lblStatus = (TextView) findViewById(R.id.recognizeGestureTxt);
					lblStatus.setText(String.format("session %s/10", learnSessions));
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
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onPause() {
		Logger.printLog("Create Gesture: ", "on pause");
		sensorManager.unregisterListener(this);
		super.onPause();
	}

}
