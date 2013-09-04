package Threads;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.msgpack.MessagePack;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;

import android.os.AsyncTask;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.Params;
import com.example.gesturemouseclient.infra.interfaces.ApplicationListener;

public class ApplicationListenerThread extends AsyncTask<Void, String, Void> {

	private static final RawValue key_app = ValueFactory.createRawValue("app".getBytes());

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
	public ApplicationListenerThread(DeviceItem remoteDeviceInfo, ApplicationListener applicationListener) {
		super();
		this.applicationListener = applicationListener;
		try {
			tcpServer = new ServerSocket(Integer.parseInt(Params.TCP_IN_GOING_PORT));
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
		while (!isCancelled()) {
			try {
				tcpServer.setSoTimeout(1000);
				Socket socket = tcpServer.accept();

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
				String appName = returnMsg.get(key_app).asRawValue().getString();

				publishProgress(appName);
				socket.close();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		applicationListener.onApplicationChanged(values[0]);
		super.onProgressUpdate(values);
	}

}
