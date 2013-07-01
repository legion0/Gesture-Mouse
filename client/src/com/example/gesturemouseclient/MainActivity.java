package com.example.gesturemouseclient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final Button flickLeftButton = (Button) findViewById(R.id.btnFlickLeft);
		final Button flickRightButton = (Button) findViewById(R.id.btnFlickRight);
		final Button flickUpButton = (Button) findViewById(R.id.btnFlickUp);
		final Button flickDownButton = (Button) findViewById(R.id.btnFlickDown);
		final Button mouseButton = (Button) findViewById(R.id.btnMouse);
		final Button gestureButton = (Button) findViewById(R.id.btnGesture);
		
		changeVisability(gestureButton);
		changeVisability(mouseButton);
		changeVisability(flickDownButton);
		changeVisability(flickUpButton);
		changeVisability(flickLeftButton);
		changeVisability(flickRightButton);
	
	
		Log.i("initialPcConnection","start1");
		Log.e("initialPcConnection","start1");
		Log.d("initialPcConnection","start1");
		
		final FindServer findServer = new FindServer(this);
		findServer.execute();
		

		flickLeftButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				
			}
		
		});
		flickRightButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
			
		});
		flickUpButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		flickDownButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		mouseButton.setOnClickListener(new OnClickListener(){

			private boolean pauseMouse = true;

			@Override
			public void onClick(View v) {
				if(pauseMouse)
				{
//					findServer.resumeMouse();
//					pauseMouse = false;
				}
				else
				{
//					gestureHandler.pauseMouse();
//					pauseMouse = true;
				}
				
			}
		});
		gestureButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
//				gestureHandler.closeAll();
				
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void changeVisability(Button b)
	{
		if(b.getVisibility() == b.VISIBLE)
		{
			b.setVisibility(b.INVISIBLE);
		}
		else
		{
			b.setVisibility(b.VISIBLE);
		}
	}
}
