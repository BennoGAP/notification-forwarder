package org.groebl.sms.ui.dialog;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

import org.groebl.sms.R;
import org.groebl.sms.ui.base.QKActivity;
import org.groebl.sms.ui.settings.SettingsFragment;
import org.groebl.sms.ui.view.QKEditText;

import java.util.HashSet;
import java.util.Set;

public class BlockedBluetoothDialog {

    public static void blockWAConversation(SharedPreferences prefs, String address) {
        Set<String> idStrings = prefs.getStringSet(SettingsFragment.BLOCKED_WHATSAPP, new HashSet<>());
        idStrings.add(address);
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_WHATSAPP, idStrings).apply();
    }

    public static void unblockWAConversation(SharedPreferences prefs, String address) {
        Set<String> idStrings2 = prefs.getStringSet(SettingsFragment.BLOCKED_WHATSAPP, new HashSet<>());
        idStrings2.remove(address);
        prefs.edit().putStringSet(SettingsFragment.BLOCKED_WHATSAPP, idStrings2).apply();
    }

    public static Set<String> getWABlockedConversations(SharedPreferences prefs) {
        return prefs.getStringSet(SettingsFragment.BLOCKED_WHATSAPP, new HashSet<>());
    }

    public static boolean isWABlocked(SharedPreferences prefs, String address) {
        for (String s : getWABlockedConversations(prefs)) {
            if (s.equalsIgnoreCase(address)) {
                return true;
            }
        }

        return false;
    }

    public static void showDialog(final QKActivity context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> addresses = getWABlockedConversations(prefs);

        new QKDialog()
                .setContext(context)
                .setTitle(R.string.pref_block_whatsapp_title)
                .setItems(addresses.toArray(new String[addresses.size()]), (parent, view, position, id) -> new QKDialog()
                        .setContext(context)
                        .setTitle(R.string.title_unblock_whatsapp_group)
                        .setMessage(((TextView) view).getText().toString())
                        .setPositiveButton(R.string.yes, v -> unblockWAConversation(prefs, ((TextView) view).getText().toString()))
                        .setNegativeButton(R.string.cancel, null)
                        .show())
                .setPositiveButton(R.string.add, v -> {
                    final QKEditText editText = new QKEditText(context);
                    new QKDialog()
                            .setContext(context)
                            .setTitle(R.string.title_block_whatsapp_group)
                            .setCustomView(editText)
                            .setPositiveButton(R.string.add, v1 -> {
                                if (editText.getText().length() > 0) {
                                    blockWAConversation(prefs, editText.getText().toString());
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
