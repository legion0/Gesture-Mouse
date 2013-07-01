package com.example.gesturemouseclient;

import java.net.InetAddress;

public class TcpClient extends Thread{
	private int serverPort;
	private InetAddress serverAddress;
	
	
	/**
	 * Constructor: 
	 * 
	 * @param port
	 * @param serverAddress
	 */
	public TcpClient(int port, InetAddress serverAddress) {
		super();
		this.serverPort = port;
		this.serverAddress = serverAddress;
	}
	
	public int getPort() {
		return serverPort;
	}
	
	public InetAddress getServerAddress() {
		return serverAddress;
	}

	public void initConnection() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	

}
