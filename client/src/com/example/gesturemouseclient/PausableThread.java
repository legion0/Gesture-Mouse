package com.example.gesturemouseclient;

import com.example.gesturemouseclient.infra.Logger;

public abstract class PausableThread extends Thread {

	protected Boolean pause;
	protected volatile boolean stop;

	public PausableThread() {
		super();
		this.pause = false;
		this.stop = false;
	}
	
	public void stopRun(){
		this.pause = true;
		this.stop = true;
	}

	public void pauseRun() {
		synchronized (pause) {
			pause = true;
		}
	}

	public void resumeRun() {
		synchronized (pause) {
			pause = false;
		}
		notify();
	}

	@Override
	public void run() {
		Logger.printLog("Pauseable run: ", "start running...");
		while (!stop) {
			synchronized (pause) {
				if (pause) {
					try {
						Logger.printLog("Pauseable run: ", "wait...");
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