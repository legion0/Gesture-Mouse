package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.InetAddress;

import org.msgpack.type.RawValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;

import android.os.AsyncTask;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.ResponseReader;

public class TcpInitConnection extends AsyncTask<Void, Void, Void> {

	private int tcp_outgoing_port;
	private InetAddress address;
	private String deviceName;
	private final MainActivity activity;
	private DeviceItem device;

	/**
	 * Constructor:
	 * 
	 * @param device
	 * @param activity
	 */
	public TcpInitConnection(DeviceItem device,	MainActivity activity) {
		this.activity = activity;
		tcp_outgoing_port = device.getControlPort();
		address = device.getAddress();
		this.deviceName = device.getMachineName();
		this.device = device;
	}

	@Override
	protected void onPreExecute() {
		// TODO: create progress bar
	}

	@Override
	protected Void doInBackground(Void... params) {
		MyResponseReader response = new MyResponseReader();
		TcpClient client = new TcpClient(response, tcp_outgoing_port,
				deviceName, address);
		client.setTimeout(5);
		try {
			client.initControllSession(Params.TCP_IN_GOING_PORT, null,device);
			Logger.printLog("TcpInitialConnection", device.getUDPPort() + "");
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

	static class MyResponseReader implements ResponseReader {

		public String udpPort;
		private final RawValue udp_port = ValueFactory
				.createRawValue("udp".getBytes());

		@Override
		public void read(Value extra_info) {
			udpPort = extra_info.asMapValue().get(udp_port).asRawValue()
					.getString();
		}
	};


}
