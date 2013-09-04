package com.example.gesturemouseclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;

import com.example.gesturemouseclient.infra.DeviceItem;
import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.ResponseReader;

public class TcpClient {

	// private ResponseReader responseReader = null;
	private long timeout = -1;

	private static final RawValue key_features = ValueFactory.createRawValue("features".getBytes());
	private static final RawValue key_port = ValueFactory.createRawValue("port".getBytes());
	private static final RawValue key_name = ValueFactory.createRawValue("name".getBytes());
	private static final RawValue key_gid = ValueFactory.createRawValue("gid".getBytes());
	private static final RawValue key_apps = ValueFactory.createRawValue("apps".getBytes());
	private static final RawValue key_actions = ValueFactory.createRawValue("actions".getBytes());
	private static final RawValue key_gestures = ValueFactory.createRawValue("gestures".getBytes());
	private static final RawValue key_udp = ValueFactory.createRawValue("udp".getBytes());
	private static final RawValue key_session_id = ValueFactory.createRawValue("session_id".getBytes());
	private final int tcp_outgoing_port;
	private final String deviceName;
	private final InetAddress address;
	private Socket socket;

	/**
	 * Constructor:
	 * 
	 * @param responseReader
	 * @param deviceName
	 * @param tcp_outgoing_port
	 * @param address
	 * @param tcp_port
	 */
	public TcpClient(ResponseReader responseReader, int tcp_outgoing_port, String deviceName, InetAddress address) {
		// this.responseReader = responseReader;
		this.tcp_outgoing_port = tcp_outgoing_port;
		this.deviceName = deviceName;
		this.address = address;
	}

	public void setTimeout(int seconds) {
		timeout = seconds * 1000000000l;
	}

	private byte[] createMsg(final String tcpPort, String[] features) throws IOException {
		Map<Object, Object> msg = new LinkedHashMap<Object, Object>() {
			{
				put(key_name, deviceName);
				put(key_port, tcpPort);
				List<Object> apps = new ArrayList<Object>() {
					{
						Map<Object, Object> app = new LinkedHashMap<Object, Object>() {
							{
								put(key_name, "browser");
								Map<Object, Object> gestures = new LinkedHashMap<Object, Object>() {
									{
										put(key_name, "flick left");
										put(key_gid, "134");
										List<Object> actions = new ArrayList<Object>() {
											{
												add("17");
											}
										};
										put(key_actions, actions);
									}
								};
								put(key_gestures, gestures);
							}
						};
						add(app);
					}
				};
				put(key_apps, apps);
			}
		};
		if (features != null && features.length > 0) {
			msg.put(key_features, features);
		}
		MessagePack msgpack = new MessagePack();
		return msgpack.write(msg);
	}

	public void initControllSession(final String tcpPort, String[] features, DeviceItem device) throws IOException {
		byte[] msgBuffer = createMsg(tcpPort, features);

		Logger.printLog("TCP Client", "C: Connecting...");

		// create a socket to make the connection with the server
		socket = new Socket(address, tcp_outgoing_port);

		try {

			// send the message to the server
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(msgBuffer);
			Logger.printLog("TCP Client", "C: Sent.");

			// receive the message which the server sends back
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
			int udpFromServer = returnMsg.get(key_udp).asIntegerValue().getInt();
			String sessionId = returnMsg.get(key_session_id).asRawValue().getString();
			if (udpFromServer <= 0) {
				throw new MessageTypeException("Invalid Udp.");
			}
			if (sessionId == null) {
				throw new MessageTypeException("Invalid session id.");
			}
			device.setSessionId(sessionId);
			device.setUDPPort(udpFromServer);

			Logger.printLog("TCP Client", "S: Received udp port: " + udpFromServer);
		} catch (Exception e) {
			Logger.printLog("TCPClient", "S: Error, " + e.getMessage());
		}

	}

	public Socket getSocket() {
		return socket;
	}

}
