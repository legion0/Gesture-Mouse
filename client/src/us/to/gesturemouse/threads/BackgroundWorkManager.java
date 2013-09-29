package us.to.gesturemouse.threads;

import android.app.Activity;
import android.content.Context;
import us.to.gesturemouse.infra.RemoteDeviceInfo;
import us.to.gesturemouse.infra.interfaces.ApplicationListener;

public class BackgroundWorkManager {
	private ControlSessionThread controlSession;
	private FastSampleSenderThread fastSampleSender;
	private Thread controlSessionThread;
	private Thread fastSampleSenderThread;
	private ApplicationListener applicationListener;
	private ApplicationListenerTask applicationListenerTask;
	private final RemoteDeviceInfo remoteDeviceInfo;

	public BackgroundWorkManager(RemoteDeviceInfo remoteDeviceInfo, Context context, Activity activity, Runnable actionOnConnect, ApplicationListener applicationListener) {
		super();
		this.remoteDeviceInfo = remoteDeviceInfo;
		this.applicationListener = applicationListener;
		controlSession = new ControlSessionThread(remoteDeviceInfo, context, activity, actionOnConnect);
		fastSampleSender = new FastSampleSenderThread(remoteDeviceInfo);
		controlSessionThread = new Thread(controlSession);
		fastSampleSenderThread = new Thread(fastSampleSender);
		applicationListenerTask = new ApplicationListenerTask(remoteDeviceInfo, applicationListener);
	}

	public void connect() {
		controlSession.connect();
	}

	public void disconnect() {
		controlSession.disconnect();
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
		controlSessionThread = new Thread(controlSession);
		fastSampleSenderThread = new Thread(fastSampleSender);
		controlSessionThread.start();
		fastSampleSenderThread.start();
		applicationListenerTask = new ApplicationListenerTask(remoteDeviceInfo, applicationListener);
		applicationListenerTask.execute();
	}

	public void stop() {
		applicationListenerTask.cancel(false);
		controlSession.disconnect();
		fastSampleSender.stop();
		controlSession.stop();
	}

	public void join() throws InterruptedException {
		controlSessionThread.join();
		fastSampleSenderThread.join();
	}

	public void suspend() {
		controlSession.suspend();
		fastSampleSender.suspend();
		applicationListenerTask.cancel(false);
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
