package us.to.gesturemouse.infra;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Tools {
	public static boolean equals(String a, String b) {
		if (a == null && b == null) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

	public static void showErrorModal(Activity activity, String title, String message) {
		showErrorModal(activity, title, message, "OK", null);
	}

	public static void showErrorModal(final Activity activity, String title, String message, String positiveButton, final Runnable runnable) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		if (title != null) {
			builder.setTitle(title);
		}
		if (message != null) {
			builder.setMessage(message);
		}
		builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				if (runnable != null) {
					runnable.run();
				}
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public static Sensor getGestureSensor(SensorManager sensorManager) {
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (sensor == null) {
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		return sensor;
	}

	public static Sensor getMouseSensor(SensorManager sensorManager) {
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		return sensor;
	}

	public static boolean registerMouseSensor(SensorManager sensorManager, SensorEventListener sensorEventListener, int rate) {
		if (sensorManager == null || sensorEventListener == null) {
			return false;
		}
		Sensor sensor = getMouseSensor(sensorManager);
		return sensorManager.registerListener(sensorEventListener, sensor, rate);
	}

	public static boolean registerGestureSensor(SensorManager sensorManager, SensorEventListener sensorEventListener, int rate) {
		if (sensorManager == null || sensorEventListener == null) {
			return false;
		}
		Sensor sensor = getGestureSensor(sensorManager);
		if (sensor != null) {
			return sensorManager.registerListener(sensorEventListener, sensor, rate);
		}
		return false;
	}

	public static void unregisterGestureSensor(SensorManager sensorManager, SensorEventListener sensorEventListener) {
		if (sensorManager == null || sensorEventListener == null) {
			return;
		}
		Sensor sensor = getGestureSensor(sensorManager);
		if (sensor != null) {
			sensorManager.unregisterListener(sensorEventListener, sensor);
		}
	}

	public static void unregisterMouseSensor(SensorManager sensorManager, SensorEventListener sensorEventListener) {
		if (sensorManager == null || sensorEventListener == null) {
			return;
		}
		Sensor sensor = getMouseSensor(sensorManager);
		if (sensor != null) {
			sensorManager.unregisterListener(sensorEventListener, sensor);
		}
	}

	public static boolean closeSocket(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
				return true;
			} catch (IOException e) {
			}
		}
		return false;
	}

	public static boolean closeSocket(ServerSocket socket) {
		if (socket != null) {
			try {
				socket.close();
				return true;
			} catch (IOException e) {
			}
		}
		return false;
	}
}
