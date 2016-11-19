package org.groebl.sms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.groebl.sms.service.DeleteOldMessagesService;
import org.groebl.sms.transaction.NotificationManager;
import org.groebl.sms.ui.settings.SettingsFragment;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager.initQuickCompose(context, false, false);
        NotificationManager.create(context);

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefs.edit().putBoolean(SettingsFragment.BLUETOOTH_CURRENT_STATUS, false).commit();

        SettingsFragment.updateAlarmManager(context, PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.NIGHT_AUTO, false));

        DeleteOldMessagesService.setupAutoDeleteAlarm(context);
    }
}
