package com.example.gesturemouseclient;

import com.example.gesturemouseclient.infra.Logger;

public abstract class PausableThread implements Runnable {

	protected Boolean pause;
	protected volatile Boolean stop;

	public PausableThread() {
		super();
		this.pause = false;
		this.stop = false;
	}
	
	public void stopRun(){
		synchronized (this) {
			this.stop = true;
		}
		resumeRun();
		
	}

	public void pauseRun() {
		synchronized (this) {
			pause = true;
		}
	}

	public void resumeRun() {
		synchronized (this) {
			pause = false;
			notify();
		}
		
	}

	@Override
	public void run() {
		Logger.printLog("Pauseable run: ", "start running...");
		while (!stop) {
			synchronized (this) {
				if (pause) {
					try {
						Logger.printLog("Pauseable run: ", "wait...");
						wait();
					} catch (InterruptedException e) {
						Logger.printLog("Pauseable Thread(InterruptedException): ","errorMSg: "+e.getMessage());
						continue;
					} 
					catch (Exception e) {
						Logger.printLog("Pauseable Thread(Exception): ","errorMSg: "+e.getMessage());
						break;
					}
				}
			}
			innerAction();
		}
	}

	protected abstract void innerAction();

}