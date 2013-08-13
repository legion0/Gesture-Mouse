package com.example.gesturemouseclient;

import java.net.SocketException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.GyroSample;
import com.example.gesturemouseclient.infra.Logger;

public class MainActivity extends Activity implements SensorEventListener{

	private DeviceItem device;
	FastSensorConnection fastConnection;
	HardwareListener hardwareListener;
	private GestureListener gestureListener;
	private TextView pcConnectedName;
	private TextView appConnectedName;
	private boolean volumeDownIsPressed = false;
	private boolean volumeUpIsPressed = false;
	private SensorManager sm;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
		
		setContentView(R.layout.activity_main);
		Intent intent = getIntent();
		device = ((DeviceItem)intent.getExtras().get("device"));
		
		pcConnectedName = (TextView) findViewById(R.id.connectedPcName);
		appConnectedName = (TextView) findViewById(R.id.connectedAppName);
		
		pcConnectedName.setText(device.getMachineName());
		appConnectedName.setText("Mouse");
		
		TcpInitConnection tcpConnection = new TcpInitConnection(device,this);
		tcpConnection.execute();
	}
	
	protected void onResume(){
		super.onResume();
		List<Sensor> sensorList = sm.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
		if(sensorList == null || sensorList.size() < 1){
			return;
		}
		sm.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
		resumeAllThreads();
	}
	
	protected void onPuase(){
		super.onPause();
		sm.unregisterListener(this);
		pauseAllThreads();
	}
	
	private void resumeAllThreads() {
		fastConnection.resumeRun();
		hardwareListener.resumeRun();
		gestureListener.resumeRun();
	}
	
	private void pauseAllThreads() {
		fastConnection.pauseRun();
		hardwareListener.pauseRun();
		gestureListener.pauseRun();
	}

	private void stopAllThreads() {
		fastConnection.stopRun();
		hardwareListener.stopRun();
		gestureListener.stopRun();
	}

	protected void onStop(){
		super.onStop();
		sm.unregisterListener(this);
		stopAllThreads();
	}

	public void setControlSession() {
		try {
			Logger.printLog("main activety : ", "setControlSession");
			fastConnection = new FastSensorConnection(device);
			fastConnection.start();

			hardwareListener = new HardwareListener(device);
			hardwareListener.start();

			gestureListener = new GestureListener(device);
			//gestureListener.start();



		} catch (SocketException e) {
			Logger.printLog("MainActivet", "failed to open udp socket, "+e.getMessage());
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
			if(!volumeDownIsPressed)
			{
				volumeDownIsPressed = true;
				device.getClickQueue().offerLast(2);
				return true;
			}
		}else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
		{
			if(!volumeUpIsPressed)
			{
				volumeUpIsPressed = true;
				device.getClickQueue().offerLast(0);
				return true;
			}
		}
		return false;
	}

public boolean onKeyUp(int keyCode, KeyEvent event) {
	super.onKeyUp(keyCode, event);
	if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
	{
		if(volumeDownIsPressed)
		{
			volumeDownIsPressed = false;
			device.getClickQueue().offerLast(3);			
			return true;
		}
	}else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
	{
		if(volumeUpIsPressed)
		{
			volumeUpIsPressed = false;
			device.getClickQueue().offerLast(1);
			return true;
		}
	}
	return false;
}

//TODO: remember to check the state of the device, it's possible we dont want to update since we're in anotre state.
public void onSensorChanged(SensorEvent event){
	Logger.printLog("onSensorChanged", Integer.toString(event.sensor.getType()));
	if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		Logger.printLog("onSensorChanged", "sendSample("+x+","+y+","+z+")");
		GyroSample sample = new GyroSample(x, y, z);

		device.getGyroQueue().offerLast(sample );
		device.getGestureQueue().offerLast(sample );
	}
}

@Override
public void onAccuracyChanged(Sensor sensor, int accuracy) {
	// TODO Auto-generated method stub
	
}


}
