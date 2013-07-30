package com.example.gesturemouseclient;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.example.gesturemouseclient.infra.Logger;

public class FastSensorConnection extends PausableThread {
	
	private final InetSocketAddress inetSocketAddress;
	private DatagramSocket socket;
	
	/**
	 * Constctur:
	 * 
	 * @param inetSocketAddress
	 * @throws SocketException 
	 */
	public FastSensorConnection(InetSocketAddress inetSocketAddress) throws SocketException {
		super();
		this.inetSocketAddress = inetSocketAddress;	
		socket = new DatagramSocket(inetSocketAddress.getPort(),inetSocketAddress.getAddress());
	}
	
	
	
	
	
	
	
	@Override
	protected void innerAction() {
		
		
	}



	

	
	
	

}
