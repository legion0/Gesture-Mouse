package us.to.gesturemouse.threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.msgpack.MessagePack;

import us.to.gesturemouse.infra.RemoteDeviceInfo;

import android.util.Log;


public class FastSampleSenderThread extends PausableThread {

	private BlockingDeque<float[]> outgoingSampleQueue;
	private static int MAX_MEMORY = 10;
	private MessagePack msgpack;
	private final RemoteDeviceInfo remoteDeviceInfo;

	/**
	 * Constctur:
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public FastSampleSenderThread(RemoteDeviceInfo remoteDeviceInfo) {
		super();
		this.remoteDeviceInfo = remoteDeviceInfo;
		outgoingSampleQueue = new LinkedBlockingDeque<float[]>();
		msgpack = new MessagePack();
	}

	public void sendSample(float[] sample) {
		outgoingSampleQueue.offerLast(sample);
	}

	@Override
	protected void innerAction() {
		while (outgoingSampleQueue.size() > MAX_MEMORY) {
			outgoingSampleQueue.pollFirst();
		}
		float[] sample = null;
		try {
			sample = outgoingSampleQueue.pollFirst(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Log.e("FastSampleSenderThread", "SLEEP INTERRUPT !!!", e);
		}
		if (sample != null) {
			try {
				if (sample != null) {
					// Logger.printLog("FastSensorConnection : ", "sendSample(" + sample[0] + "," + sample[1] + "," + sample[2] + ")");
					byte[] buffer = msgpack.write(sample);

					DatagramPacket packet = new DatagramPacket(buffer, buffer.length, remoteDeviceInfo.getAddress(), remoteDeviceInfo.getUDPPort());
					DatagramSocket socket = new DatagramSocket();
					socket.send(packet);
					socket.close();
				}
			} catch (SocketException ex) {
			} catch (IOException ex) {
			}
		}
	}

}
