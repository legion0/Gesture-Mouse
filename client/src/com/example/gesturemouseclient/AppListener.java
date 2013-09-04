package com.example.gesturemouseclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.Params;

public class AppListener extends PausableThread {

	private BlockingDeque<Integer> queue;
	private MessagePack msgpack;
	private Map<String, Object> message;
	private final DeviceItem device;
	
	private static final RawValue key_app = ValueFactory.createRawValue("app".getBytes());
	private final MainActivity mainActivity;
	

	/**
	 * Constctur:
	 * @param mainActivity 
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException
	 */
	public AppListener(DeviceItem device, MainActivity mainActivity) throws SocketException {
		super();
		this.device = device;
		this.mainActivity = mainActivity;
		this.queue = device.getClickQueue();
		this.msgpack = new MessagePack();
		this.message = new LinkedHashMap<String, Object>(1);
		this.message.put("session_id", device.getSessionId());
		
	}

	protected void findRunningPcApp() throws IOException {
		
		ServerSocket tcpServer = new ServerSocket(Integer.parseInt(Params.TCP_IN_GOING_PORT));
		Socket socket = tcpServer.accept();
		
		byte[] bufInput = new byte[4096];
		DatagramPacket incomingPacket = new DatagramPacket(bufInput, bufInput.length);
		socket.setSoTimeout(3000);
		InputStream inputStream = socket.getInputStream();
		inputStream.read(bufInput);
		
		byte[] tempBuffer = new byte[incomingPacket.getLength()];
		System.arraycopy(bufInput, 0, tempBuffer, 0, incomingPacket.getLength());
		bufInput = tempBuffer;
		ByteArrayInputStream in = new ByteArrayInputStream(bufInput);
		MessagePack msgpack = new MessagePack();
		Unpacker unpacker = msgpack.createUnpacker(in);
		MapValue returnMsg = unpacker.readValue().asMapValue();
		String appName = returnMsg.get(key_app).asRawValue().getString();
		
		mainActivity.setRunningApp(appName);
		
		socket.close();
		tcpServer.close();
	}

	@Override
	protected void innerAction() {
		try {
			findRunningPcApp();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	

}
