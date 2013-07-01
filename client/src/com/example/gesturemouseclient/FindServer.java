package com.example.gesturemouseclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.msgpack.MessagePack;

import com.example.gesturemouseclient.mesage.ClientInitReqestMessage;
import com.example.gesturemouseclient.mesage.ServerInitResponseMessage;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

public class FindServer extends AsyncTask<Void,Void,InetSocketAddress> {

	private final int UDP_SERVER_PORT = 35200;
	
	private TcpClient tcpClient;
	private UdpClient udpClient;

	private MainActivity mainActivity;
	
	public FindServer(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	protected void onPreExecute(){
		//TODO: create progress bar
	}

	@Override
	protected InetSocketAddress doInBackground(Void... params) {
		
		DatagramSocket socket,listeningSocket;
		DatagramPacket packet;
		byte[] messageBuffer;
		Log.i("initialPcConnection","start");
		Log.e("initialPcConnection","start");
		Log.d("initialPcConnection","start");
		try {
			listeningSocket = new DatagramSocket(UDP_SERVER_PORT);
			
			// Sending UDP to server (PC):
			socket = new DatagramSocket();
			InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
			ClientInitReqestMessage clientRequest = new ClientInitReqestMessage();
			
			MessagePack msgpack = new MessagePack();
			messageBuffer = msgpack.write(clientRequest);
			
			packet = new DatagramPacket(messageBuffer, messageBuffer.length,broadcastAddress,UDP_SERVER_PORT);
			socket.send(packet);
			socket.close();
			
			// receive response from server:
			messageBuffer = new byte[1024];
			packet = new DatagramPacket(messageBuffer, messageBuffer.length);
			listeningSocket.receive(packet);
			byte[] newMessageBuffer = new byte[packet.getLength()];
			System.arraycopy(messageBuffer, 0, newMessageBuffer, 0, newMessageBuffer.length);
			
			ServerInitResponseMessage serverResponse = msgpack.read(newMessageBuffer,ServerInitResponseMessage.class);
			
			listeningSocket.close();
			
			if(serverResponse.getService().equals(clientRequest.get("service"))){
				
				Log.i("initialPcConnection", "response succseed, connecting to server...");
				Log.i("initialPcConnection", "address: "+packet.getAddress().toString());
				Log.i("initialPcConnection", "port: "+serverResponse.getTcp());
				return new InetSocketAddress(packet.getAddress(), serverResponse.getTcp());
//				
//				Log.i("serverResponse ","service: "+serverResponse.getService());
//				
//				tcpClient = new TcpClient(serverResponse.getTcp(),dpListener.getAddress());
//				udpClient = new UdpClient(dpListener.getAddress());
//				
//				tcpClient.initConnection();
//				udpClient.initConnection();
//				Log.i("initialPcConnection","success");
//				Log.e("initialPcConnection","success");
//				Log.d("initialPcConnection","success");
			}else{
				Log.e("initialPcConnection", "response failed, did not succseed connecting to server.");
				return null;
			}
			
		} catch (SocketException e) {
			Log.e("initialPcConnection",e.getMessage());
		}catch (UnknownHostException e) {
			Log.e("initialPcConnection",e.getMessage());
		}catch (IOException e) {
			Log.e("initialPcConnection",e.getMessage());
		}
		
		return null;
	}
	
	protected void onProgressUpdate(Integer... progress) {
        //TODO: update bar...
    }

    protected void onPostExecute(InetSocketAddress result) {
        //TODO: open connections.
    	
    }
	
	
}
