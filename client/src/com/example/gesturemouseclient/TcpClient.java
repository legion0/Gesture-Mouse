package com.example.gesturemouseclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

import com.example.gesturemouseclient.infra.Logger;
import com.example.gesturemouseclient.infra.ResponseReader;

public class TcpClient {
	
	private ResponseReader responseReader = null;
	private long timeout = -1;
	
	private static final RawValue key_extra_info = ValueFactory.createRawValue("extra_info".getBytes());
	private static final RawValue key_features = ValueFactory.createRawValue("features".getBytes());
	private static final RawValue key_port = ValueFactory.createRawValue("port".getBytes());
	private static final RawValue key_name = ValueFactory.createRawValue("name".getBytes());
	private static final RawValue key_gid = ValueFactory.createRawValue("gid".getBytes());
	private static final RawValue key_apps = ValueFactory.createRawValue("apps".getBytes());
	private static final RawValue key_actions = ValueFactory.createRawValue("actions".getBytes());
	private static final RawValue key_gestures = ValueFactory.createRawValue("gestures".getBytes());
	private static final RawValue key_udp = ValueFactory.createRawValue("udp".getBytes());
	private final int tcp_outgoing_port;
	private final String deviceName;
	private final InetAddress address;


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
		this.responseReader = responseReader;
		this.tcp_outgoing_port = tcp_outgoing_port;
		this.deviceName = deviceName;
		this.address = address;
	}


	public void setTimeout(int seconds) {
		timeout  = seconds * 1000000000l;
	}
	
	private byte[] createmsg(final String tcpPort, String[] features) throws IOException {
		Map<Object, Object> msg = new LinkedHashMap<Object, Object>() {
			{
				put(key_name,deviceName);
				put(key_port, tcpPort);
				List<Object> apps = new ArrayList<Object>() {
					{
						Map<Object, Object> app = new LinkedHashMap<Object, Object>() {
							{
								put(key_name,"browser");
								Map<Object, Object> gestures = new LinkedHashMap<Object, Object>() {
									{
										put(key_name,"flick left");
										put(key_gid,"134");
										List<Object> actions = new ArrayList<Object>() {
											{
												add("17");										
											}
										};
										put(key_actions,actions);										
									}
								};
								put(key_gestures,gestures);
							}
						};
						add(app);
					}
				};
				put(key_apps,apps);
			}
		};
		if (features != null && features.length > 0) {
			msg.put(key_features, features);
		}
		MessagePack msgpack = new MessagePack();
		return msgpack.write(msg);
	} 
	
	


	public Integer initControllSession(final String tcpPort, String[] features) throws IOException {
		byte[] msgBuffer = createmsg(tcpPort,features);

        Logger.printLog("TCP Client", "C: Connecting...");

        //create a socket to make the connection with the server
        Socket socket = new Socket(address, tcp_outgoing_port);
        
        try {
        	 
            //send the message to the server
        	OutputStream outputStream = socket.getOutputStream();
        	outputStream.write(msgBuffer);
            Logger.printLog("TCP Client", "C: Sent.");

            //receive the message which the server sends back
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
			if (udpFromServer <= 0) {
				throw new MessageTypeException("Invalid Udp");
			}
			
			Logger.printLog("TCP Client","S: Received udp port: " + udpFromServer);
			
			return new Integer(udpFromServer);

        } catch (Exception e) {

            Logger.printLog("TCPClient", "S: Error, "+e.getMessage());

        } finally {
            //the socket must be closed. It is not possible to reconnect to this socket
            // after it is closed, which means a new socket instance has to be created.
            socket.close();
        }
//		InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
//		MulticastSocket socket = new MulticastSocket();
//		socket.setTimeToLive(ttl);
//		DatagramPacket outgoingPacket = new DatagramPacket(msgBuffer, msgBuffer.length, group, NSD_PORT);
//		DatagramPacket incomingPacket;
//
//		byte[] buf;
//		long end = timeout > -1 ? System.nanoTime() + timeout : Long.MAX_VALUE;
//		try {
//			while (System.nanoTime() < end) {
//				try {
//					socket.send(outgoingPacket);
//
//					buf = new byte[4096];
//					incomingPacket = new DatagramPacket(buf, buf.length);
//					socket.setSoTimeout(1000);
//					socket.receive(incomingPacket);
//					byte[] tempBuffer = new byte[incomingPacket.getLength()];
//					System.arraycopy(buf, 0, tempBuffer, 0, incomingPacket.getLength());
//					buf = tempBuffer;
//					ByteArrayInputStream in = new ByteArrayInputStream(buf);
//					Unpacker unpacker = msgpack.createUnpacker(in);
//					MapValue returnMsg = unpacker.readValue().asMapValue();
//					String returnServiceName = returnMsg.get(key_service).asRawValue().getString();
//					if (!serviceName.equals(returnServiceName)) {
//						throw new MessageTypeException("Invalid Service Name");
//					}
//					int port = returnMsg.get(key_port).asIntegerValue().getInt();
//					if (returnMsg.containsKey(key_extra_info)) {
//						responseReader.read(returnMsg.get(key_extra_info));
//					}
//					return new InetSocketAddress(incomingPacket.getAddress(), port);
//				} catch (SocketTimeoutException ex) {
//					// System.out.println("timeout");
//				} catch (NullPointerException ex) {
//					ex.printStackTrace();
//				} catch (MessageTypeException ex) {
//					ex.printStackTrace();
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//		} finally {
//			socket.close();
//		}
		return null;
	}


	
	
	

}
