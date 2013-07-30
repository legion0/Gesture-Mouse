package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.msgpack.MessagePack;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.GyroSample;

public class GestureListener extends PausableThread {

	private static final int HISTORY_SIZE = 100;
	private BlockingDeque<GyroSample> queue;
	private MessagePack msgpack;
	private Map<String, Object> message;

	/**
	 * Constctur:
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public GestureListener(DeviceItem device) throws SocketException {
		super();
		this.queue = device.getGestureQueue();
		this.msgpack = new MessagePack();
		this.message = new LinkedHashMap<String, Object>(1);
		this.message.put("session_id", device.getSessionId());
	}

	protected void detectGesture() throws IOException {
		while (this.queue.size() > HISTORY_SIZE) {
			this.queue.removeFirst();
		}
		//GyroSample sample = this.queue.pop();
		//this.message.put("gesture", click);
		//byte[] buffer = msgpack.write(this.message);
		//this.socket.getOutputStream().write(buffer);
	}

	@Override
	protected void innerAction() {
		try {
			detectGesture();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
