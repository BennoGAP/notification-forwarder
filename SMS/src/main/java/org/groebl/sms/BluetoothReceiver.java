package org.groebl.sms;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import org.groebl.sms.ui.settings.SettingsFragment;

import java.util.HashSet;
import java.util.Set;


public class BluetoothReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> bt_device_whitelist = mPrefs.getStringSet(SettingsFragment.BLUETOOTH_DEVICES, new HashSet<>());
        BluetoothDevice bt_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (bt_device_whitelist.contains(bt_device.getName())) {

            switch (action) {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    mPrefs.edit().putBoolean(SettingsFragment.BLUETOOTH_CURRENT_STATUS, true).commit();

                    //Set Bluetooth-Volume
                    if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MAXVOL, false)) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                        }, 5000);
                    }
                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mPrefs.edit().putBoolean(SettingsFragment.BLUETOOTH_CURRENT_STATUS, false).commit();

                    //Delete Temporary Messages
                    if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_DELETE, true)) {
                        new Thread(() -> {
                            BluetoothHelper.deleteBluetoothMessages(context, false);
                        }).start();
                    }
                    break;
            }
        }
    }
}


