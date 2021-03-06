package us.to.gesturemouse.threads;

import java.io.IOException;

import us.to.gesturemouse.TcpClient;
import us.to.gesturemouse.activities.MainActivity;
import us.to.gesturemouse.infra.RemoteDeviceInfo;
import android.os.AsyncTask;


public class TcpInitConnectionTask extends AsyncTask<Boolean, Void, Void> {

	private final MainActivity activity;
	private RemoteDeviceInfo remoteDevice;
	private boolean isClose = false;

	/**
	 * Constructor:
	 * 
	 * @param remoteDevice
	 * @param activity
	 */
	public TcpInitConnectionTask(RemoteDeviceInfo remoteDevice,	MainActivity activity) {
		this.activity = activity;
		this.remoteDevice = remoteDevice;
	}

	@Override
	protected void onPreExecute() {
		// TODO: create progress bar
	}

	@Override
	protected Void doInBackground(Boolean... params) {
		boolean openNewTcpConnection = params[0];

		TcpClient client = new TcpClient(remoteDevice, activity);
		client.setTimeout(5);
		try {
			if(openNewTcpConnection)
			{
				client.initControllSession(null,remoteDevice);
//				Logger.printLog("TcpInitialConnection", remoteDevice.getUDPPort() + "");
				isClose = false;
			}else{
				client.closeSession(null,remoteDevice);
//				Logger.printLog("TcpInitialConnection", "close connection with "+remoteDevice.getUDPPort());
				isClose = true;
			}
		} catch (IOException e) {

		}
		return null;
	}



	protected void onProgressUpdate(Integer... progress) {
		// TODO: update bar...
	}

	protected void onPostExecute(Void v) {
//		Logger.printLog("TCPinitialConnection", "onPostExecute start");
		
		if(!isClose)
		{
			activity.onConnectionToRemoteDevice();
		}
//		Logger.printLog("TCPinitialConnection", "onPostExecute end");

	}
}
