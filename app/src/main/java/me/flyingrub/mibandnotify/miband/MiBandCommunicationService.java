package me.flyingrub.mibandnotify.miband;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;


import me.flyingrub.mibandnotify.bluetooth.BLECommunicationManager;
import me.flyingrub.mibandnotify.bluetooth.WaitAction;
import me.flyingrub.mibandnotify.bluetooth.WriteAction;

/**
 * Created by Lewis on 29/12/14.
 */
public class MiBandCommunicationService
{
	private final String TAG = this.getClass().getSimpleName();

	private static final WriteAction vibrate = new WriteAction(MiBandConstants.UUID_CHARACTERISTIC_CONTROL_POINT, new byte[]{ (byte) 8, (byte) 2 });

	private BLECommunicationManager bleCommunicationManager;

    private byte[] ledColor = { (byte) 33, (byte) 150, (byte) 243};

    private boolean blinkLed = false;

	public MiBandCommunicationService(BLECommunicationManager bleCommunicationManager) {
		this.bleCommunicationManager = bleCommunicationManager;
	}

    public void onNotificationPosted() {
        blinkLed = true;
        new Thread(new Runnable() {
            public void run() {
                notifyBand();
            }
        }).start();
    }

    public void onNotificationRemoved() {
        blinkLed = false;
    }

    public boolean isBlinking() {
        return blinkLed;
    }

    public void notifyBand() {
        bleCommunicationManager.queueTask(vibrate);
        bleCommunicationManager.queueTask(new WaitAction(500));

        while (blinkLed) {
            Log.d(TAG, "blink !!!");
            bleCommunicationManager.queueTask(new WriteAction(MiBandConstants.UUID_CHARACTERISTIC_CONTROL_POINT, new byte[]{14, ledColor[0], ledColor[1], ledColor[2], (byte) 1}));

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
