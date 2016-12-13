package org.groebl.sms;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothBootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent myintent) {
            boolean found = false;
            for (RunningServiceInfo service : ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
                if (BluetoothService.class.getName().equals(service.service.getClassName())) {
                    found = true;
                    break;
                }
            }

            if (!found) { context.startService(new Intent(context, BluetoothService.class)); }
    }
}