package com.example.gesturemouseclient;

import java.io.IOException;

import android.os.AsyncTask;

import com.example.gesturemouseclient.activities.MainActivity;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;

public class TcpInitConnection extends AsyncTask<Boolean, Void, Void> {

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
	protected Void doInBackground(Boolean... params) {
		boolean close = params[0];

		TcpClient client = new TcpClient(remoteDevice, activity.getApplicationContext());
		client.setTimeout(5);
		try {
			if(!close)
			{
				client.initControllSession(Params.TCP_IN_GOING_PORT, null,remoteDevice);
				Logger.printLog("TcpInitialConnection", remoteDevice.getUDPPort() + "");
			}else{
				client.closeSession(Params.TCP_IN_GOING_PORT, null,remoteDevice);
				Logger.printLog("TcpInitialConnection", "close connection with "+remoteDevice.getUDPPort());
			}
		} catch (IOException e) {

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
