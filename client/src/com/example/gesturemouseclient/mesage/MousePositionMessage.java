package com.example.gesturemouseclient.mesage;

import java.io.IOException;

import org.msgpack.MessagePack;
import org.msgpack.annotation.MessagePackMessage;

@MessagePackMessage
public class MousePositionMessage {
	private int gyro;

	public MousePositionMessage(int gyro) {
		this.gyro = gyro;
	}
	
	public byte[] pack() throws IOException
	{
		MessagePack msgpack = new MessagePack();
		return msgpack.write(this);
	}

}
