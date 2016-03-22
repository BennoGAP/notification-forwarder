package org.groebl.smsmms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import org.groebl.smsmms.transaction.NotificationManager;
import org.groebl.smsmms.ui.settings.SettingsFragment;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager.initQuickCompose(context, false, false);
        NotificationManager.create(context);

        SettingsFragment.updateAlarmManager(context, PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.NIGHT_AUTO, false));
    }
}
