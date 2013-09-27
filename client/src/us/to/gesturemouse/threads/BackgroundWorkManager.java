package us.to.gesturemouse.threads;

import us.to.gesturemouse.infra.RemoteDeviceInfo;

public class BackgroundWorkManager {
	private ControlSessionThread controlSession;
	private FastSampleSenderThread fastSampleSender;
	private Thread controlSessionThread;
	private Thread fastSampleSenderThread;

	public BackgroundWorkManager(RemoteDeviceInfo remoteDeviceInfo) {
		super();
		this.controlSession = new ControlSessionThread(remoteDeviceInfo);
		this.fastSampleSender = new FastSampleSenderThread(remoteDeviceInfo);
		
		this.controlSessionThread = new Thread(controlSession);
		this.fastSampleSenderThread = new Thread(fastSampleSender);
	}

	public void sendGesture(int gestureId) {
		this.controlSession.sendGesture(gestureId);
	}

	public void sendKey(int keyId) {
		this.controlSession.sendKey(keyId);
	}

	public void sendKeys(int[] keyIds) {
		this.controlSession.sendKeys(keyIds);
	}

	public void sendSample(float[] sample) {
		this.fastSampleSender.sendSample(sample);
	}

	public void start() {
		this.controlSessionThread.start();
		this.fastSampleSenderThread.start();
	}

	public void stop() {
		this.controlSession.stopRun();
		this.fastSampleSender.stopRun();
	}

	public void suspend() {
		this.controlSession.pauseRun();
		this.fastSampleSender.pauseRun();
	}

	public void resume() {
		this.controlSession.resumeRun();
		this.fastSampleSender.resumeRun();
	}

	public void resumeFastSampleSenderThread() {
		this.fastSampleSender.resumeRun();
	}

	public void suspendFastSampleSenderThread() {
		this.fastSampleSender.pauseRun();		
	}
}
