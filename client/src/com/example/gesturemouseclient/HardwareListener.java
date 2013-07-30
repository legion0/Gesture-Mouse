package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.msgpack.MessagePack;

import com.example.gesturemouseclient.infra.DeviceItem;

public class HardwareListener extends PausableThread {

	private BlockingDeque<Integer> queue;
	private MessagePack msgpack;
	private Map<String, Object> message;
	private final DeviceItem device;

	/**
	 * Constctur:
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public HardwareListener(DeviceItem device) throws SocketException {
		super();
		this.device = device;
		this.queue = device.getClickQueue();
		this.msgpack = new MessagePack();
		this.message = new LinkedHashMap<String, Object>(1);
		this.message.put("session_id", device.getSessionId());
	}

	protected void sendSample() throws IOException {
		// GyroSample sample = this.queue.poll(500, TimeUnit.MILLISECONDS);
		if (!this.queue.isEmpty()) {
			Socket socket = new Socket(device.getAddress(),device.getControlPort());
			Integer click = this.queue.removeFirst();
			this.message.put("click", click);
			byte[] buffer = msgpack.write(this.message);
			socket.getOutputStream().write(buffer);
		}
	}

	@Override
	protected void innerAction() {
		try {
			sendSample();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
