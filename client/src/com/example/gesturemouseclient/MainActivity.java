package com.example.gesturemouseclient;

import java.util.concurrent.BlockingDeque;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.GyroSample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class MainActivity extends Activity{
	
	private DeviceItem device;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

//	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//		super.onKeyLongPress(keyCode, event);
//		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
//		{
//			
//			return true;
//		}else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
//		{
//			
//			return true;
//		}
//		return false;
//	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
		{
			device.getClickQueue().offerLast(keyCode);			
			return true;
		}else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			device.getClickQueue().offerLast(keyCode);
			return true;
		}
		return false;
	}





}
