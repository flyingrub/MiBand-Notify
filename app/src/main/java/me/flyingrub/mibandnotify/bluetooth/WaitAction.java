package me.flyingrub.mibandnotify.bluetooth;

/**
 * Created by Lewis on 10/01/15.
 */
public class WaitAction implements BLEAction {
	private final long duration;

	public WaitAction(int duration) {
		this.duration = duration;
	}

	public void run() {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
