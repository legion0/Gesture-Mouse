package com.example.gesturemouseclient.activities;

import org.w3c.dom.Text;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.R.id;
import com.example.gesturemouseclient.R.layout;
import com.example.gesturemouseclient.R.string;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FirstTimeLoginActivity extends Activity {

	private Button nextBtn;
	boolean doneWithInstructions = false;
	private boolean noInstructions = false;
	private TextView instructionText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_time_login);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		instructionText = (TextView) findViewById(R.id.instrcutionText);
		instructionText.setVisibility(View.INVISIBLE);
		
		if(noInstructions)
		{
			goToStartAppPage();
		}
		
		showFirstInstruction();
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
