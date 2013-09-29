package us.to.gesturemouse.threads;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.msgpack.MessagePack;

import us.to.gesturemouse.TcpClient;
import us.to.gesturemouse.infra.RemoteDeviceInfo;
import us.to.gesturemouse.infra.interfaces.ApplicationListener;

import android.app.Activity;
import android.content.Context;
import android.util.Log;


public class ControlSessionThread extends PausableRunnable {
	
	public static enum ConnectionState {
		CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
	}

	private RemoteDeviceInfo remoteDeviceInfo;
	private final BlockingDeque<byte[]> outgoingControlMessages;
	private MessagePack msgpack;
	private Map<String, Object> msg;
	private boolean shouldConnect;
	private boolean shouldDisconnect;
	private Context context;
	private Runnable runOnConnect;
	private ConnectionState connectionState;
	private Activity activity;
	private Thread thread;

	public ControlSessionThread(RemoteDeviceInfo remoteDeviceInfo, Context context, Activity activity, Runnable runOnConnect) {
		super();
		this.activity = activity;
		connectionState =  ConnectionState.DISCONNECTED;
		this.remoteDeviceInfo = remoteDeviceInfo;
		this.context = context;
		this.runOnConnect = runOnConnect;
		outgoingControlMessages = new LinkedBlockingDeque<byte[]>();
		msgpack = new MessagePack();
		msg = new LinkedHashMap<String, Object>();
		msg.put("session_id", remoteDeviceInfo.getSessionId());
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public void sendGesture(int gestureId) {
		msg.put("gesture", gestureId);
		byte[] buffer = null;
		try {
			buffer = msgpack.write(msg);
		} catch (IOException e) {
			Log.e("ControlSessionThread", "sendGesture: Failed to encode msg: " + msg.toString(), e);
		}
		msg.remove("gesture");
		if (buffer != null) {
			outgoingControlMessages.offerLast(buffer);
		}
	}

	public void sendKey(int keyId) {
		msg.put("key_event", keyId);
		byte[] buffer = null;
		try {
			buffer = msgpack.write(msg);
		} catch (IOException e) {
			Log.e("ControlSessionThread", "sendKey: Failed to encode msg: " + msg.toString(), e);
		}
		msg.remove("key_event");
		if (buffer != null) {
			outgoingControlMessages.offerLast(buffer);
		}
	}

	public void sendKeys(int[] keyIds) {
		msg.put("key_event", keyIds);
		byte[] buffer = null;
		try {
			buffer = msgpack.write(msg);
		} catch (IOException e) {
			Log.e("ControlSessionThread", "sendKeys: Failed to encode msg: " + msg.toString(), e);
		}
		msg.remove("key_event");
		if (buffer != null) {
			outgoingControlMessages.offerLast(buffer);
		}
	}

	@Override
	protected void innerAction() {
		doConnect();
		doSendMsg();
		doDisconnect();
	}

	private void doDisconnect() {
		try {
			if (getConnectionState() == ConnectionState.DISCONNECTING) {
				TcpClient client = new TcpClient(remoteDeviceInfo, context);
				client.closeSession();
			}
			if (getConnectionState() == ConnectionState.DISCONNECTING) {
				setConnectionState(ConnectionState.DISCONNECTED);
			}
		} catch (IOException e) {
			Log.e("ControlSessionThread", "doConnect", e);
		}
	}

	private void doConnect() {
		try {
			if (getConnectionState() == ConnectionState.CONNECTING) {
				TcpClient client = new TcpClient(remoteDeviceInfo, context);
				client.initControllSession();
			}
			if (getConnectionState() == ConnectionState.CONNECTING) {
				setConnectionState(ConnectionState.CONNECTED);
				if (activity != null && runOnConnect != null) {
					activity.runOnUiThread(runOnConnect);
				}
			}
		} catch (IOException e) {
			Log.e("ControlSessionThread", "doConnect", e);
		}
	}

	public synchronized ConnectionState getConnectionState() {
		return connectionState;
	}

	private synchronized void setConnectionState(ConnectionState connectionState) {
		this.connectionState = connectionState;
	}

	private void doSendMsg() {
		byte[] buffer = null;
		try {
			buffer = outgoingControlMessages.pollFirst(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Log.e("ControlSessionThread", "SLEEP INTERRUPT !!!", e);
		}
		if (buffer != null) {
			try {
				Socket socket = new Socket(remoteDeviceInfo.getAddress(), remoteDeviceInfo.getControlPort());
				OutputStream outputStream = socket.getOutputStream();
				outputStream.write(buffer);
				socket.close();
			} catch (IOException e) {
				Log.e("ControlSessionThread", "Exception in: innerAction", e);
			}
		}
	}

	public synchronized void connect() {
		if (connectionState != ConnectionState.CONNECTED && connectionState != ConnectionState.CONNECTING) {
			connectionState = ConnectionState.CONNECTING;
			outgoingControlMessages.clear();
		}
	}

	public synchronized void disconnect() {
		if (connectionState != ConnectionState.DISCONNECTED && connectionState != ConnectionState.DISCONNECTING) {
			connectionState = ConnectionState.DISCONNECTING;
			outgoingControlMessages.clear();
		}
	}

}
