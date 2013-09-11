package com.example.gesturemouseclient;

import java.io.IOException;

import android.os.AsyncTask;

import com.example.gesturemouseclient.activities.MainActivity;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;

public class TcpInitConnection extends AsyncTask<Void, Void, Void> {

	private final MainActivity activity;
	private RemoteDeviceInfo remoteDevice;

	/**
	 * Constructor:
	 * 
	 * @param remoteDevice
	 * @param activity
	 */
	public TcpInitConnection(RemoteDeviceInfo remoteDevice,	MainActivity activity) {
		this.activity = activity;
		this.remoteDevice = remoteDevice;
	}

	@Override
	protected void onPreExecute() {
		// TODO: create progress bar
	}

	@Override
	protected Void doInBackground(Void... params) {
		TcpClient client = new TcpClient(remoteDevice, activity.getApplicationContext());
		client.setTimeout(5);
		try {
			client.initControllSession(Params.TCP_IN_GOING_PORT, null,remoteDevice);
			Logger.printLog("TcpInitialConnection", remoteDevice.getUDPPort() + "");
		} catch (IOException e) {
			Logger.printLog("TcpInitialConnection",
					"Failed to find TCP connection.");
		}
		return null;
	}
	
	protected void onProgressUpdate(Integer... progress) {
		// TODO: update bar...
	}

	protected void onPostExecute(Void v) {
		Logger.printLog("TCPinitialConnection", "onPostExecute start");

		
		activity.onConnectionToRemoteDevice();
		Logger.printLog("TCPinitialConnection", "onPostExecute end");

	}
}
