package com.example.gesturemouseclient.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.logic.GestureModel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.dal.GestureDAL;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.Tools;

public class CreateGestureActivity extends Activity implements SensorEventListener {

	private AndroidWiigee andgee;
	private Button learnGestureBtn;
	private SensorManager sensorManager;
	protected TextView lblStatus;
	private Button saveGestureBtn;
	private GestureDAL gesture;
	private int learnSessions = 0;
	private ProgressBar saveProgressBar;
	private int appId;
	private boolean hasAction = false;
	private boolean hasGesture = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_gesture);
		gesture = new GestureDAL(null, null, null);
		Intent intent = getIntent();
		appId = intent.getIntExtra("app_id", -1);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		lblStatus = (TextView) findViewById(R.id.recognizeGestureTxt);

		initAndgee();
		initLearnGesture();

		Button setActionBtn = (Button) findViewById(R.id.setActionBtn);
		final Intent setActionIntent = new Intent(this, CreateActionActivity.class);
		setActionBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(setActionIntent, Params.PICK_ACTION_REQUEST);
			}
		});

		saveProgressBar = (ProgressBar) findViewById(R.id.saveProgressBar);
		saveProgressBar.setVisibility(View.INVISIBLE);

		saveGestureBtn = (Button) findViewById(R.id.saveGestureBtn);
		saveGestureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveGestureBtn.setClickable(false);
				SaveAsyncTask save = new SaveAsyncTask();
				save.execute();
			}
		});
	}

	@Override
	protected void onStart() {
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		// TODO: Error checks
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
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

	@Override
	protected void onStop() {
		sensorManager.unregisterListener(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	void setSessionCount(int newCount) {
		learnSessions = newCount;
		lblStatus.setText(String.format("session %s/10", learnSessions));
	}

	void increaseSessionCount() {
		learnSessions++;
		setSessionCount(learnSessions);
	}

	class SaveAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			saveProgressBar.setVisibility(View.VISIBLE);
			gesture.setName("");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (hasGesture && hasAction) {
				Context context = getApplicationContext();
				Logger.printLog("Create Gesture", "save gesture.");
				int classifierGestureId = andgee.getDevice().getProcessingUnit().saveLearningAsGesture();
				GestureModel gestureModel = andgee.getDevice().getProcessingUnit().getClassifier().getGestureModel(classifierGestureId);
				gesture.setModel(gestureModel);
				gesture.save(context);
				gesture.addToApplication(context, appId);
				andgee.getDevice().getProcessingUnit().getClassifier().clear();
				gesture = new GestureDAL(null, null, null);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			if (!hasGesture) {
				openAlertDialog("Record a new gesture, recording 10 times will result with better identification.");
			} else if (!hasAction) {
				openAlertDialog("Select an action for your gesture.");
			} else{
				
			}
			saveProgressBar.setVisibility(View.INVISIBLE);
			saveGestureBtn.setClickable(true);
			if (hasAction && hasGesture) {
				setSessionCount(0);
				hasAction = false;
				hasGesture = false;
			}
		}
	}

	private void openAlertDialog(String message) {
		Tools.showErrorModal(this, "Gesture Data Missing", message);
		Log.w("CreateGestureActivity", " open alert should show");
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Params.PICK_ACTION_REQUEST) {
			if (resultCode == RESULT_OK) {
				String actionStr = data.getStringExtra("action").replace(" ", "");
				List<Integer> action = new ArrayList<Integer>();
				String[] actionSplited = actionStr.split(",");
				if(actionSplited.length != 0 && !actionSplited[0].equals("")){
					for (int i = 0; i < actionSplited.length; i++) {
						action.add(Integer.parseInt(actionSplited[i]));
					}
					this.hasAction = true;
					gesture.setAction(action);
				}else{
					this.hasAction = false;
				}
				
			}
		}
	}

	private void initLearnGesture() {
		learnGestureBtn = (Button) findViewById(R.id.recordGestureBtn);

		learnGestureBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					hasGesture = true;
					Logger.printLog("Create Gesture Activity", "start learning");
					andgee.getDevice().getProcessingUnit().startLearning();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					Logger.printLog("Create Gesture Activity", "stop learning");
					andgee.getDevice().getProcessingUnit().endLearning();
					increaseSessionCount();
				}
				return true;

			}
		});

	}

	private void initAndgee() {
		andgee = new AndroidWiigee();
		try {
			andgee.getDevice().setAccelerationEnabled(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		andgee.getDevice().onSensorChanged(event);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		andgee.getDevice().onAccuracyChanged(sensor, accuracy);
	}

}
