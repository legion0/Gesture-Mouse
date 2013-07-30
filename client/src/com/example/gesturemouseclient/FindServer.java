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

import android.os.AsyncTask;
import android.os.SystemClock;


public class FindServer extends AsyncTask<Void,Void,DeviceItem> {

	

	private MainActivity mainActivity;
	
	public FindServer(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	protected void onPreExecute(){
		//TODO: create progress bar
	}

	@Override
	protected DeviceItem doInBackground(Void... params) {
		Logger.printLog("initialPcConnection","doInBackground");
		SystemClock.sleep(1000);
		
		MyResponseReader response = new MyResponseReader();
		Client client = new Client(response);
		client.setTimeout(5);
		InetSocketAddress serverAddress;
		try {
			serverAddress = client.findFirst("GM");
			Logger.printLog("initialPcConnection", serverAddress.toString());
			Logger.printLog("initialPcConnection", response.machineName);
			
			return new DeviceItem(serverAddress.getPort(),serverAddress.getAddress(),response.machineName);
		} catch (IOException e) {
			Logger.printLog("initialPcConnection", "Failed to find Pc connection.");
			return null;
		}
		//return null;
	}

	static class MyResponseReader implements ResponseReader {

		public String machineName;
		private final RawValue key_machine_name = ValueFactory.createRawValue("machine_name".getBytes());

		@Override
		public void read(Value extra_info) {
			machineName = extra_info.asMapValue().get(key_machine_name).asRawValue().getString();
		}
	};
		

	static int deviceCounter = 0;
	
	protected void onProgressUpdate(Integer... progress) {
        //TODO: update bar...
    }

    protected void onPostExecute(DeviceItem result) {
    	Logger.printLog("initialPcConnection","onPostExecute");
    	if(result != null)
    	{
    		mainActivity.addDevice(result);
    	}else{
    		deviceCounter++;
        	mainActivity.addDevice(new DeviceItem(23,null,"device"));
    	}
   	
    	mainActivity.stopProgressBar();  	
    }
	
	
}
