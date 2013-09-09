package com.example.gesturemouseclient.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.gesturemouseclient.R;

public class FirstTimeLoginActivity extends Activity {

	private Button nextBtn;
	boolean doneWithInstructions = true;
	private boolean noInstructions = true;
	private TextView instructionText;
	private CheckBox removeInstructionCheckBox;

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

		if(noInstructions)
		{
			goToStartAppPage();
		}else{
			showFirstInstruction();
			initInstructionCheckBox();
			initNextButton();
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
		nextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//TODO: write down state to date base...
			}
		});
	}

	/**
	 * go to the find server activity where you'll locate the device to connect with.
	 */
	private void goToStartAppPage() {
		Intent intent = new Intent(this, FindServerActivity.class);
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
