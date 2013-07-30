package com.example.gesturemouseclient.infra;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Yotam & Jonatan
 * 
 *         a class to hold the device data we wish to connect with
 * 
 */
public class DeviceItem {

	private InetAddress address;
	private int controlPort;
	private int UDPPort;
	private final String machineName;
	private Socket controlSocket;
	private BlockingDeque<GyroSample> queue;

	/**
	 * Constructor
	 * 
	 * @param controlPort
	 * @param inetAddress
	 */
	public DeviceItem(int controlPort, InetAddress address, String machineName) {
		this.controlPort = controlPort;
		this.address = address;
		this.machineName = machineName;
		this.queue = new LinkedBlockingDeque<GyroSample>();
	}

	public int getControlPort() {
		return controlPort;
	}

	public String getMachineName() {
		return machineName;
	}

	public InetAddress getAddress() {
		return address;
	}

	public int getUDPPort() {
		return UDPPort;
	}

	public void setUDPPort(int uDPPort) {
		UDPPort = uDPPort;
	}

	public Socket getControlSocket() {
		return controlSocket;
	}

	public void setControlSocket(Socket controlSocket) {
		this.controlSocket = controlSocket;
	}

	public BlockingDeque<GyroSample> getQueue() {
		return queue;
	}

}
