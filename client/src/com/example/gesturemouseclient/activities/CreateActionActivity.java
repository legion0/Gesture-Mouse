package com.example.gesturemouseclient.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.infra.Params;

public class CreateActionActivity extends Activity {

	private Button createActionBtn;
	private EditText actionEditTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_action);
		
		createActionBtn = (Button) findViewById(R.id.createActionBtn);
		actionEditTxt = (EditText) findViewById(R.id.actionText);
		
		createActionBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String action = actionEditTxt.getText().toString();
				
				goBackToCreateGestureActivity(action);
			}
		});
	}

	protected void goBackToCreateGestureActivity(String action) {
		Intent intent = new Intent(this, CreateGestureActivity.class);
		intent.putExtra("action", action);
		setResult(RESULT_OK, intent);
		finish();
	}

}
