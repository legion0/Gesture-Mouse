package us.to.gesturemouse.threads;


public abstract class PausableRunnable implements Runnable {

	protected Boolean shouldPause;
	protected boolean isStoped;
	private boolean isPaused;

	public PausableRunnable() {
		super();
		shouldPause = false;
		isStoped = false;
		isPaused = false;
	}
	
	public synchronized void stop() {
		isStoped = true;
		if (isPaused) {
			notify();
		}
	}

	public synchronized void suspend() {
		shouldPause = true;
	}

	public synchronized void resume() {
		if (isPaused) {
			notify();
		}
	}

	@Override
	public void run() {
//		Logger.printLog("Pauseable run: ", "start running...");
		while (!isStoped) {
			doPause();
			innerAction();
		}
	}

	private synchronized void doPause() {
		if (shouldPause) {
			shouldPause = false;
			isPaused = true;
			try {
//				Logger.printLog("Pauseable run: ", "wait...");
				wait();
			} catch (InterruptedException e) {
//				Logger.printLog("Pauseable Thread(InterruptedException): ","errorMSg: "+e.getMessage());
			}
			isPaused = false;
		}
	}

	protected abstract void innerAction();

}