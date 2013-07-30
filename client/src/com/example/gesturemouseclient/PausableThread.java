package com.example.gesturemouseclient;

public abstract class PausableThread extends Thread {

	protected Boolean pause;
	protected volatile boolean stop;

	public PausableThread() {
		super();
		this.pause = false;
		this.stop = false;
	}

	void pauseRun() {
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
		while (!stop) {
			synchronized (pause) {
				if (pause) {
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