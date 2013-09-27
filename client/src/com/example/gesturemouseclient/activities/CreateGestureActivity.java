package com.example.gesturemouseclient.activities;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.logic.GestureModel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.dal.GestureDAL;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Tools;

public class CreateGestureActivity extends Activity implements SensorEventListener {

	private AndroidWiigee andgee;
	private ImageView learnGestureBtn;
	private SensorManager sensorManager;
	protected TextView lblStatus;
	private ImageView saveGestureBtn;
	private GestureDAL gesture;
	private ProgressBar saveProgressBar;
	private int appId;
	private int[] action;
	private TextView createGestureTitle;
	private int traningSequenceSizeMin;
	private String applicationName;
	private String processName;
	private String windowTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_gesture);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		Intent intent = getIntent();
		appId = intent.getIntExtra("app_id", -1);
		applicationName = intent.getStringExtra("app_name");
		windowTitle = intent.getStringExtra("window_title");
		processName = intent.getStringExtra("process_name");
		action = intent.getIntArrayExtra("action");

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		lblStatus = (TextView) findViewById(R.id.recognizeGestureTxt);

		andgee = new AndroidWiigee();
		andgee.getDevice().setAccelerationEnabled(true);
		learnGestureBtn = (ImageView) findViewById(R.id.recordGestureBtn);

		learnGestureBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Logger.printLog("Create Gesture Activity", "start learning");
					andgee.getDevice().getProcessingUnit().startLearning();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					Logger.printLog("Create Gesture Activity", "stop learning");
					andgee.getDevice().getProcessingUnit().endLearning();
					updateSessionCountLabel();
				}
				return true;
			}
		});

		saveProgressBar = (ProgressBar) findViewById(R.id.saveProgressBar);
		saveProgressBar.setVisibility(View.GONE);

		saveGestureBtn = (ImageView) findViewById(R.id.saveGestureBtn);
		saveGestureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSaveGesture();
			}
		});

		createGestureTitle = (TextView) findViewById(R.id.headLineCreateGesture);

		andgee.getDevice().getProcessingUnit().getClassifier().clear();
		gesture = new GestureDAL(null, action, null);
		updateSessionCountLabel();

		Resources res = getResources();
		traningSequenceSizeMin = res.getInteger(R.integer.trainingSequenceSizeMin);
	}

	private int trainingSequenceSize() {
		return andgee.getDevice().getProcessingUnit().trainingSequenceSize();
	}

	private void startSaveGesture() {
		if (trainingSequenceSize() < traningSequenceSizeMin) {
			openAlertDialog("Record a new gesture, at least 3 sessions are required but recording 10 times will result with better identification.");
			return;
		}
		if (createGestureTitle.getText().toString().length() < 1) {
			openAlertDialog("Please enter a name for the Gesture.");
			return;
		}
		saveGestureBtn.setClickable(false);
		SaveAsyncTask save = new SaveAsyncTask(this);
		save.execute();
	}

	@Override
	protected void onStart() {
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
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

	private void updateSessionCountLabel() {
		lblStatus.setText(String.format("recorded %s session out of the recommended 10.", trainingSequenceSize()));
	}

	class SaveAsyncTask extends AsyncTask<Void, Void, Void> {
		
		public SaveAsyncTask(Context context) {
			super();
			this.context = context;
		}

		private Context context;

		@Override
		protected void onPreExecute() {
			saveProgressBar.setVisibility(View.VISIBLE);
			saveGestureBtn.setVisibility(View.GONE);
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Logger.printLog("Create Gesture", "save gesture.");
			int classifierGestureId = andgee.getDevice().getProcessingUnit().saveLearningAsGesture();
			GestureModel gestureModel = andgee.getDevice().getProcessingUnit().getClassifier().getGestureModel(classifierGestureId);
			gesture.setModel(gestureModel);
			gesture.save(context);

			if (appId == -1) {
				ApplicationDAL applicationDAL = new ApplicationDAL(applicationName, processName, windowTitle);
				applicationDAL.save(context);
				appId = applicationDAL.getId();
			}

			gesture.addToApplication(context, appId);
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			setResult(RESULT_OK);
			finish();
		}
	}

	private void openAlertDialog(String message) {
		Tools.showErrorModal(this, "Gesture Data Missing", message);
		Log.w("CreateGestureActivity", " open alert should show");
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
