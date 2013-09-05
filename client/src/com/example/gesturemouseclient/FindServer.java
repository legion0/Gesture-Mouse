package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import org.msgpack.type.RawValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;

import android.os.AsyncTask;
import android.os.SystemClock;

import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.ResponseReader;

public class FindServer extends AsyncTask<Void, Void, List<RemoteDeviceInfo>> {

	private FindServerActivety mainActivity;

	public FindServer(FindServerActivety mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	protected void onPreExecute() {
		// TODO: create progress bar
	}

	@Override
	protected List<RemoteDeviceInfo> doInBackground(Void... params) {
		List<RemoteDeviceInfo> deviceList = new LinkedList<RemoteDeviceInfo>();
		Logger.printLog("initialPcConnection", "doInBackground");
		SystemClock.sleep(1000);

		MyResponseReader response = new MyResponseReader();
		Client client = new Client(response);
		client.setTimeout(5);
		InetSocketAddress serverAddress;
		try {
			serverAddress = client.findFirst("GM");
			if (serverAddress != null) {
				Logger.printLog("initialPcConnection", serverAddress.toString());
				Logger.printLog("initialPcConnection", response.machineName);
				deviceList.add(new RemoteDeviceInfo(serverAddress.getPort(), serverAddress.getAddress(), response.machineName));
			} else {
				Logger.printLog("initialPcConnection", "no sever is founds");
			}
			return deviceList;
		} catch (IOException e) {
			Logger.printLog("initialPcConnection", "Failed to find Pc connection.");
			return null;
		}
		// return null;
	}

	static class MyResponseReader implements ResponseReader {

		public String machineName;
		private final RawValue key_machine_name = ValueFactory.createRawValue("machine_name".getBytes());

		@Override
		public void read(Value extra_info) {
			machineName = extra_info.asMapValue().get(key_machine_name).asRawValue().getString();
		}
	};

	protected void onProgressUpdate(Integer... progress) {
		// TODO: update bar...
	}

	protected void onPostExecute(List<RemoteDeviceInfo> result) {
		Logger.printLog("initialPcConnection", "onPostExecute");
		for (RemoteDeviceInfo deviceItem : result) {
			mainActivity.addDevice(deviceItem);
		}

		mainActivity.stopProgressBar();
	}

}
