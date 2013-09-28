package us.to.gesturemouse.threads;

import us.to.gesturemouse.infra.RemoteDeviceInfo;

public class BackgroundWorkManager {
	private ControlSessionThread controlSession;
	private FastSampleSenderThread fastSampleSender;
	private Thread controlSessionThread;
	private Thread fastSampleSenderThread;

	public BackgroundWorkManager(RemoteDeviceInfo remoteDeviceInfo) {
		super();
		controlSession = new ControlSessionThread(remoteDeviceInfo);
		fastSampleSender = new FastSampleSenderThread(remoteDeviceInfo);
		
		controlSessionThread = new Thread(controlSession);
		fastSampleSenderThread = new Thread(fastSampleSender);
	}

	public void sendGesture(int gestureId) {
		controlSession.sendGesture(gestureId);
	}

	public void sendKey(int keyId) {
		controlSession.sendKey(keyId);
	}

	public void sendKeys(int[] keyIds) {
		controlSession.sendKeys(keyIds);
	}

	public void sendSample(float[] sample) {
		fastSampleSender.sendSample(sample);
	}

	public void start() {
		controlSessionThread.start();
		fastSampleSenderThread.start();
	}

	public void stop() {
		controlSession.stop();
		fastSampleSender.stop();
	}

	public void join() throws InterruptedException {
		controlSessionThread.join();
		fastSampleSenderThread.join();
	}

	public void suspend() {
		controlSession.suspend();
		fastSampleSender.suspend();
	}

	public void resume() {
		controlSession.resume();
		fastSampleSender.resume();
	}

	public void resumeFastSampleSenderThread() {
		fastSampleSender.resume();
	}

	public void suspendFastSampleSenderThread() {
		fastSampleSender.suspend();
	}
}
