package com.example.gesturemouseclient.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.dal.GestureDAL;
import com.example.gesturemouseclient.dal.SystemVariablesDAL;

public class FirstTimeLoginActivity extends Activity {

	private Button nextBtn;
	private boolean doneWithInstructions = false;
	private Boolean noInstructions = false;
	private TextView instructionText;
	private CheckBox removeInstructionCheckBox;
	
	private static final String INSTRUCTION_KEY = "instruction_off";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_time_login);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		//TODO: initialize noInstruction boolean from data base.

		instructionText = (TextView) findViewById(R.id.instrcutionText);
		instructionText.setVisibility(View.INVISIBLE);
		
		
		
		 noInstructions = new Boolean(SystemVariablesDAL.get(getApplicationContext(), INSTRUCTION_KEY));
		 
		 Log.d("FirstTimeLoginActivity", "skip instructions: "+noInstructions);

		if(noInstructions)
		{
			goToStartAppPage();
		}else{
			initNextButton();
			showFirstInstruction();
			initInstructionCheckBox();
			
		}
	}

	/**
	 * initialize the next button. 
	 */
	private void initNextButton() {
		nextBtn = (Button) findViewById(R.id.nextBtn);
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(doneWithInstructions){
					goToStartAppPage();						
				}else{
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
				SystemVariablesDAL.set(getApplicationContext(),INSTRUCTION_KEY,""+true);
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
	private void showFirstInstruction()
	{
		instructionText.setVisibility(View.VISIBLE);
	}

	private void showSecondInstruction()
	{
		instructionText.setText(R.string.instructionTwo);
		doneWithInstructions = true;
	}





}
