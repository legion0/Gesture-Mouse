package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.example.gesturemouseclient.mesage.MousePositionMessage;

public class UdpClient extends ConnectionAbs{

	
	private DatagramSocket socket;
	
	/**
	 * Constructor: 
	 * 
	 * @param port
	 * @param serverAddress
	 */
	public UdpClient(int port, InetAddress address) {
		super(port,address);
	}

	public void initConnection() throws SocketException {
		socket = new DatagramSocket();
		run();
	}

	public void sendMessage(MousePositionMessage message) throws IOException
	{
		byte[] messageBuffer = message.pack();	
		DatagramPacket dp = new DatagramPacket(messageBuffer, messageBuffer.length,address,port);
		socket.send(dp);
	}


	public void close()
	{
		stop = true;
		socket.close();
	}


	@Override
	protected void innerAction() {
		try {
			sendMessage(new MousePositionMessage((int) (Math.random()*100)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}






}
