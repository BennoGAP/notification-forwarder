package org.groebl.smsmms.transaction;

import android.content.Context;
import android.util.Log;

import org.groebl.smsmms.mmssms.Message;
import org.groebl.smsmms.mmssms.Transaction;
import com.mariussoft.endlessjabber.sdk.IEndlessJabberImplementation;
import com.mariussoft.endlessjabber.sdk.MMSPart;
import org.groebl.smsmms.data.ConversationLegacy;

public class EndlessJabber implements IEndlessJabberImplementation {
    private final String TAG = "EndlessJabber";

    public EndlessJabber() {

    }

    @Override
    public void UpdateReadMessages(Context context, long time, int threadId) {
        Log.d(TAG, "UpdateReadMessages");
        new ConversationLegacy(context, threadId).markRead();
    }

    @Override
    public void DeleteThread(Context context, int threadId) {
        Log.d(TAG, "DeleteThread");
        new ConversationLegacy(context, threadId).delete();
    }

    @Override
    public void SendMMS(Context context, String[] recipients, MMSPart[] parts, String subject, boolean save, boolean send) {
        Log.d(TAG, "SendMMS");
        Transaction sendTransaction = new Transaction(context, SmsHelper.getSendSettings(context));

        Message message = new Message();
        message.setType(Message.TYPE_SMSMMS);
        message.setAddresses(recipients);
        message.setSubject(subject);
        message.setSave(save);

        sendTransaction.sendNewMessage(message, 0);
    }

    @Override
    public void SendSMS(Context context, String[] recipients, String body, boolean send) {
        Log.d(TAG, "SendSMS");
        Transaction sendTransaction = new Transaction(context, SmsHelper.getSendSettings(context));

        Message message = new Message();
        message.setType(Message.TYPE_SMSMMS);
        message.setAddresses(recipients);
        message.setText(body);

        sendTransaction.sendNewMessage(message, 0);
    }
}
