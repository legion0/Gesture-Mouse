package us.to.gesturemouse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;

import us.to.gesturemouse.dal.ApplicationDAL;
import us.to.gesturemouse.dal.GestureDAL;
import us.to.gesturemouse.infra.RemoteDeviceInfo;
import us.to.gesturemouse.infra.Tools;
import android.content.Context;
import android.util.Log;


public class TcpClient {

	// private ResponseReader responseReader = null;
	private int timeout = 3;

	private static final RawValue key_port = ValueFactory.createRawValue("port".getBytes());
	private static final RawValue key_name = ValueFactory.createRawValue("name".getBytes());
	private static final RawValue key_process_name = ValueFactory.createRawValue("process_name".getBytes());
	private static final RawValue key_window_title = ValueFactory.createRawValue("window_title".getBytes());
	private static final RawValue key_id = ValueFactory.createRawValue("id".getBytes());
	private static final RawValue key_apps = ValueFactory.createRawValue("apps".getBytes());
	private static final RawValue key_action = ValueFactory.createRawValue("action".getBytes());
	private static final RawValue key_gestures = ValueFactory.createRawValue("gestures".getBytes());
	private static final RawValue key_udp = ValueFactory.createRawValue("udp".getBytes());
	private static final RawValue key_session_id = ValueFactory.createRawValue("session_id".getBytes());
	private static final RawValue key_close = ValueFactory.createRawValue("close".getBytes());
	private Context applicationContext;

	private RemoteDeviceInfo remoteDevice;

	/**
	 * Constructor:
	 * 
	 * @param responseReader
	 * @param deviceName
	 * @param tcp_outgoing_port
	 * @param address
	 * @param tcp_port
	 */
	public TcpClient(RemoteDeviceInfo remoteDevice, Context applicationContext) {
		this.remoteDevice = remoteDevice;
		this.applicationContext = applicationContext;
	}

	public void setTimeout(int seconds) {
		timeout = seconds * 1000;
	}

	private byte[] createMsg(int localControlPort) throws IOException {
		Map<Object, Object> msg = new LinkedHashMap<Object, Object>();
		String localHostname = android.os.Build.MODEL + android.os.Build.USER;
		msg.put(key_name, localHostname); // TODO: self name not remote name
		msg.put(key_port, localControlPort);
		List<Object> appsMsg = new ArrayList<Object>();
		Set<ApplicationDAL> applications = ApplicationDAL.loadWithGestures(applicationContext);
		Iterator<ApplicationDAL> appIter = applications.iterator();
		while (appIter.hasNext()) {
			ApplicationDAL app = appIter.next();
			Map<Object, Object> appMsg = new LinkedHashMap<Object, Object>();
			appMsg.put(key_id, app.getId());
			appMsg.put(key_name, app.getName());
			appMsg.put(key_process_name, app.getProcessName());
			appMsg.put(key_window_title, app.getWindowTitle());
			List<Object> gesturesMsg = new ArrayList<Object>();
			Set<GestureDAL> gestures = app.getGestures();
			Iterator<GestureDAL> gestIter = gestures.iterator();
			while (gestIter.hasNext()) {
				Map<Object, Object> gestureMsg = new LinkedHashMap<Object, Object>();
				GestureDAL gesture = gestIter.next();
				gestureMsg.put(key_id, gesture.getId());
				gestureMsg.put(key_name, gesture.getName());
				List<Object> actionMsg = new ArrayList<Object>();
				int[] action = gesture.getAction();
				for (int keyCode : action) {
					actionMsg.add(keyCode);
				}
				gestureMsg.put(key_action, actionMsg);
				gesturesMsg.add(gestureMsg);
			}
			appMsg.put(key_gestures, gesturesMsg);
			appsMsg.add(appMsg);
		}
		msg.put(key_apps, appsMsg);
		MessagePack msgpack = new MessagePack();
		return msgpack.write(msg);
	}

	private int findNewLocalControlPort()
	{
		int port = -1;
		ServerSocket tcpServer = null;
		try {
			tcpServer = new ServerSocket();
			tcpServer.bind(null);
			port = tcpServer.getLocalPort();
		} catch (IOException e) {
			Log.e("TCPClient", "findNewLocalControlPort", e);
		} finally {
			Tools.closeSocket(tcpServer);
		}
		Log.d("TcpClient","tcp port found: "+port);
		return port;
	}

	public void initControllSession(String[] features, RemoteDeviceInfo device) throws IOException {	
		
		int localControlPort = findNewLocalControlPort();
		device.setLocalControlPort(localControlPort);
		
		byte[] msgBuffer = createMsg(localControlPort);
		
		

//		Logger.printLog("TCP Client", "C: Connecting...");

		// create a socket to make the connection with the server
		Socket socket = new Socket(remoteDevice.getAddress(), remoteDevice.getControlPort());

		try {

			// send the message to the server
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(msgBuffer);
//			Logger.printLog("TCP Client", "C: Sent.");

			// receive the message which the server sends back
			byte[] bufInput = new byte[4096];
			DatagramPacket incomingPacket = new DatagramPacket(bufInput, bufInput.length);
			socket.setSoTimeout(timeout);
			InputStream inputStream = socket.getInputStream();
			inputStream.read(bufInput);

			byte[] tempBuffer = new byte[incomingPacket.getLength()];
			System.arraycopy(bufInput, 0, tempBuffer, 0, incomingPacket.getLength());
			bufInput = tempBuffer;
			ByteArrayInputStream in = new ByteArrayInputStream(bufInput);
			MessagePack msgpack = new MessagePack();
			Unpacker unpacker = msgpack.createUnpacker(in);
			MapValue returnMsg = unpacker.readValue().asMapValue();
			int udpFromServer = returnMsg.get(key_udp).asIntegerValue().getInt();
			String sessionId = returnMsg.get(key_session_id).asRawValue().getString();
			if (udpFromServer <= 0) {
				throw new MessageTypeException("Invalid Udp.");
			}
			if (sessionId == null) {
				throw new MessageTypeException("Invalid session id.");
			}
			device.setSessionId(sessionId);
			device.setUDPPort(udpFromServer);

//			Logger.printLog("TCP Client", "S: Received udp port: " + udpFromServer);
		} catch (IOException e) {
			Log.e("TCPClient", "initControllSession", e);
		} catch (NullPointerException e) {
			Log.e("TCPClient", "initControllSession", e);
		} catch (MessageTypeException e) {
			Log.e("TCPClient", "initControllSession", e);
		} finally {
			Tools.closeSocket(socket);
		}

	}

	

	public void closeSession(Object object, RemoteDeviceInfo remoteDevice2) throws IOException {
		Map<Object, Object> msg = new LinkedHashMap<Object, Object>();

		msg.put(key_close, "temporary close"); 
		msg.put(key_session_id, remoteDevice.getSessionId());
		MessagePack msgpack = new MessagePack();
		byte[] msgBuffer = msgpack.write(msg);

//		Logger.printLog("TCP Client", "C: Connecting...");

		// create a socket to make the connection with the server
		Socket socket = new Socket(remoteDevice.getAddress(), remoteDevice.getControlPort());

		try {

			// send the message to the server
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(msgBuffer);
//			Logger.printLog("TCP Client", "C: close.");

		} catch (IOException e) {
			Log.e("TCPClient", "closeSession", e);
		} catch (NullPointerException e) {
			Log.e("TCPClient", "closeSession", e);
		} catch (MessageTypeException e) {
			Log.e("TCPClient", "closeSession", e);
		} finally {
			Tools.closeSocket(socket);
		}
	}
}
