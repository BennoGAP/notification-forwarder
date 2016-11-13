package org.groebl.sms.ui.welcome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.groebl.sms.R;

public class WelcomeBluetoothFragment extends BaseWelcomeFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_bluetooth, container, false);

        //QKTextView default_app = (QKTextView) view.findViewById(R.id.welcome_bluetooth_default);
        //default_app.setOnClickListener(this);

        //QKTextView notification_allow = (QKTextView) view.findViewById(R.id.welcome_bluetooth_notification);
        //notification_allow.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        /*
        switch (v.getId()) {
            case R.id.welcome_bluetooth_default:
                if (Utils.isDefaultSmsApp(mContext)) {
                    new QKDialog()
                            .setContext(mContext)
                            .setMessage(R.string.bluetooth_alert_already_default)
                            .setPositiveButton(R.string.okay, null)
                            .show();
                } else {
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, mContext.getPackageName());
                    startActivity(intent);
                }
                break;
            case R.id.welcome_bluetooth_notification:
                    new QKDialog()
                            .setContext(mContext)
                            .setMessage(R.string.bluetooth_alert_notificationaccess)
                            .setPositiveButton(R.string.okay, view -> {
                                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));})
                            .setCancelOnTouchOutside(false)
                            .show();
                break;
        }
       */
    }
}
