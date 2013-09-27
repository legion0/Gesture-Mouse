package Threads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.Arrays;

import org.msgpack.MessagePack;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;

import android.os.AsyncTask;
import android.util.Log;

import com.example.gesturemouseclient.dal.ApplicationDAL;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;
import com.example.gesturemouseclient.infra.interfaces.ApplicationListener;

public class ApplicationListenerTask extends AsyncTask<Void, ApplicationDAL, Void> {

	private static final RawValue key_app_id = ValueFactory.createRawValue("app_id".getBytes());
	private static final RawValue key_window_title = ValueFactory.createRawValue("window_title".getBytes());
	private static final RawValue key_process_name = ValueFactory.createRawValue("process_name".getBytes());

	private ServerSocket tcpServer;

	private ApplicationListener applicationListener;
	private final RemoteDeviceInfo remoteDeviceInfo;

	/**
	 * Constctur:
	 * 
	 * @param mainActivity
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public ApplicationListenerTask(RemoteDeviceInfo remoteDeviceInfo, ApplicationListener applicationListener) throws InvalidParameterException {
		super();
		this.remoteDeviceInfo = remoteDeviceInfo;
		this.applicationListener = applicationListener;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Logger.printLog("Application Listener", "" + isCancelled());
		
		
		while (!isCancelled()) {
			try {
				Log.d("Application Listener", "binding socket");
				tcpServer = new ServerSocket();
				tcpServer.setReuseAddress(true);
				tcpServer.bind(new InetSocketAddress(remoteDeviceInfo.getLocalControlPort()));
				break;
			} catch (IOException e) {
				Log.e("ApplicationListenerTask", "Failed to bind port", e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					Log.e("ApplicationListenerTask", "SLEEP INTERRUPT !!!", e);
				}
			}
		}
		
		while (!isCancelled()) {
			Socket socket = null;
			try {
				tcpServer.setSoTimeout(1000);
				try {
					socket = tcpServer.accept();
				} catch (SocketTimeoutException e) {
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

				Log.d("Application Listener", "return msg: " + returnMsg.toString());

				ApplicationDAL appData = new ApplicationDAL(null, null, null);

				if (returnMsg.containsKey(key_app_id)) {
					appData.setId(returnMsg.get(key_app_id).asIntegerValue().getInt());
				} else {
					byte[] bytes = returnMsg.get(key_window_title).asRawValue().getByteArray();
					Log.d("ApplicationListenerTask", "window_title bytes = " + Arrays.toString(bytes));
					String windowTitle = new String(bytes, Charset.forName("UTF-8"));
					appData.setWindowTitle(windowTitle);
					Log.d("ApplicationListenerTask", "window_title = " + windowTitle);
					appData.setProcessName(new String(returnMsg.get(key_process_name).asRawValue().getByteArray(), Charset.forName("UTF-8")));
				}
				publishProgress(appData);
			} catch (SocketException ex) {
				Log.e("ApplicationListenerTask", "doInBackground", ex);
			} catch (IOException ex) {
				Log.e("ApplicationListenerTask", "doInBackground", ex);
			} finally {
				if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		try {
			tcpServer.close();
			Log.d("Application Listener", " closed socket.");
		} catch (IOException e) {
		}
		Log.d("Application Listener", " closing");
		return null;
	}

	@Override
	protected void onProgressUpdate(ApplicationDAL... values) {
		applicationListener.onApplicationChanged(values[0]);
		super.onProgressUpdate(values);
	}
}
