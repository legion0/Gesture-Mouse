package com.example.gesturemouseclient.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.infra.Tools;

public class CreateNewApplicationActivity extends Activity {

	private static final int REQUEST_NEW_ACTION = 0;
	private ImageView createApplicationBtn;
	private EditText applicationEditTxt;
	private String processName;
	private String windowTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_application);
		createApplicationBtn = (ImageView) findViewById(R.id.createNewApplicationBtn);
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
				} else if (newApplicationName.length() >= 20) {
					openAlertDialog("App name should be shorter than 20 characters.");
					Log.w("Create New Application", "app name is not legal");
				} else {
					ApplicationDAL applicationDAL = new ApplicationDAL(newApplicationName, processName, newApplicationName);
					applicationDAL.save(getApplicationContext());
					Log.v("Create New Application", "app name is legal");
					gotoActionActiv(applicationDAL.getId());
				}
			}

		});
	}

	private void openAlertDialog(String message) {
		Tools.showErrorModal(this, "App Name Warning", message);
	}

	protected void gotoActionActiv(Integer applicationId) {
		Intent intent = new Intent(this, CreateActionActivity.class);
		intent.putExtra("app_id", applicationId);
		startActivityForResult(intent, REQUEST_NEW_ACTION);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_NEW_ACTION && resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
