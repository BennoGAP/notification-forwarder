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

import org.groebl.sms.transaction.SmsHelper;
import org.groebl.sms.ui.settings.SettingsFragment;

import java.util.Set;

public class BluetoothReceiver extends BroadcastReceiver {

    public static Boolean BTconnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> bt_device_blacklist = mPrefs.getStringSet(SettingsFragment.BLUETOOTH_DEVICES, null);
        Boolean blacklist = false;
        BluetoothDevice bt_device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (bt_device_blacklist != null && bt_device_blacklist.contains(bt_device.getName())) { blacklist = true; }


        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) && !blacklist) {
            BluetoothReceiver.BTconnected = true;
            if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MAXVOL, false)){
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    AudioManager mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                }, 5000);
            }

            //if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_TETHERING, false)) {
            //}

        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) && !blacklist) {
            BluetoothReceiver.BTconnected = false;
            if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_DELETE, false)) {
                new Thread(() -> {
                    SmsHelper.deleteBluetoothMessages(context, false);
                }).start();
            }

            //if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_TETHERING, false)) {
            //}
        }
    }
}

