package com.example.gesturemouseclient.mesage;

import org.msgpack.annotation.MessagePackMessage;

@MessagePackMessage
public class GestureMessage {
	private int gid;

	public GestureMessage(int gid) {
		this.gid = gid;
	}
	
	

}
