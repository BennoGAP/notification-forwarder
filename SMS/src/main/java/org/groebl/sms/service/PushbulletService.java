package org.groebl.sms.service;

import org.groebl.sms.mmssms.Message;
import org.groebl.sms.mmssms.Transaction;
import org.groebl.sms.data.ConversationLegacy;
import org.groebl.sms.transaction.NotificationManager;
import org.groebl.sms.transaction.SmsHelper;
import org.groebl.sms.ui.popup.QKReplyActivity;
import com.pushbullet.android.extension.MessagingExtension;

public class PushbulletService extends MessagingExtension {
    private final String TAG = "PushbulletService";

    @Override
    protected void onMessageReceived(String conversationIden, String body) {
        long threadId = Long.parseLong(conversationIden);
        ConversationLegacy conversation = new ConversationLegacy(getApplicationContext(), threadId);

        Transaction sendTransaction = new Transaction(getApplicationContext(), SmsHelper.getSendSettings(getApplicationContext()));
        Message message = new org.groebl.sms.mmssms.Message(body, conversation.getAddress());
        message.setType(org.groebl.sms.mmssms.Message.TYPE_SMSMMS);
        sendTransaction.sendNewMessage(message, conversation.getThreadId());

        QKReplyActivity.dismiss(conversation.getThreadId());

        NotificationManager.update(getApplicationContext());
    }

    @Override
    protected void onConversationDismissed(String conversationIden) {
        long threadId = Long.parseLong(conversationIden);
        ConversationLegacy conversation = new ConversationLegacy(getApplicationContext(), threadId);
        conversation.markRead();
    }

}
