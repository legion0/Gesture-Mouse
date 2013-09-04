package Threads;

import com.example.gesturemouseclient.infra.DeviceItem;

public class BackgroundWorkManager {
	private ControlSessionThread controlSessionThread;
	private FastSampleSenderThread fastSampleSenderThread;

	public BackgroundWorkManager(DeviceItem remoteDeviceInfo) {
		super();
		this.controlSessionThread = new ControlSessionThread(remoteDeviceInfo);
		this.fastSampleSenderThread = new FastSampleSenderThread(remoteDeviceInfo);
	}

	public void sendGesture(int gestureId) {
		this.controlSessionThread.sendGesture(gestureId);
	}

	public void sendKey(int keyId) {
		this.controlSessionThread.sendKey(keyId);
	}

	public void sendSample(float[] sample) {
		this.fastSampleSenderThread.sendSample(sample);
	}

	public void start() {
		this.controlSessionThread.start();
		this.fastSampleSenderThread.start();
	}

	public void stop() {
		this.controlSessionThread.stopRun();
		this.fastSampleSenderThread.stopRun();
	}

	public void suspend() {
		this.controlSessionThread.pauseRun();
		this.fastSampleSenderThread.pauseRun();
	}

	public void resume() {
		this.controlSessionThread.resumeRun();
		this.fastSampleSenderThread.resumeRun();
	}
}
