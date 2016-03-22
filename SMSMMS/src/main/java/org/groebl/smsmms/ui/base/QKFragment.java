package org.groebl.smsmms.ui.base;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import org.groebl.smsmms.QKSMSApp;
import org.groebl.smsmms.common.LiveViewManager;
import org.groebl.smsmms.enums.QKPreference;
import org.groebl.smsmms.ui.ThemeManager;
import com.squareup.leakcanary.RefWatcher;
import icepick.Icepick;

public class QKFragment extends Fragment {

    protected QKActivity mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = (QKActivity) activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LiveViewManager.registerView(QKPreference.BACKGROUND, this, key -> {
            if (getView() != null) {
                getView().setBackgroundColor(ThemeManager.getBackgroundColor());
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = QKSMSApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
