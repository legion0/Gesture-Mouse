package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;

import org.msgpack.MessagePack;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.GyroSample;

public class FastSensorConnection extends PausableThread {

	private DatagramSocket socket;
	private BlockingDeque<GyroSample> queue;
	private MessagePack msgpack;
	private ArrayList<Float> message;

	/**
	 * Constctur:
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public FastSensorConnection(DeviceItem device) throws SocketException {
		super();
		this.socket = new DatagramSocket(device.getUDPPort(),
				device.getAddress());
		this.queue = device.getGyroQueue();
		this.msgpack = new MessagePack();
		this.message = new ArrayList<Float>(3);
		this.message.add(0.0f);
		this.message.add(0.0f);
		this.message.add(0.0f);
	}

	protected void sendSample() throws IOException {
		// GyroSample sample = this.queue.poll(500, TimeUnit.MILLISECONDS);
		if (!this.queue.isEmpty()) {
			GyroSample sample = this.queue.removeFirst();
			this.message.set(0, sample.getX());
			this.message.set(1, sample.getY());
			this.message.set(2, sample.getZ());
			byte[] buffer = msgpack.write(this.message);
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			this.socket.send(packet);
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
