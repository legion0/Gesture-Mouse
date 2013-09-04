package com.example.gesturemouseclient;

import java.net.SocketException;

import org.wiigee.control.AndroidWiigee;
import org.wiigee.event.GestureEvent;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.KeyEvent;

import com.example.gesturemouseclient.infra.Logger;

public class GestureListener implements SensorEventListener, org.wiigee.event.GestureListener {

	private AndroidWiigee androidWiigee;
	
	private static final int LEARN_KEY = KeyEvent.KEYCODE_T;
    private static final int START_KEY = KeyEvent.KEYCODE_SPACE;
    private static final int STOP_KEY = KeyEvent.KEYCODE_ENTER;

	/**
	 * Constctur:
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public GestureListener() {
		super();
		this.androidWiigee = new AndroidWiigee();
		this.androidWiigee.setRecognitionButton(START_KEY);
		this.androidWiigee.setCloseGestureButton(STOP_KEY);
		this.androidWiigee.setTrainButton(LEARN_KEY);
		this.androidWiigee.addGestureListener(this);

		Logger.printLog("GestureListener", "constructed !");
	}

	@Override
	public void gestureReceived(GestureEvent event) {
		Logger.printLog("gestureReceived", "id="+event.getId());
		Logger.printLog("gestureReceived", "prob="+event.getProbability());
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		this.androidWiigee.getDevice().onSensorChanged(event);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		this.androidWiigee.getDevice().onAccuracyChanged(sensor, accuracy);
	}

}
