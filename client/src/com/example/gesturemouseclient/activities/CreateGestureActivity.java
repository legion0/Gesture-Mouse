package com.example.gesturemouseclient.activities;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.R.layout;
import com.example.gesturemouseclient.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class CreateGestureActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_gesture);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_gesture, menu);
		return true;
	}

}
