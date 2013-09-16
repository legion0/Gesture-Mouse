package Threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.msgpack.MessagePack;

import com.example.gesturemouseclient.infra.RemoteDeviceInfo;

public class FastSampleSenderThread extends PausableThread {

	private BlockingDeque<float[]> outgoingSampleQueue;
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
		try {
			float[] sample = outgoingSampleQueue.pollFirst();
			if (sample != null) {
//				Logger.printLog("FastSensorConnection : ", "sendSample(" + sample[0] + "," + sample[1] + "," + sample[2] + ")");
				byte[] buffer = msgpack.write(sample);

				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, remoteDeviceInfo.getAddress(), remoteDeviceInfo.getUDPPort());
				DatagramSocket socket = new DatagramSocket();
				socket.send(packet);
				socket.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
