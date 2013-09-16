package com.example.gesturemouseclient;

import java.io.IOException;
import java.util.List;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;
import org.wiigee.event.GestureListener;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Maarten 'MrSnowflake' Krijn
 */
public class AndgeeTest extends Activity implements SensorEventListener {
	protected static final String TAG = "AndgeeTest";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.andgee);

		mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		lblStatus = (TextView) findViewById(R.id.status);
		lblStartKey = (TextView) findViewById(R.id.lblStartKey);
		lblStartKey.setText("Hold Space to recognize");
		lblStopKey = (TextView) findViewById(R.id.lblStopKey);
		lblStopKey.setText("Press enter to stop");
		lblLearnKey = (TextView) findViewById(R.id.lblLearnKey);
		lblLearnKey.setText("Hold T to learn");

		Button reg = (Button) findViewById(R.id.regonize);
		reg.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					lblStatus.setText("Recognizing");
					andgee.getDevice().fireButtonPressedEvent(START_KEY);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					lblStatus.setText("");
					andgee.getDevice().fireButtonReleasedEvent(START_KEY);
				}
				return false;
			}

		});

		Button learn = (Button) findViewById(R.id.learn);
		learn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					lblStatus.setText("Learning");
					andgee.getDevice().fireButtonPressedEvent(LEARN_KEY);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					lblStatus.setText("");
					andgee.getDevice().fireButtonReleasedEvent(LEARN_KEY);
				}
				return false;
			}

		});

		Button stop = (Button) findViewById(R.id.stop);
		stop.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					lblStatus.setText("Saving");
					andgee.getDevice().fireButtonPressedEvent(STOP_KEY);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					lblStatus.setText("");
					andgee.getDevice().fireButtonReleasedEvent(STOP_KEY);
				}
				return false;
			}

		});

		andgee.setRecognitionButton(START_KEY);
		andgee.setCloseGestureButton(STOP_KEY);
		andgee.setTrainButton(LEARN_KEY);

		andgee.addGestureListener(new GestureListener() {
			@Override
			public void gestureReceived(GestureEvent event) {
				Log.i(TAG, "GestureReceived " + event.getId());
				lblStatus.setText("Recognized: " + event.getId() + " Probability: " + event.getProbability());
			}
		});
	}

	private static final int LEARN_KEY = KeyEvent.KEYCODE_T;
	private static final int START_KEY = KeyEvent.KEYCODE_SPACE;
	private static final int STOP_KEY = KeyEvent.KEYCODE_ENTER;

	/**
	 * TODO Zorg voor juiste return waardes van Andgee.onKeyDown() en Up()
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == STOP_KEY)
			lblStatus.setText("Stop");

		else if (keyCode == LEARN_KEY)
			lblStatus.setText("Learning");
		else if (keyCode == START_KEY)
			lblStatus.setText("Recognizing");

		if (keyCode == STOP_KEY || keyCode == LEARN_KEY || keyCode == START_KEY) {
			andgee.getDevice().fireButtonPressedEvent(event.getKeyCode());
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * TODO Zorg voor juiste return waardes
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == STOP_KEY || keyCode == LEARN_KEY || keyCode == START_KEY) {
			andgee.getDevice().fireButtonReleasedEvent(keyCode);
			lblStatus.setText("");
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
		try {
			andgee.getDevice().setAccelerationEnabled(true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onPause() {
		try {
			andgee.getDevice().setAccelerationEnabled(false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		mSensorManager.unregisterListener(andgee.getDevice());
		super.onPause();
	}

	private TextView lblStatus;
	private TextView lblStartKey;
	private TextView lblStopKey;
	private TextView lblLearnKey;

	private SensorManager mSensorManager;
	private AndroidWiigee andgee = new AndroidWiigee();

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Logger.printLog("onSensorChanged", Arrays.toString(event.values));
		andgee.getDevice().onSensorChanged(event);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		andgee.getDevice().onAccuracyChanged(sensor, accuracy);
	}
}
