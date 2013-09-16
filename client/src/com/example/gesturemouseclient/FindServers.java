package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.msgpack.type.RawValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;

import android.os.AsyncTask;
import android.os.SystemClock;

import com.example.gesturemouseclient.Client.Result;
import com.example.gesturemouseclient.activities.FindServersActivity;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.ResponseReader;

public class FindServers extends AsyncTask<Void, Void, List<RemoteDeviceInfo>> {

	private FindServersActivity findServerActivity;

	public FindServers(FindServersActivity findServerActivity) {
		this.findServerActivity = findServerActivity;

	}

	@Override
	protected void onPreExecute() {
		// TODO: create progress bar
	}

	@Override
	protected List<RemoteDeviceInfo> doInBackground(Void... params) {
		Logger.printLog("initialPcConnection", "doInBackground");
		List<RemoteDeviceInfo> deviceList = new LinkedList<RemoteDeviceInfo>();

		MyResponseReader response = new MyResponseReader();
		Client client = new Client();
		client.setTimeout(5);
		Result[] results = new Result[0];
		try {
			results = client.findAll("GM");
			for (Result res : results) {
				response.read(res.extraInfo);
				deviceList.add(new RemoteDeviceInfo(res.inetSocketAddress.getPort(), res.inetSocketAddress.getAddress(), response.machineName));
			}
			return deviceList;
		} catch (IOException e) {
			Logger.printLog("initialPcConnection", "Failed to find Pc connection.");
			return null;
		}
	}

	static class MyResponseReader implements ResponseReader {

		public String machineName;
		private final RawValue key_machine_name = ValueFactory.createRawValue("machine_name".getBytes());

		@Override
		public void read(Value extra_info) {
			machineName = extra_info.asMapValue().get(key_machine_name).asRawValue().getString();
		}
	};

	protected void onPostExecute(List<RemoteDeviceInfo> result) {
		Logger.printLog("initialPcConnection", "onPostExecute");
		for (RemoteDeviceInfo deviceItem : result) {
			findServerActivity.addDevice(deviceItem);
		}

		findServerActivity.stopProgressBar();
	}

}
