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

	private Socket socket;
	private BlockingDeque<Integer> queue;
	private MessagePack msgpack;
	private Map<String, Integer> message;

	/**
	 * Constctur:
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public HardwareListener(DeviceItem device) throws SocketException {
		super();
		this.socket = device.getControlSocket();
		this.queue = device.getClickQueue();
		this.msgpack = new MessagePack();
		this.message = new LinkedHashMap<String, Integer>(1);
	}

	protected void sendSample() throws IOException {
		// GyroSample sample = this.queue.poll(500, TimeUnit.MILLISECONDS);
		if (!this.queue.isEmpty()) {
			Integer click = this.queue.removeFirst();
			this.message.put("click", click);
			byte[] buffer = msgpack.write(this.message);
			this.socket.getOutputStream().write(buffer);
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
