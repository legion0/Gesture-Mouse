package com.example.gesturemouseclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.type.MapValue;
import org.msgpack.type.RawValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;

public class Client {

	private long timeout = -1;
	private int ttl = 3;
	private static final String MULTICAST_GROUP = "224.0.0.1";
	private static final int NSD_PORT = 33333;
	private static final RawValue key_extra_info = ValueFactory.createRawValue("extra_info".getBytes());
	private static final RawValue key_service = ValueFactory.createRawValue("service".getBytes());
	private static final RawValue key_features = ValueFactory.createRawValue("features".getBytes());
	private static final RawValue key_port = ValueFactory.createRawValue("port".getBytes());

	public Client() {
	}

	public Result findFirst(String serviceName) throws IOException {
		return findFirst(serviceName, null);
	}

	public Result[] findAll(String serviceName) throws IOException {
		return findAll(serviceName, null);
	}

	public void setTimeout(int seconds) {
		timeout = seconds * 1000000000l;
	}

	public class Result {
		public InetSocketAddress inetSocketAddress;
		public Value extraInfo;

		public Result(InetSocketAddress inetSocketAddress, Value extraInfo) {
			super();
			this.inetSocketAddress = inetSocketAddress;
			this.extraInfo = extraInfo;
		}
	}

	public Result[] findAll(final String serviceName, String[] features) throws IOException {
		Map<Object, Object> msg = new LinkedHashMap<Object, Object>();
		msg.put(key_service, serviceName);
		if (features != null && features.length > 0) {
			msg.put(key_features, features);
		}
		MessagePack msgpack = new MessagePack();
		byte[] msgBuffer = msgpack.write(msg);
		InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
		MulticastSocket socket = new MulticastSocket();
		socket.setTimeToLive(ttl);
		DatagramPacket outgoingPacket = new DatagramPacket(msgBuffer, msgBuffer.length, group, NSD_PORT);
		DatagramPacket incomingPacket;

		byte[] buf;
		long end = timeout > -1 ? System.nanoTime() + timeout : Long.MAX_VALUE;
		List<Result> servers = new ArrayList<Result>();
		try {
			while (System.nanoTime() < end) {
				try {
					socket.send(outgoingPacket);
					buf = new byte[4096];
					incomingPacket = new DatagramPacket(buf, buf.length);
					socket.setSoTimeout(1000);
					socket.receive(incomingPacket);
					byte[] tempBuffer = new byte[incomingPacket.getLength()];
					System.arraycopy(buf, 0, tempBuffer, 0, incomingPacket.getLength());
					buf = tempBuffer;
					ByteArrayInputStream in = new ByteArrayInputStream(buf);
					Unpacker unpacker = msgpack.createUnpacker(in);
					MapValue returnMsg = unpacker.readValue().asMapValue();
					String returnServiceName = returnMsg.get(key_service).asRawValue().getString();
					if (serviceName.equals(returnServiceName)) {
						int port = returnMsg.get(key_port).asIntegerValue().getInt();
						InetSocketAddress inetSocketAddress = new InetSocketAddress(incomingPacket.getAddress(), port);
						boolean ignore = false;
						for (Result server : servers) {
							if (server.inetSocketAddress.equals(inetSocketAddress)) {
								ignore = true;
								break;
							}
						}
						if (!ignore) {
							Result result = new Result(inetSocketAddress, null);
							if (returnMsg.containsKey(key_extra_info)) {
								result.extraInfo = returnMsg.get(key_extra_info);
							}
							servers.add(result);
						}
					}
				} catch (SocketTimeoutException ex) {
					// System.out.println("timeout");
				} catch (NullPointerException ex) {
					throw new RuntimeException(ex);
				} catch (MessageTypeException ex) {
					throw new RuntimeException(ex);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		} finally {
			socket.close();
		}
		Result[] res_arr = new Result[servers.size()];
		servers.toArray(res_arr);
		return res_arr;
	}

	public Result findFirst(final String serviceName, String[] features) throws IOException {
		@SuppressWarnings("serial")
		Map<Object, Object> msg = new LinkedHashMap<Object, Object>() {
			{
				put(key_service, serviceName);
			}
		};
		if (features != null && features.length > 0) {
			msg.put(key_features, features);
		}
		MessagePack msgpack = new MessagePack();
		byte[] msgBuffer = msgpack.write(msg);
		InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
		MulticastSocket socket = new MulticastSocket();
		socket.setTimeToLive(ttl);
		DatagramPacket outgoingPacket = new DatagramPacket(msgBuffer, msgBuffer.length, group, NSD_PORT);
		DatagramPacket incomingPacket;

		byte[] buf;
		long end = timeout > -1 ? System.nanoTime() + timeout : Long.MAX_VALUE;
		try {
			while (System.nanoTime() < end) {
				try {
					socket.send(outgoingPacket);

					buf = new byte[4096];
					incomingPacket = new DatagramPacket(buf, buf.length);
					socket.setSoTimeout(1000);
					socket.receive(incomingPacket);
					byte[] tempBuffer = new byte[incomingPacket.getLength()];
					System.arraycopy(buf, 0, tempBuffer, 0, incomingPacket.getLength());
					buf = tempBuffer;
					ByteArrayInputStream in = new ByteArrayInputStream(buf);
					Unpacker unpacker = msgpack.createUnpacker(in);
					MapValue returnMsg = unpacker.readValue().asMapValue();
					String returnServiceName = returnMsg.get(key_service).asRawValue().getString();
					if (serviceName.equals(returnServiceName)) {
						int port = returnMsg.get(key_port).asIntegerValue().getInt();
						InetSocketAddress inetSocketAddress = new InetSocketAddress(incomingPacket.getAddress(), port);
						Result result = new Result(inetSocketAddress, null);
						if (returnMsg.containsKey(key_extra_info)) {
							result.extraInfo = returnMsg.get(key_extra_info);
						}
						return result;
					}
				} catch (SocketTimeoutException ex) {
					
				} catch (NullPointerException ex) {
					throw new RuntimeException(ex);
				} catch (MessageTypeException ex) {
					throw new RuntimeException(ex);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		} finally {
			socket.close();
		}
		return null;
	}
}
