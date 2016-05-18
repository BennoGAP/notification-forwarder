package org.groebl.sms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;

import org.groebl.sms.ui.settings.SettingsFragment;


public class BluetoothApps extends PreferenceFragment {
    private SharedPreferences mSharedPref;
    private PreferenceCategory mWhiteList;
    private Set<String> mWhiteListEntries;
    private ColorFilter mGrayscaleFilter;



    class AppPreference extends CheckBoxPreference {
        private String mPkgName;

        public String getPkgName() {
            return mPkgName;
        }

        public void setPkgName(String mPkgName) { this.mPkgName = mPkgName; }

        public AppPreference(Context context) {
            super(context);
        }

    }

    class ApplicationComparator implements Comparator<AppPreference> {
        @Override
        public int compare(AppPreference a, AppPreference b) {
            return a.getTitle().toString().toLowerCase().compareTo(b.getTitle().toString().toLowerCase());
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
        PackageManager mPackageManager = getActivity().getPackageManager();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f);
        float[] matrix = colorMatrix.getArray();
        matrix[18] = 0.5f;
        mGrayscaleFilter = new ColorMatrixColorFilter(colorMatrix);
        addPreferencesFromResource(org.groebl.sms.R.xml.settings_bluetooth_apps);
        mWhiteList = (PreferenceCategory) findPreference(getString(org.groebl.sms.R.string.cat_applist));
        mWhiteList.setTitle(org.groebl.sms.R.string.pref_bluetooth_apps_title);
        Set<String> entries = mSharedPref.getStringSet(SettingsFragment.BLUETOOTH_SELECTAPPS, null);
        if (entries == null) {
            mWhiteListEntries = new HashSet<>();
        } else {
            mWhiteListEntries = new HashSet<>(entries);
        }
        List<ApplicationInfo> pkgs = mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppPreference> prefs = new ArrayList<>();

        for (ApplicationInfo pkg : pkgs) {
            AppPreference pref = new AppPreference(getActivity());
            pref.setTitle(mPackageManager.getApplicationLabel(pkg));
            Drawable icon = pkg.loadIcon(mPackageManager);
            pref.setPkgName(pkg.packageName);
            if (mWhiteListEntries.contains(pkg.packageName)) {
                pref.setDefaultValue(true);
            } else {
                pref.setDefaultValue(false);
                icon.setColorFilter(mGrayscaleFilter);
            }
            pref.setIcon(icon);
            prefs.add(pref);
        }
        Collections.sort(prefs, new ApplicationComparator());
        for (AppPreference pref : prefs) {
            mWhiteList.addPreference(pref);
        }
    }

    private void editEntry(AppPreference pref) {

        String pkg = pref.getPkgName();
        boolean disabled = !pref.isChecked();
        Drawable icon = pref.getIcon();
        if (pref.isChecked()) {
            icon.setColorFilter(null);
        } else {
            icon.setColorFilter(mGrayscaleFilter);
        }
        ArrayList<String> newlist = new ArrayList<>(mWhiteListEntries);
        boolean iswhitelisted = newlist.contains(pkg);
        if (disabled && !iswhitelisted) {
            return;
        } else if (disabled) { //
            newlist.remove(pkg);
        } else if (!disabled && iswhitelisted) {
            return;
        } else if (!disabled) {
            newlist.add(pkg);
        }


        mWhiteListEntries = new HashSet<>(newlist);
        Editor editor = mSharedPref.edit();
        editor.putStringSet(SettingsFragment.BLUETOOTH_SELECTAPPS, mWhiteListEntries);
        editor.apply();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
        if (!isAdded()) { return false; }

        String key = preference.getKey() != null ? preference.getKey() : "";

        switch(key) {
            case SettingsFragment.BLUETOOTH_SELECT_ALL:
                for (int i = 0; i < mWhiteList.getPreferenceCount(); i++) {
                    AppPreference pref = (AppPreference) mWhiteList.getPreference(i);
                    pref.setChecked(true);
                    editEntry(pref);
                }
                break;
            case SettingsFragment.BLUETOOTH_SELECT_NONE:
                for (int i = 0; i < mWhiteList.getPreferenceCount(); i++) {
                    AppPreference pref = (AppPreference) mWhiteList.getPreference(i);
                    pref.setChecked(false);
                    editEntry(pref);
                }
                break;
            default:
                AppPreference pref = (AppPreference) preference;
                editEntry(pref);
        }

        return true;
    }
}