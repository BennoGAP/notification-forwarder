package org.groebl.sms.ui.messagelist;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import org.groebl.sms.R;
import org.groebl.sms.common.ConversationPrefsHelper;
import org.groebl.sms.common.utils.PhoneNumberUtils;
import org.groebl.sms.mmssms.Utils;
import org.groebl.sms.ui.base.QKActivity;
import org.groebl.sms.ui.base.QKSwipeBackActivity;
import org.groebl.sms.ui.dialog.ConversationSettingsDialog;
import org.groebl.sms.ui.dialog.DefaultSmsHelper;
import org.groebl.sms.ui.settings.SettingsFragment;
import org.groebl.sms.ui.welcome.WelcomeActivity;

import java.net.URLDecoder;

public class MessageListActivity extends QKSwipeBackActivity {
    private final String TAG = "MessageListActivity";

    public static final String ARG_THREAD_ID = "thread_id";
    public static final String ARG_ROW_ID = "rowId";
    public static final String ARG_HIGHLIGHT = "highlight";
    public static final String ARG_SHOW_IMMEDIATE = "showImmediate";

    private long mThreadId;
    private long mRowId;
    private String mHighlight;
    private boolean mShowImmediate;

    private long mWaitingForThreadId = -1;

    public static void launch(QKActivity context, long threadId, long rowId, String pattern, boolean showImmediate) {
        Intent intent = new Intent(context, MessageListActivity.class);
        intent.putExtra(ARG_THREAD_ID, threadId);
        intent.putExtra(ARG_ROW_ID, rowId);
        intent.putExtra(ARG_HIGHLIGHT, pattern);
        intent.putExtra(ARG_SHOW_IMMEDIATE, showImmediate);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mThreadId = intent.getLongExtra(ARG_THREAD_ID, -1);
        mRowId = intent.getLongExtra(ARG_ROW_ID, -1);
        mHighlight = intent.getStringExtra(ARG_HIGHLIGHT);
        mShowImmediate = intent.getBooleanExtra(ARG_SHOW_IMMEDIATE, false);

        if (mThreadId == -1 && intent.getData() != null) {
            String data = intent.getData().toString();
            String scheme = intent.getData().getScheme();

            String address = null;
            if (scheme.startsWith("smsto") || scheme.startsWith("mmsto")) {
                address = data.replace("smsto:", "").replace("mmsto:", "");
            } else if (scheme.startsWith("sms") || (scheme.startsWith("mms"))) {
                address = data.replace("sms:", "").replace("mms:", "");
            }

            address = URLDecoder.decode(address);
            address = "" + Html.fromHtml(address);
            address = PhoneNumberUtils.formatNumber(address);
            mThreadId = Utils.getOrCreateThreadId(this, address);
        }

        if (mThreadId != -1) {
            Log.v(TAG, "Opening thread: " + mThreadId);
            FragmentManager fm = getFragmentManager();
            MessageListFragment fragment = (MessageListFragment) fm.findFragmentByTag(MessageListFragment.TAG);
            if (fragment == null) {
                fragment = MessageListFragment.getInstance(mThreadId, mRowId, mHighlight, mShowImmediate);
            }
            mSwipeBackLayout.setScrollChangedListener(fragment);
            FragmentTransaction menuTransaction = fm.beginTransaction();
            menuTransaction.replace(R.id.content_frame, fragment, MessageListFragment.TAG);
            menuTransaction.commit();
        } else {
            String msg = "Couldn't open conversation: {action:";
            msg += intent.getAction();
            msg += ", data:";
            msg += intent.getData() == null ? "null" : intent.getData().toString();
            msg += ", scheme:";
            msg += intent.getData() == null ? "null" : intent.getData().getScheme();
            msg += ", extras:{";
            Object[] keys = intent.getExtras().keySet().toArray();
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i].toString();
                msg += keys[i].toString();
                msg += ":";
                msg += intent.getExtras().get(key);
                if (i < keys.length - 1) {
                    msg += ", ";
                }
            }
            msg += "}}";
            Log.d(TAG, msg);
            makeToast(R.string.toast_conversation_error);
            finish();
        }
    }

    public void getResultForThreadId(long threadId) {
        mWaitingForThreadId = threadId;
    }



    /**
     * When In-App billing is done, it'll return information via onActivityResult().
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConversationSettingsDialog.RINGTONE_REQUEST_CODE) {
            if (data != null) {
                if (mWaitingForThreadId > 0) {
                    ConversationPrefsHelper conversationPrefs = new ConversationPrefsHelper(this, mWaitingForThreadId);
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    conversationPrefs.putString(SettingsFragment.NOTIFICATION_TONE, uri.toString());
                    mWaitingForThreadId = -1;
                }
            }

        } else if (requestCode == WelcomeActivity.WELCOME_REQUEST_CODE) {
            new DefaultSmsHelper(this, R.string.not_default_first).showIfNotDefault(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_list, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
