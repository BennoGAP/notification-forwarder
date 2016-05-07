package org.groebl.sms.service;

import android.app.IntentService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.groebl.sms.R;
import org.groebl.sms.transaction.SmsHelper;
import org.groebl.sms.ui.view.RunnableToast;

public class CopyUnreadMessageTextService extends IntentService {

    public static String EXTRA_THREAD_URI = "threadUri";

    private Handler mHandler;

    public CopyUnreadMessageTextService() {
        super("CopyUnreadMessageTextService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri threadUri = intent.getParcelableExtra(EXTRA_THREAD_URI);

        String messages = SmsHelper.getUnreadMessageText(this, threadUri);

        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(messages, messages));
        mHandler.post(new RunnableToast(this, R.string.toast_copy_text, Toast.LENGTH_SHORT));
    }
}
