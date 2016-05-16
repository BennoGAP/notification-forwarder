package org.groebl.sms;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import java.util.Set;

import org.groebl.sms.transaction.SmsHelper;
import org.groebl.sms.ui.settings.SettingsFragment;

import com.vdurmont.emoji.EmojiParser;



public class BluetoothNotificationService extends NotificationListenerService {

    private Context context;
    private String last_msg = "";
    private Long time_last_msg = System.currentTimeMillis()-15000;

    private boolean isPhoneNumber(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        char c = name.charAt(0);
        return !name.contains("@") && !name.matches(".*[a-zA-Z]+.*") && (c == '+' || c == '(' || Character.isDigit(c));
    }

    private String removeDirectionChars(String text) {
        return text.replaceAll("[\u202A|\u202B|\u202C|\u200B]", "");
    }



    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        //Global enabled
        if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_ENABLED, false)) {

            //Only when connected to BT
            if ((mPrefs.getBoolean(SettingsFragment.BLUETOOTH_CONNECTED, false) && BluetoothReceiver.BTconnected) ||
                    !mPrefs.getBoolean(SettingsFragment.BLUETOOTH_CONNECTED, false)) {

                //Only for -clearable- Notifications
                if (sbn.isClearable())
                {
                    String pack = sbn.getPackageName();
                    Boolean whitelist = false;

                    //Only for selected apps
                    Set<String> appwhitelist = mPrefs.getStringSet(SettingsFragment.ALLOWED_APPS, null);
                    if (appwhitelist.contains(pack)) { whitelist = true; }

                    String set_sender = "";
                    String set_content = "";
                    String ticker = "";
                    String title = "";
                    String text = "";
                    String summary = "";
                    Integer errorCode = SmsHelper.BT_ERROR_CODE;

                    //If everything is fine and msg not too old
                    if (whitelist && sbn.getNotification().when > time_last_msg) {
                        Bundle extras = sbn.getNotification().extras;

                        if (sbn.getNotification().tickerText != null) {
                            ticker = removeDirectionChars(sbn.getNotification().tickerText.toString());
                        }

                        if (extras.getCharSequence(Notification.EXTRA_TITLE) != null) {
                            title = removeDirectionChars(extras.getCharSequence(Notification.EXTRA_TITLE).toString());
                        }

                        if (extras.getCharSequence(Notification.EXTRA_TEXT) != null) {
                            text = removeDirectionChars(extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                        }

                        switch(pack) {
                            case "org.telegram.messenger":
                                if (ticker.equals("")) { break; }

                                set_sender = "Telegram";
                                set_content = ticker;
                                break;

                            case "ch.threema.app":
                                if (ticker.equals("")) { break; }

                                set_sender = "Threema";
                                set_content = ticker;
                                break;

                            case "com.google.android.gm":
                                if (title.matches("^[0-9]*\\u00A0.*$")) { break; }

                                set_sender = "E-Mail";
                                set_content = title + ": " + text;
                                break;

                            case "com.whatsapp":
                                if(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT) != null) {
                                    summary = removeDirectionChars(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT).toString());
                                }

                                if (text.equals(summary)) { break; }

                                if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_WHATSAPP_MAGIC, false)) {

                                    String WA_grp = "";
                                    String WA_name = "";
                                    String WA_msg = "";
                                    String phoneNumber = "";

                                    errorCode = SmsHelper.BT_ERROR_CODE_WA;

                                    //Yeah, here happens magic and stuff  ¯\_(ツ)_/¯
                                    if (ticker.endsWith(" @ " + title) && text.contains(": ")) {
                                        WA_grp = title;
                                        WA_name = text.substring(0, text.indexOf(": "));
                                        WA_msg = text.substring(text.indexOf(": ") + 2, text.length());
                                        //title: GRUPPE // txt: NAME: NACHRICHT
                                        //ticker: Nachricht von NAME @  GRUPPE
                                    } else if (title.contains(" @ ")) {
                                        WA_grp = title.substring(title.indexOf(" @ ") + 3, title.length());
                                        WA_name = title.substring(0, title.indexOf(" @ "));
                                        WA_msg = text;
                                        //title: NAME @ GRUPPE //txt: NACHRICHT
                                    } else {
                                        WA_grp = "";
                                        WA_name = title;
                                        WA_msg = text;
                                    }

                                    //Check if the Name is just a Number or a Name we can search for in the Phonebook
                                    if(isPhoneNumber(WA_name)) {
                                        set_sender = WA_name;
                                    } else {
                                        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                new String[] {"data1"},
                                                "display_name = ? AND account_type = ?",
                                                new String[] {WA_name, "com.whatsapp"},
                                                null);

                                        if (c.moveToFirst()) { phoneNumber = c.getString(0); }
                                        if (c != null && !c.isClosed()) { c.close(); }

                                        //Check if everything went fine, otherwise back to the roots (╯°□°）╯︵ ┻━┻
                                        if (phoneNumber.equals("") && !isPhoneNumber(phoneNumber)) {
                                            set_sender = "WhatsApp";
                                            set_content = title + ": " + text;
                                            errorCode = SmsHelper.BT_ERROR_CODE;
                                        } else {
                                            set_sender = phoneNumber;
                                        }
                                    }

                                    //Check if neccessary (see above) // Personal Msg or Group-Chat Msg
                                    if (set_content.equals("")) {
                                        if (WA_grp.equals("")) {
                                            set_content = "WA: " + WA_msg;
                                        } else {
                                            set_content = "WA @ " + WA_grp + ": " + WA_msg;
                                        }
                                    }

                                } else {
                                    set_sender = "WhatsApp";
                                    set_content = title + ": " + text;
                                }

                                break;

                            default:
                                if (!pack.equalsIgnoreCase(getPackageName()) && !pack.equalsIgnoreCase("android")) {
                                    PackageManager pm = getApplicationContext().getPackageManager();
                                    ApplicationInfo ai;

                                    try {
                                        ai = pm.getApplicationInfo(pack, 0);
                                        set_sender = pm.getApplicationLabel(ai).toString();
                                    } catch (PackageManager.NameNotFoundException e) {
                                        set_sender = null;
                                    }

                                    set_content = (ticker.equals("") ? title + ": " + text : ticker);
                                }
                        }

                        if (!set_sender.equals("") && !set_content.equals("") && !set_content.equals(last_msg)) {
                            time_last_msg = sbn.getNotification().when;
                            last_msg = set_content;

                            //Only if Enabled and if WA-Special-Magic-Stuff is not used
                            if (!mPrefs.getBoolean(SettingsFragment.BLUETOOTH_SHOWNAME, false) && errorCode.equals(SmsHelper.BT_ERROR_CODE)) {
                                set_content = set_sender + ": " + set_content;
                                set_sender  = "0049987654321";
                            }

                            set_sender  =  set_sender.substring(0, Math.min(set_sender.length(), 49));
                            set_content = set_content.substring(0, Math.min(set_content.length(), 999));
                            Long senttime = System.currentTimeMillis();

                            //Enter the Data in the SMS-DB
                            SmsHelper.addMessageToInboxAsRead(context, EmojiParser.removeAllEmojis(set_sender), EmojiParser.parseToAliases(set_content, EmojiParser.FitzpatrickAction.REMOVE), senttime, (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MARKREAD, false) && !mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MARKREAD_DELAYED, false)), errorCode);

                            //Delayed Mark-as-Read
                            if(mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MARKREAD, false) && mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MARKREAD_DELAYED, false)) {
                                ContentValues cv = new ContentValues();
                                cv.put("read", true);

                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(() -> context.getContentResolver().update(SmsHelper.RECEIVED_MESSAGE_CONTENT_PROVIDER, cv, SmsHelper.COLUMN_DATE_SENT + " = " + senttime + " AND (" + SmsHelper.COLUMN_ERROR_CODE + " = " + SmsHelper.BT_ERROR_CODE + " OR " + SmsHelper.COLUMN_ERROR_CODE + " = " + SmsHelper.BT_ERROR_CODE_WA + ")", null), 500);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d("SMS", "onNotificationRemoved");
    }
}
