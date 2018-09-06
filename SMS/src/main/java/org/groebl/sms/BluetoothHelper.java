package org.groebl.sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.provider.Telephony;
import android.util.Log;

import org.groebl.sms.transaction.SmsHelper;

import java.util.HashSet;
import java.util.Set;


public class BluetoothHelper {

    public static final int BT_ERROR_CODE = 777;
    public static final int BT_ERROR_CODE_WA = 778;

    private static final String TAG = "BTSMSHelper";


    public static void deleteBluetoothMessages(Context context, boolean afterTime) {
        Log.d(TAG, "Deleting temporary Bluetooth messages");
        String selection = "";

        if(afterTime) { selection = " AND " + Telephony.Sms.DATE_SENT + " < " + (System.currentTimeMillis()-21600000); }

        context.getContentResolver().delete(Telephony.Sms.CONTENT_URI, Telephony.Sms.ERROR_CODE + " = ? or " + Telephony.Sms.ERROR_CODE + " = ? " + selection, new String[]{Integer.toString(BT_ERROR_CODE), Integer.toString(BT_ERROR_CODE_WA)});
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
        String enabledNotificationListeners = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");

        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(context.getPackageName())) {
            return false;
        } else {
            return true;
        }
    }

    public static Set<String> getBluetoothConversations(Context mContext) {
        Long threadId;
        Cursor cursor;

        Set<String> bluetoothConversations = new HashSet<>();
        try {
            cursor = mContext.getContentResolver().query(SmsHelper.SMS_CONTENT_PROVIDER, new String[]{SmsHelper.COLUMN_THREAD_ID}, SmsHelper.COLUMN_ERROR_CODE + " = ?", new String[]{Integer.toString(BT_ERROR_CODE)}, null);

            while (cursor.moveToNext()) {
                threadId = cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.COLUMN_THREAD_ID));
                bluetoothConversations.add(threadId.toString());
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bluetoothConversations;
    }
}
