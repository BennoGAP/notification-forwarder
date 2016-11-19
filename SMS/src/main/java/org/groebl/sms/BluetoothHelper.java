package org.groebl.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import org.groebl.sms.data.Message;
import org.groebl.sms.transaction.SmsHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class BluetoothHelper {

    public static final int BT_ERROR_CODE = 777;
    public static final int BT_ERROR_CODE_WA = 778;

    public static final String BT_TEMP_SELECTION = SmsHelper.COLUMN_ERROR_CODE + " = " + Integer.toString(BT_ERROR_CODE);
    public static final String BT_TEMP_SELECTION_WA = SmsHelper.COLUMN_ERROR_CODE + " = " + Integer.toString(BT_ERROR_CODE_WA);

    private static final String TAG = "BTSMSHelper";


    public static List<Message> deleteBluetoothMessages(Context context, boolean afterTime) {
        Log.d(TAG, "Deleting temporary Bluetooth messages");
        Cursor cursor = null;
        List<Message> messages = new ArrayList<>();
        String selection;

        if(afterTime) { selection = "(" + BT_TEMP_SELECTION + " OR " + BT_TEMP_SELECTION_WA + ") AND date_sent < " + (System.currentTimeMillis()-21600000); } else { selection = BT_TEMP_SELECTION + " OR " + BT_TEMP_SELECTION_WA; }

        try {
            cursor = context.getContentResolver().query(SmsHelper.SMS_CONTENT_PROVIDER, new String[]{SmsHelper.COLUMN_ID}, selection, null, SmsHelper.sortDateDesc);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                messages.add(new Message(context, cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_ID))));
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (Message m : messages) {
            m.delete();
        }
        return messages;
    }

    public static Uri addMessageToInboxAsRead(Context context, String address, String body, long senttime, boolean asRead, int errorCode) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();

        cv.put("address", address);
        cv.put("body", body);
        cv.put("date_sent", senttime);
        cv.put("seen", true);
        cv.put("error_code", errorCode);
        if(asRead) {
            cv.put("read", true);
        }

        return contentResolver.insert(SmsHelper.RECEIVED_MESSAGE_CONTENT_PROVIDER, cv);
    }

    public static boolean hasNotificationAccess(Context context) {
        //NotificationManagerCompat.from(context).areNotificationsEnabled() //todo: does this work?!
        return Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners").contains(context.getPackageName());
    }

    public static Set<String> getBluetoothConversations(Context mContext) {
        Long threadId;
        Cursor cursor;

        Set<String> bluetoothConversations = new HashSet<String>();
        try {
            cursor = mContext.getContentResolver().query(SmsHelper.SMS_CONTENT_PROVIDER, new String[]{SmsHelper.COLUMN_THREAD_ID}, BluetoothHelper.BT_TEMP_SELECTION, null, null);

            while (cursor.moveToNext()) {
                threadId = cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_THREAD_ID));
                bluetoothConversations.add(threadId.toString());
            }

            cursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        return bluetoothConversations;
    }
}
