package com.example.gesturemouseclient.infra;

import java.net.InetAddress;

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

	/**
	 * Constructor
	 * @param serverPort
	 * @param inetAddress
	 */
	public DeviceItem(int serverPort, InetAddress address,String machineName) {
		this.serverPort = serverPort;
		this.setAddress(address);
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



	public void setAddress(InetAddress address) {
		this.address = address;
	}

	
	


}
