package us.to.gesturemouse.threads;

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

import us.to.gesturemouse.dal.ApplicationDAL;
import us.to.gesturemouse.infra.RemoteDeviceInfo;
import us.to.gesturemouse.infra.Tools;
import us.to.gesturemouse.infra.interfaces.ApplicationListener;
import android.os.AsyncTask;
import android.util.Log;


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

	private void sleepOrDie() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Log.e("ApplicationListenerTask", "SLEEP INTERRUPT !!!", e);
			cancel(false);
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
//		Logger.printLog("Application Listener", "" + isCancelled());
		
		
		while (!isCancelled()) {
			if (remoteDeviceInfo.getLocalControlPort() > 0) {
				try {
					Log.d("Application Listener", "binding socket");
					tcpServer = new ServerSocket();
					tcpServer.setReuseAddress(true);
					tcpServer.bind(new InetSocketAddress(remoteDeviceInfo.getLocalControlPort()));
					break;
				} catch (IOException e) {
					Log.e("ApplicationListenerTask", "Failed to bind port, waiting 1 second and trying again.", e);
					sleepOrDie();
				}
			} else {
				Log.e("ApplicationListenerTask", "No port yet, waiting 1 second to check again");
				sleepOrDie();
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
				Tools.closeSocket(socket);
			}
		}
		Tools.closeSocket(tcpServer);
		Log.d("Application Listener", " closed");
		return null;
	}

	@Override
	protected void onProgressUpdate(ApplicationDAL... values) {
		applicationListener.onApplicationChanged(values[0]);
		super.onProgressUpdate(values);
	}
}
