package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.msgpack.type.RawValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.ResponseReader;

public class TcpInitConnection extends AsyncTask<Void, Void, Integer> {

	private int tcp_outgoing_port;
	private InetAddress address;
	private String deviceName;
	private final static String TCP_IN_GOING_PORT = "35202";
	private final MainActivity mainActivity;
	private DeviceItem device;

	/**
	 * Constructor:
	 * 
	 * @param device
	 * @param mainActivity
	 */
	public TcpInitConnection(DeviceItem device, String deviceName,
			MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		tcp_outgoing_port = device.getControlPort();
		address = device.getAddress();
		this.deviceName = deviceName;
		this.device = device;
	}

	@Override
	protected void onPreExecute() {
		// TODO: create progress bar
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyResponseReader response = new MyResponseReader();
		TcpClient client = new TcpClient(response, tcp_outgoing_port,
				deviceName, address);
		client.setTimeout(5);
		int serverUdp;
		try {

			serverUdp = client.initControllSession(TCP_IN_GOING_PORT, null);
			device.setTcpSocket(client.getSocket());
			Logger.printLog("TcpInitialConnection", response.udpPort);
			return serverUdp;
		} catch (IOException e) {
			Logger.printLog("TcpInitialConnection",
					"Failed to find TCP connection.");
			return null;
		}
	}

	static class MyResponseReader implements ResponseReader {

		public String udpPort;
		private final RawValue udp_port = ValueFactory
				.createRawValue("udp_port".getBytes());

		@Override
		public void read(Value extra_info) {
			udpPort = extra_info.asMapValue().get(udp_port).asRawValue()
					.getString();
		}
	};

	protected void onProgressUpdate(Integer... progress) {
		// TODO: update bar...
	}

	protected void onPostExecute(Integer result) {
		Logger.printLog("TCPinitialConnection", "onPostExecute");

		mainActivity.setControlSession(new InetSocketAddress(address, result));

	}

}
