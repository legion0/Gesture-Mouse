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

public class MainActivity extends Activity implements SensorEventListener {

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

		gestureListener = new GestureListener();
		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
		List<Sensor> sensorList = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
		//TODO: Error checks
		sm.registerListener(gestureListener, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);

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

	@Override
	protected void onResume(){
		super.onResume();
		List<Sensor> sensorList = sm.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
		if(sensorList == null || sensorList.size() < 1){
			return; // TODO: error for no sensor
		}
//		sm.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_GAME);
		resumeAllThreads();
	}

	@Override
	protected void onPause(){
		super.onPause();
		sm.unregisterListener(this);
		pauseAllThreads();
	}

	protected void onStop(){
		super.onStop();
		sm.unregisterListener(this);
		stopAllThreads();
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		sm.unregisterListener(this);
		stopAllThreads();
	}

	private void resumeAllThreads() {
		if(fastConnection != null)
			fastConnection.resumeRun();
		if(hardwareListener != null)
			hardwareListener.resumeRun();
	}

	private void pauseAllThreads() {
		if(fastConnection != null)
			fastConnection.pauseRun();
		if(hardwareListener != null)
			hardwareListener.pauseRun();
	}

	private void stopAllThreads() {
		if(fastConnection != null)
			fastConnection.stopRun();
		if(hardwareListener != null)
			hardwareListener.stopRun();
	}



	public void setControlSession() {
		try {
			Logger.printLog("main activety : ", "setControlSession");
			fastConnection = new FastSensorConnection(device);
			fastConnection.start();

			hardwareListener = new HardwareListener(device);
			hardwareListener.start();

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
			
			
			float[] rotationMatrix = new float[9];
			SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
			
			float[] newValues = new float[3];
			SensorManager.getOrientation(rotationMatrix, newValues );
			
			float x = newValues[0];
			float y = newValues[1];
			float z = newValues[2];
			
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
