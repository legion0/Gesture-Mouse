package com.example.gesturemouseclient.mesage;

import org.msgpack.annotation.MessagePackMessage;

@MessagePackMessage
public class MouseClickMessage {
	private String click;

	public MouseClickMessage(String click) {
		this.click = click;
	}
	
	

}
