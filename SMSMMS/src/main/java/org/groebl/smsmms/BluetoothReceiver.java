package org.groebl.smsmms;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.preference.PreferenceManager;

import org.groebl.smsmms.transaction.SmsHelper;
import org.groebl.smsmms.ui.settings.SettingsFragment;


public class BluetoothReceiver extends BroadcastReceiver {

    public static Boolean BTconnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothReceiver.BTconnected = true;
            
            if(mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MAXVOL, false)){
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AudioManager mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                    }
                }, 5000);
            }
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothReceiver.BTconnected = false;
            if(mPrefs.getBoolean(SettingsFragment.BLUETOOTH_DELETE, false)) { SmsHelper.deleteBluetoothMessages(context, false); }
        }
    }
}

