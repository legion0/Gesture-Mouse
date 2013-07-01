package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class TcpServer extends ConnectionAbs{
	
	private final static int PORT = 35202;
	private ServerSocket serverSocket; 
	
	
	/**
	 * Constructor: 
	 * 
	 * @param serverAddress
	 */
	public TcpServer(InetAddress address) {
		super(PORT,address);
	}
	
	

	public void initConnection() throws IOException {
//		Log.i("TcpServer:initConnetion","S: Connecting...");
		 
        //create a server socket. A server socket waits for requests to come in over the network.
//        serverSocket = new ServerSocket(PORT);
//        Socket s = serverSocket.accept();
	}



	@Override
	protected void innerAction() {
			
	}
	
	

		

}
