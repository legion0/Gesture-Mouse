package com.example.gesturemouseclient;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingDeque;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.GyroSample;
import com.example.gesturemouseclient.infra.Logger;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class MainActivity extends Activity{

	private DeviceItem device;
	FastSensorConnection fastConnection;
	HardwareListener hardwareListener;
	private GestureListener gestureListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TcpInitConnection tcpConnection = new TcpInitConnection(device,this);
		tcpConnection.execute();
	}
	
	public void setControlSession() {
		try {
			fastConnection = new FastSensorConnection(device);
			fastConnection.start();
			
			hardwareListener = new HardwareListener(device);
			hardwareListener.start();
			
			gestureListener = new GestureListener(device);
			//gestureListener.start();
			
			
			
		} catch (SocketException e) {
			Logger.printLog("MainActivet", "failed to open udp socket");
		}
		
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

	//TODO: remember to check the state of the device, it's possible we dont want to update since we're in anotre state. 
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

	//TODO: remember to check the state of the device, it's possible we dont want to update since we're in anotre state.
	public void onSensorChanged(SensorEvent event){
		if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			GyroSample sample = new GyroSample(x, y, z);
			
			device.getGyroQueue().offerLast(sample );
			device.getGestureQueue().offerLast(sample );
		}
	}





}
