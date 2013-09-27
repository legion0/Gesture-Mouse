package com.example.gesturemouseclient.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.dal.SystemVariablesDAL;
import com.example.gesturemouseclient.infra.Tools;

public class FirstTimeLoginActivity extends Activity {

	private ImageView nextBtn;
	private boolean doneWithInstructions = false;
	private Boolean noInstructions = false;
	private TextView instructionText;
	private CheckBox removeInstructionCheckBox;

	private static final String INSTRUCTION_KEY = "instruction_off";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Activity this_ = this;
		setContentView(R.layout.activity_first_time_login);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		// TODO: initialize noInstruction boolean from data base.

		instructionText = (TextView) findViewById(R.id.instrcutionText);
		instructionText.setVisibility(View.INVISIBLE);

		noInstructions = Boolean.valueOf(SystemVariablesDAL.get(this, INSTRUCTION_KEY));

		Log.d("FirstTimeLoginActivity", "skip instructions: " + noInstructions);

		if (noInstructions) {
			goToStartAppPage();
		} else {
			initNextButton();
			showFirstInstruction();
			initInstructionCheckBox();

		}

		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		final Runnable finish = new Runnable() {

			@Override
			public void run() {
				this_.finish();
			}
		};
		if (sensor == null) {
			Tools.showErrorModal(this, "Error", "Your device does not have the GYROSCOPE sensor and cannot run this application.", "OK", finish);
		}
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		if (sensor == null) {
			Tools.showErrorModal(this, "Error", "Your device does not have the ROTATION_VECTOR virtual sensor and cannot run this application.", "OK", finish);
		}
	}

	/**
	 * initialize the next button.
	 */
	private void initNextButton() {
		nextBtn = (ImageView) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (doneWithInstructions) {
					goToStartAppPage();
				} else {
					showSecondInstruction();
				}
			}
		});
	}

	/**
	 * initialize the check box for removing the instruction made for first time users.
	 */
	private void initInstructionCheckBox() {
		removeInstructionCheckBox = (CheckBox) findViewById(R.id.removeInstructionCheckBox);
		removeInstructionCheckBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SystemVariablesDAL.set(FirstTimeLoginActivity.this, INSTRUCTION_KEY, "" + true);
			}
		});
	}

	/**
	 * go to the find server activity where you'll locate the device to connect with.
	 */
	private void goToStartAppPage() {
		Intent intent = new Intent(this, FindServersActivity.class);
		startActivity(intent);
	}

	/**
	 * show the first instruction.
	 */
	private void showFirstInstruction() {
		instructionText.setVisibility(View.VISIBLE);
		instructionText.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private void showSecondInstruction() {
		instructionText.setText(R.string.instructionTwo);
		doneWithInstructions = true;
	}

}
