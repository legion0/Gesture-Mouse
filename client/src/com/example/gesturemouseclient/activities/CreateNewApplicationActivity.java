package com.example.gesturemouseclient.activities;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.R.layout;
import com.example.gesturemouseclient.R.menu;
import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.Tools;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CreateNewApplicationActivity extends Activity {

	private Button createApplicationBtn;
	private EditText applicationEditTxt;
	private String processName;
	private String windowTitle;
//	private TextView headLine;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_application);
		
//		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/BigAppleNF.ttf");
//		headLine = (TextView) findViewById(R.id.createApplicationHeadLine);
//		headLine.setTypeface(tf);
		
		createApplicationBtn = (Button) findViewById(R.id.createNewApplicationBtn);
		applicationEditTxt = (EditText) findViewById(R.id.createApplicationTxt);
		
		Intent intent = getIntent();
		processName = ((String) intent.getExtras().get("process_name")); 
		windowTitle = ((String) intent.getExtras().get("window_title"));
		
		applicationEditTxt.setText(windowTitle);
		
		
		
		createApplicationBtn.setOnClickListener(new OnClickListener() {
		
			
			@Override
			public void onClick(View v) {
				String newApplicationName = applicationEditTxt.getText().toString();
				if (!(windowTitle.startsWith(newApplicationName) || windowTitle.endsWith(newApplicationName))) {
					openAlertDialog("Choose a suffix or postfix from original app name.");
					Log.w("Create New Application", "app name is not legal");
				} else if(newApplicationName.length() >= 20) {
					openAlertDialog("App name should be shorter than 20 characters.");
					Log.w("Create New Application", "app name is not legal");
				} else {
					ApplicationDAL applicationDAL = new ApplicationDAL(newApplicationName, processName, newApplicationName);
					applicationDAL.save(getApplicationContext());
					goBackToMainActivity(applicationDAL.getId());
					Log.v("Create New Application", "app name is legal");
				}
				
				
			}
		
		});
	}
	
	private void openAlertDialog(String message) {
		Tools.showErrorModal(this, "App Name Warning", message);
	}

	protected void goBackToMainActivity(Integer applicationId) {
		Intent intent = new Intent(this, CreateGestureActivity.class);
		intent.putExtra("app_id", applicationId);
		setResult(RESULT_OK, intent);
		finish();
	}

}
