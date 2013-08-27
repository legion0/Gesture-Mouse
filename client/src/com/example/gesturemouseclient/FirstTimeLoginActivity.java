package com.example.gesturemouseclient;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class FirstTimeLoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_time_login);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_time_login, menu);
		return true;
	}

}
