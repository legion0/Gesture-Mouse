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

import us.to.gesturemouse.infra.RemoteDeviceInfo;

import android.util.Log;


public class ControlSessionThread extends PausableThread {

	private RemoteDeviceInfo remoteDeviceInfo;
	private final BlockingDeque<byte[]> outgoingControlMessages;
	private MessagePack msgpack;
	private Map<String, Object> msg;

	public ControlSessionThread(RemoteDeviceInfo remoteDeviceInfo) {
		super();
		this.remoteDeviceInfo = remoteDeviceInfo;
		outgoingControlMessages = new LinkedBlockingDeque<byte[]>();
		msgpack = new MessagePack();
		msg = new LinkedHashMap<String, Object>();
		msg.put("session_id", remoteDeviceInfo.getSessionId());
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

}
