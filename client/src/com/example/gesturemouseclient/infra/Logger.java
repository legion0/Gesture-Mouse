package com.example.gesturemouseclient.infra;

import android.util.Log;

public class Logger {
	
	public static void printLog(String tag, String msg){
		Log.i(tag,msg);		
		Log.v(tag,msg);
		Log.d(tag,msg);
		Log.w(tag,msg);
	}

}
