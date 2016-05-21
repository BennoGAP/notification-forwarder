package org.groebl.sms;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import org.groebl.sms.ui.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class BluetoothDevices extends PreferenceFragment {
    private SharedPreferences mSharedPref;
    private Set<String> mBlackListEntries;


    class AppPreference extends CheckBoxPreference {
        public AppPreference(Context context) {
            super(context);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> isNotSelected = new HashSet<>();
        addPreferencesFromResource(R.xml.settings_bluetooth_devices);
        PreferenceCategory mBlackList = (PreferenceCategory) findPreference(getString(R.string.cat_devicelist));
        mBlackList.setTitle(R.string.pref_bluetooth_devices_title);
        Set<String> entries = mSharedPref.getStringSet(SettingsFragment.BLUETOOTH_DEVICES, null);

        if (entries == null) {
           mBlackListEntries = new HashSet<>();
        } else {
           mBlackListEntries = new HashSet<>(entries);
        }

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice bt : pairedDevices) {
                AppPreference pref = new AppPreference(getActivity());
                pref.setTitle(bt.getName());
                pref.setIcon(R.drawable.ic_launcher_bluetooth);
                if (mBlackListEntries.contains(bt.getName())) {
                    pref.setDefaultValue(false);
                    isNotSelected.add(bt.getName());
                } else {
                    pref.setDefaultValue(true);
                }
                mBlackList.addPreference(pref);
            }

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putStringSet(SettingsFragment.BLUETOOTH_DEVICES, isNotSelected);
            editor.apply();
        }

    }

    private void editEntry(AppPreference pref) {

        String dev_name = pref.getTitle().toString();
        boolean disabled = !pref.isChecked();

        ArrayList<String> newlist = new ArrayList<>(mBlackListEntries);
        boolean isblacklisted = newlist.contains(dev_name);
        if (disabled && isblacklisted) {
            return;
        } else if (disabled) {
            newlist.add(dev_name);
        } else if (!disabled && !isblacklisted) {
            return;
        } else if (!disabled) {
            newlist.remove(dev_name);
        }


        mBlackListEntries = new HashSet<>(newlist);
        Editor editor = mSharedPref.edit();
        editor.putStringSet(SettingsFragment.BLUETOOTH_DEVICES, mBlackListEntries);
        editor.apply();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
        if (!isAdded()) { return false; }

        AppPreference pref = (AppPreference) preference;
        editEntry(pref);
        return true;
    }
}