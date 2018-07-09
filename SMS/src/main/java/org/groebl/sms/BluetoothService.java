package org.groebl.sms;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

public class BluetoothService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private final BroadcastReceiver mReceiver = new BluetoothReceiver();

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCreate()   {
    }

    public void onDestroy() {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter1 = new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED");
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");
        IntentFilter filter3 = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
        getApplicationContext().registerReceiver(this.mReceiver, filter1);
        getApplicationContext().registerReceiver(this.mReceiver, filter2);
        getApplicationContext().registerReceiver(this.mReceiver, filter3);
        return START_STICKY;
    }

}
