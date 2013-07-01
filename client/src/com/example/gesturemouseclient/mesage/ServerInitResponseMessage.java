package com.example.gesturemouseclient.mesage;

import java.io.IOException;

import org.msgpack.annotation.MessagePackMessage;
import org.msgpack.unpacker.Converter;

import android.util.Log;

@MessagePackMessage
public class ServerInitResponseMessage{
	
	public String service;
	public int tcp;
	public String getService() {
		return service;
	}
	
	public int getTcp() {
		return tcp;
	}
	
	
	
//	public int getTcp() {
//		try {
//			return new Converter(get("tcp")).readInt();
//		} catch (IOException e) {
//			Log.e("ServerInitResponseMessage",e.getMessage());
//			return 0;
//		}
//	}
//
//	public String getService() {
//		try {
//			return new Converter(get("service")).readString();
//		} catch (IOException e) {
//			Log.e("ServerInitResponseMessage",e.getMessage());
//			return null;
//		}
//	}

}
