package com.example.gesturemouseclient.infra;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * @author Yotam & Jonatan
 * 
 *         a class to hold the device data we wish to connect with
 * 
 */
public class RemoteDeviceInfo implements Parcelable {

	private String sessionId;
	private InetAddress address;
	private int controlPort;
	private int localControlPort;
	private int UDPPort;
	private final String machineName;
	private boolean connected;
	private String activeApplication;

	/**
	 * Constructor
	 * 
	 * @param controlPort
	 * @param inetAddress
	 */
	public RemoteDeviceInfo(int controlPort, InetAddress address, String machineName) {
		this.controlPort = controlPort;
		this.address = address;
		this.machineName = machineName;
		connected = false;
	}

	// example constructor that takes a Parcel and gives you an object populated with it's values
	private RemoteDeviceInfo(Parcel in) {
		sessionId = in.readString();
		try {
			address = InetAddress.getByName(in.readString());
		} catch (UnknownHostException e) {
			Log.e("RemoteDeviceInfo", "failed to get Self Network name", e);
		}
		controlPort = in.readInt();
		localControlPort = in.readInt();
		UDPPort = in.readInt();
		machineName = in.readString();
		activeApplication = in.readString();
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

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	// 99.9% of the time you can just ignore this
	public int describeContents() {
		return 0;
	}

	// write your object's data to the passed-in Parcel
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(sessionId);
		out.writeString(address.getHostAddress());
		out.writeInt(controlPort);
		out.writeInt(localControlPort);
		out.writeInt(UDPPort);
		out.writeString(machineName);
		out.writeString(activeApplication);
	}

	// this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
	public static final Parcelable.Creator<RemoteDeviceInfo> CREATOR = new Parcelable.Creator<RemoteDeviceInfo>() {
		public RemoteDeviceInfo createFromParcel(Parcel in) {
			return new RemoteDeviceInfo(in);
		}

		public RemoteDeviceInfo[] newArray(int size) {
			return new RemoteDeviceInfo[size];
		}
	};

	public synchronized boolean isConnected() {
		return connected;
	}

	public synchronized void setConnected(boolean connected) {
		this.connected = connected;
	}

	public String getActiveApplication() {
		return activeApplication;
	}

	public void setActiveApplication(String activeApplication) {
		this.activeApplication = activeApplication;
	}

	public String getName() {
		return getMachineName();
	}
	
	public int getLocalControlPort() {
		return localControlPort;
	}

	public void setLocalControlPort(int localControlPort) {
		this.localControlPort = localControlPort;
	}
}
