package Threads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.msgpack.MessagePack;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;

import android.os.AsyncTask;
import android.util.Log;

import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.interfaces.ApplicationListener;

public class ApplicationListenerThread extends AsyncTask<Void, ApplicationDAL, Void> {

	private static final RawValue key_app_id = ValueFactory.createRawValue("app_id".getBytes());
	private static final RawValue key_window_title = ValueFactory.createRawValue("window_title".getBytes());
	private static final RawValue key_process_name = ValueFactory.createRawValue("process_name".getBytes());

	private ServerSocket tcpServer;

	private ApplicationListener applicationListener;

	/**
	 * Constctur:
	 * 
	 * @param mainActivity
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public ApplicationListenerThread(RemoteDeviceInfo remoteDeviceInfo, ApplicationListener applicationListener) {
		super();
		this.applicationListener = applicationListener;
		try {
			tcpServer = new ServerSocket(Params.TCP_IN_GOING_PORT);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		Logger.printLog("Application Listener", ""+isCancelled());
		while (!isCancelled()) {
			try {
				tcpServer.setSoTimeout(1000);
				Socket socket;
				try{
				 socket = tcpServer.accept();
				}catch (SocketTimeoutException e) {
					continue;
				}
				byte[] bufInput = new byte[4096];
				socket.setSoTimeout(1000);
				InputStream inputStream = socket.getInputStream();
				int bytesRead = inputStream.read(bufInput);

				byte[] tempBuffer = new byte[bytesRead];
				System.arraycopy(bufInput, 0, tempBuffer, 0, bytesRead);
				bufInput = tempBuffer;

				ByteArrayInputStream in = new ByteArrayInputStream(bufInput);
				MessagePack msgpack = new MessagePack();
				Unpacker unpacker = msgpack.createUnpacker(in);
				MapValue returnMsg = unpacker.readValue().asMapValue();
				
				Log.d("Application Listener","return msg: "+returnMsg.toString());
				
				ApplicationDAL appData = new ApplicationDAL(null, null, null);
				
				
				
				if(returnMsg.containsKey(key_app_id))
				{
					appData.setId(returnMsg.get(key_app_id).asIntegerValue().getInt());
				}else{
					appData.setProcessName(returnMsg.get(key_process_name).asRawValue().getString());
					appData.setWindowTitle(returnMsg.get(key_window_title).asRawValue().getString());
				}
				publishProgress(appData);
				socket.close();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			tcpServer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(ApplicationDAL... values) {
		applicationListener.onApplicationChanged(values[0]);
		super.onProgressUpdate(values);
	}

}
