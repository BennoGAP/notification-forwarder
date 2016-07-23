package org.groebl.sms.ui.compose;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import org.groebl.sms.R;
import org.groebl.sms.ui.base.QKSwipeBackActivity;

public class ComposeActivity extends QKSwipeBackActivity {

    private ComposeFragment mComposeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_compose);

        FragmentManager fm = getFragmentManager();
        mComposeFragment = (ComposeFragment) fm.findFragmentByTag(ComposeFragment.TAG);
        if (mComposeFragment == null) {
            mComposeFragment = new ComposeFragment();
        }

        fm.beginTransaction()
                .replace(R.id.content_frame, mComposeFragment, ComposeFragment.TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.compose, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
