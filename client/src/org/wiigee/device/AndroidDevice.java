package org.wiigee.device;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AndroidDevice extends Device implements SensorEventListener {

	private float x0, y0, z0, x1, y1, z1;

	public AndroidDevice() {
		super(true);
		// 'Calibrate' values
		x0 = 0;
		y0 = -SensorManager.STANDARD_GRAVITY;
		z0 = 0;
		x1 = SensorManager.STANDARD_GRAVITY;
		y1 = 0;
		z1 = SensorManager.STANDARD_GRAVITY;

	}

	public void onSensorChanged(SensorEvent event) {
		if (this.accelerationEnabled) {
			double[] newValues = new double[event.values.length];
			for (int i = 0; i < event.values.length; i++) {
				newValues[i] = event.values[i];
			}
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { // leftovers from the old plugin
				// Compensate for gravity
				newValues[0] = (double) (event.values[0] - x0) / (double) (x1 - x0);
				newValues[1] = (double) (event.values[1] - y0) / (double) (y1 - y0);
				newValues[2] = (double) (event.values[2] - z0) / (double) (z1 - z0);
			}
			fireAccelerationEvent(newValues);

		}
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Nothing to do.
	}

}
