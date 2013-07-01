package com.example.gesturemouseclient;

import java.net.InetAddress;

public abstract class ConnectionAbs extends Thread{
	protected int port;
	protected InetAddress address;
	protected Boolean pause;
	protected volatile boolean stop;

	public ConnectionAbs(int port, InetAddress address) {
		super();
		this.port = port;
		this.address = address;
		this.pause = false;
		this.stop = false;
	}

	void pauseRun()
	{
		synchronized (pause) {
			pause = true;
		}
	}

	public void resumeRun(){
		synchronized (pause) {
			pause = false;
		}
		notify();
	}




	@Override
	public void run(){
		while(!stop)
		{
			synchronized (pause) {
				if(pause)
				{
					try {
						wait();
					} catch (InterruptedException e) {
						continue;
					}
				}
			}
			innerAction();
		}
	}

	protected abstract void innerAction();





}
