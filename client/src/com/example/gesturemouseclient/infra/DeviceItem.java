package com.example.gesturemouseclient.infra;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author Yotam & Jonatan
 * 
 * a class to hold the device data we wish to connect with
 *
 */
public class DeviceItem {

	private InetAddress address;
	private int serverPort;
	private final String machineName;
	private Socket tcpSocket;
	private DatagramSocket udpSocket;
	

	/**
	 * Constructor
	 * @param serverPort
	 * @param inetAddress
	 */
	public DeviceItem(int serverPort, InetAddress address,String machineName) {
		this.serverPort = serverPort;
		this.address = address;
		this.machineName = machineName;
	}
	
	public int getServerPort() {
		return serverPort;
	}

	public String getMachineName() {
		return machineName;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setTcpSocket(Socket socket) {
		this.tcpSocket = socket;
	}

	public Socket getTcpSocket() {
		return tcpSocket;
	}

	public void setUdpSocket(DatagramSocket socket) {
		this.udpSocket = socket;
	}

	public DatagramSocket getUdpSocket() {
		return udpSocket;
	}
}
