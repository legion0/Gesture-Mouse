package com.example.gesturemouseclient.infra;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.SeekBar;

/**
 * @author Yotam & Jonatan
 * 
 *         a class to hold the device data we wish to connect with
 * 
 */
public class DeviceItem implements Parcelable{

	private String sessionId;
	private InetAddress address;
	private int controlPort;
	private int UDPPort;
	private final String machineName;
	private Socket controlSocket;
	private BlockingDeque<GyroSample> gyroQueue;
	private BlockingDeque<GyroSample> gestureQueue;
	private BlockingDeque<Integer> clickQueue;

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
		this.gyroQueue = new LinkedBlockingDeque<GyroSample>();
		this.gestureQueue = new LinkedBlockingDeque<GyroSample>();
		this.clickQueue = new LinkedBlockingDeque<Integer>();

	}

	// example constructor that takes a Parcel and gives you an object populated with it's values
	private DeviceItem(Parcel in) {
		sessionId = in.readString();
		try {
			address = InetAddress.getByName(in.readString());
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		controlPort = in.readInt();
		UDPPort = in.readInt();
		machineName = in.readString();
		this.gyroQueue = new LinkedBlockingDeque<GyroSample>();
		this.gestureQueue = new LinkedBlockingDeque<GyroSample>();
		this.clickQueue = new LinkedBlockingDeque<Integer>();
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

	public BlockingDeque<GyroSample> getGyroQueue() {
		return gyroQueue;
	}

	public BlockingDeque<Integer> getClickQueue() {
		return clickQueue;
	}

	public BlockingDeque<GyroSample> getGestureQueue() {
		return gestureQueue;
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
		out.writeInt(UDPPort);
		out.writeString(machineName);
	}

	// this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
	public static final Parcelable.Creator<DeviceItem> CREATOR = new Parcelable.Creator<DeviceItem>() {
		public DeviceItem createFromParcel(Parcel in) {
			return new DeviceItem(in);
		}

		public DeviceItem[] newArray(int size) {
			return new DeviceItem[size];
		}
	};
}
