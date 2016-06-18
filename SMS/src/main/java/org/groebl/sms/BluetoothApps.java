package org.groebl.sms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import org.groebl.sms.ui.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f);
        float[] matrix = colorMatrix.getArray();
        matrix[18] = 0.5f;
        mGrayscaleFilter = new ColorMatrixColorFilter(colorMatrix);

        new LoadApplications(getActivity().getApplicationContext()).execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    private class LoadApplications extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pDialog;
        List<AppPreference> prefs = new ArrayList<>();

        public LoadApplications(Context context){
            Context mContext = context;
        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(getString(R.string.pref_bluetooth_apps_loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            Set<String> isSelected = new HashSet<>();
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            PackageManager mPackageManager = getActivity().getPackageManager();

            Set<String> entries = mSharedPref.getStringSet(SettingsFragment.BLUETOOTH_SELECTAPPS, null);
            if (entries == null) {
                mWhiteListEntries = new HashSet<>();
            } else {
                mWhiteListEntries = new HashSet<>(entries);
            }
            List<ApplicationInfo> pkgs = mPackageManager.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo pkg : pkgs) {
                AppPreference pref = new AppPreference(getActivity());
                pref.setTitle(mPackageManager.getApplicationLabel(pkg));
                Drawable icon = pkg.loadIcon(mPackageManager);
                pref.setPkgName(pkg.packageName);
                if (mWhiteListEntries.contains(pkg.packageName)) {
                    pref.setDefaultValue(true);
                    isSelected.add(pref.getPkgName());
                } else {
                    pref.setDefaultValue(false);
                    icon.setColorFilter(mGrayscaleFilter);
                }
                pref.setIcon(icon);
                prefs.add(pref);
            }

            Collections.sort(prefs, (a, b) -> a.getTitle().toString().toLowerCase().compareTo(b.getTitle().toString().toLowerCase()));

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putStringSet(SettingsFragment.BLUETOOTH_SELECTAPPS, isSelected);
            editor.apply();


            //TEMP
            SharedPreferences.Editor ed2 = mSharedPref.edit();
            ed2.putStringSet("allowedapplist", new HashSet<>()).apply();
            //

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            addPreferencesFromResource(org.groebl.sms.R.xml.settings_bluetooth_apps);
            mWhiteList = (PreferenceCategory) findPreference(getString(org.groebl.sms.R.string.cat_applist));
            mWhiteList.setTitle(org.groebl.sms.R.string.pref_bluetooth_apps_title);

            for (AppPreference pref : prefs) {
                mWhiteList.addPreference(pref);
            }


            if (pDialog.isShowing()){
                pDialog.dismiss();
            }

            super.onPostExecute(result);
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
        boolean isWhiteListed = newlist.contains(pkg);
        if (disabled && !isWhiteListed) {
            return;
        } else if (disabled) { //
            newlist.remove(pkg);
        } else if (!disabled && isWhiteListed) {
            return;
        } else if (!disabled) {
            newlist.add(pkg);
        }


        mWhiteListEntries = new HashSet<>(newlist);
        SharedPreferences.Editor editor = mSharedPref.edit();
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