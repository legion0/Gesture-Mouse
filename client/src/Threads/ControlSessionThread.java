package Threads;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.msgpack.MessagePack;

import com.example.gesturemouseclient.PausableThread;
import com.example.gesturemouseclient.infra.RemoteDeviceInfo;

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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		msg.remove("key_event");
		if (buffer != null) {
			outgoingControlMessages.offerLast(buffer);
		}
	}

	@Override
	protected void innerAction() {
		byte[] buffer = outgoingControlMessages.pollFirst();
		if (buffer != null) {
			try {
				Socket socket = new Socket(remoteDeviceInfo.getAddress(), remoteDeviceInfo.getControlPort());
				OutputStream outputStream = socket.getOutputStream();
				outputStream.write(buffer);
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
