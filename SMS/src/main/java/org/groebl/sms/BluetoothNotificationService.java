package org.groebl.sms;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Intent;
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

import com.vdurmont.emoji.EmojiParser;

import org.groebl.sms.transaction.SmsHelper;
import org.groebl.sms.ui.dialog.BlockedBluetoothDialog;
import org.groebl.sms.ui.settings.SettingsFragment;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;


public class BluetoothNotificationService extends NotificationListenerService {

    private boolean isPhoneNumber(String name) {
        if (TextUtils.isEmpty(name)) { return false; }

        char c = name.charAt(0);
        return !name.contains("@") && !name.matches(".*[a-zA-Z]+.*") && (c == '+' || c == '(' || Character.isDigit(c));
    }

    private String removeDirectionChars(String text) {
        return text.replaceAll("[\u202A|\u202B|\u202C|\u200B]", "");
    }

    private String emojiToNiceEmoji(String text, boolean active) {
        text = text.replaceAll("[\ud83d\ude42]", ":)");
        text = text.replaceAll("[\ud83d\ude0a]", ":)");
        text = text.replaceAll("[\ud83d\ude09]", ";)");
        text = text.replaceAll("[\ud83d\ude00]", ":D");
        text = text.replaceAll("[\ud83d\ude03]", ":D");
        text = text.replaceAll("[\ud83d\ude04]", ":D");
        text = text.replaceAll("[\ud83d\ude2c]", "=D");
        text = text.replaceAll("[\ud83d\ude01]", "=D");
        text = text.replaceAll("[\ud83d\ude0b]", ":P");
        text = text.replaceAll("[\ud83d\ude1b]", ":P");
        text = text.replaceAll("[\ud83d\ude1c]", ";P");
        text = text.replaceAll("[\ud83d\ude1d]", ";P");
        text = text.replaceAll("[\ud83d\ude41]", ":(");
        text = text.replaceAll("[\u2639]", ":(");
        text = text.replaceAll("[\ud83d\ude10]", ":|");
        text = text.replaceAll("[\ud83d\ude11]", ":|");
        text = text.replaceAll("[\ud83d\udc9a]", "<3");
        text = text.replaceAll("[\ud83d\udc9b]", "<3");
        text = text.replaceAll("[\ud83d\udc9c]", "<3");
        text = text.replaceAll("[\ud83d\udc99]", "<3");
        text = text.replaceAll("[\u2764]", "<3");
        text = text.replaceAll("[\ud83d\udc94]", "</3");
        //TODO: find more

        //text = text.replaceAll("[\u25a1]", ""); // [] =>
        /*
            String output = "";
            for (int i = 0; i < text.length(); i++) {
                output = output + ":" + Integer.toHexString(text.charAt(i));
            }
            Log.d("output", output);
        */

        return active ? EmojiParser.parseToAliases(text, EmojiParser.FitzpatrickAction.REMOVE) : text;
    }

    private String notificationHash(String sender, String content) {
        String code = content + " | " + sender;

        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(code.getBytes(), 0, code.length());
            BigInteger big = new BigInteger(1, m.digest());
            return String.format("%1$032x", big);
        } catch (NoSuchAlgorithmException e) {
            return code.substring(0, 31);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d("SMS", "onNotificationPosted");

        //Check if Notification is -clearable-
        if (!sbn.isClearable()) { return; }

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //Check if Bluetooth-Fordward is enabled
        if (!mPrefs.getBoolean(SettingsFragment.BLUETOOTH_ENABLED, false)) { return; }

        //Only when connected to BT
        if ((mPrefs.getBoolean(SettingsFragment.BLUETOOTH_CONNECTED, true) && mPrefs.getBoolean(SettingsFragment.BLUETOOTH_CURRENT_STATUS, false)) ||
            !mPrefs.getBoolean(SettingsFragment.BLUETOOTH_CONNECTED, false))
        {
            String pack = sbn.getPackageName();

            //Only for selected apps
            Set<String> appwhitelist = mPrefs.getStringSet(SettingsFragment.BLUETOOTH_SELECTAPPS, new HashSet<>());
            if (!appwhitelist.isEmpty() && appwhitelist.contains(pack)) {

                String set_sender = "";
                String set_content = "";
                String ticker = "";
                String title = "";
                String text = "";
                String summary = "";
                Integer errorCode = BluetoothHelper.BT_ERROR_CODE;

                Bundle extras = sbn.getNotification().extras;

                if (sbn.getNotification().tickerText != null) {
                    ticker = removeDirectionChars(sbn.getNotification().tickerText.toString());
                }

                String titleExtra = extras.containsKey(Notification.EXTRA_TITLE_BIG)
                        ? Notification.EXTRA_TITLE_BIG : Notification.EXTRA_TITLE;
                if (extras.get(titleExtra) != null) {
                    title = removeDirectionChars(extras.get(titleExtra).toString());
                }

                if (extras.get(Notification.EXTRA_TEXT) != null) {
                    text = removeDirectionChars(extras.get(Notification.EXTRA_TEXT).toString());
                }

                switch(pack) {
                    case "org.telegram.messenger":
                        if (ticker.equals("")) {
                            CharSequence[] textline_telegram = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);

                            if (textline_telegram != null) {
                                ticker = textline_telegram[0].toString();
                            } else {
                                return;
                            }
                        }

                        set_sender = "Telegram";
                        set_content = ticker;
                        break;

                    case "ch.threema.app":
                        if (ticker.equals("")) {
                            return;
                        }

                        set_sender = "Threema";
                        set_content = ticker;
                        break;

                    case "com.skype.raider":
                        if (ticker.equals("")) {
                            return;
                        }

                        if (extras.get(Notification.EXTRA_BIG_TEXT) != null) {
                            ticker = title + ": " + removeDirectionChars(extras.get(Notification.EXTRA_BIG_TEXT).toString());
                        }

                        set_sender = "Skype";
                        set_content = ticker;
                        break;

                    case "com.android.email":
                    case "com.boxer.email":
                        if (text.equals("")) {
                            return;
                        }

                        if (extras.get(Notification.EXTRA_BIG_TEXT) != null) {
                            String text_long_email = removeDirectionChars(extras.get(Notification.EXTRA_BIG_TEXT).toString());

                            if (!text_long_email.equals(text) && !title.equals("")) {
                                set_sender = "E-Mail";
                                set_content = title + ": " + text_long_email;
                            }
                        }
                        break;

                    case "com.google.android.gm":
                        if (title.matches("^[0-9]*\\u00A0.*$")) {
                            return;
                        }

                        if (extras.get(Notification.EXTRA_BIG_TEXT) != null) {
                            text = removeDirectionChars(extras.get(Notification.EXTRA_BIG_TEXT).toString());
                        }

                        set_sender = "E-Mail";
                        if (!title.equals("") && !text.equals("")) {
                            set_content = title + ": " + text;
                        }
                        break;

                    case "com.fsck.k9":
                        if (extras.get(Notification.EXTRA_BIG_TEXT) != null) {
                            ticker = title + ": " + removeDirectionChars(extras.get(Notification.EXTRA_BIG_TEXT).toString());
                        }

                        set_sender = "E-Mail";
                        set_content = ticker;
                        break;

                    case "com.microsoft.office.outlook":
                        //Newest Msg = Last Item in Line; contains: Sender Subject Text
                        CharSequence[] textline_outlook = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                        if (textline_outlook != null) {
                            text = textline_outlook[textline_outlook.length - 1].toString();
                        }

                        set_sender = "E-Mail";
                        set_content = text;
                        break;

                    case "de.web.mobile.android.mail":
                    case "de.gmx.mobile.android.mail":
                    case "com.lenovo.email":
                        if (title.equals("")) {
                            return;
                        }

                        set_sender = "E-Mail";

                        CharSequence[] textline_gmx = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                        if (textline_gmx != null) {
                            set_content = textline_gmx[0].toString();
                        } else if (!title.equals("") && !text.equals("")) {
                            set_content = title + " - " + text;
                        }

                        break;

                    case "com.ebay.mobile":
                        set_sender = "eBay";
                        if (!title.equals("") && !text.equals("")) {
                            set_content = title + ": " + text;
                        } else {
                            set_content = ticker;
                        }
                        break;

                    case "com.google.android.apps.fireball":
                        set_sender = "Allo";
                        CharSequence[] textline_allo = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);

                        if (textline_allo != null) {
                            set_content = textline_allo[0].toString().replaceFirst("\\s\\s", ": ");
                        } else if (!title.equals(ticker)) {
                            set_content = text.replaceFirst("\\s\\s", " in " + title + ": ");
                        } else {
                            set_content = title + ": " + text;
                        }
                        break;

                    case "com.whatsapp":
                        if (extras.get(Notification.EXTRA_SUMMARY_TEXT) != null) {
                            summary = removeDirectionChars(extras.get(Notification.EXTRA_SUMMARY_TEXT).toString());
                        }

                        if (!mPrefs.getBoolean(SettingsFragment.BLUETOOTH_WHATSAPP_OLD, false)) {
                            if (sbn.getTag() != null) { return; }
                        } else {
                            if (removeDirectionChars(text).equals(summary)) { return; }
                        }



                        CharSequence[] textline_whatsapp = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);

                        //if (extras.get(Notification.EXTRA_BIG_TEXT) != null) {
                        //   text = removeDirectionChars(extras.get(Notification.EXTRA_BIG_TEXT).toString());
                        //}

                        if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_WHATSAPP_MAGIC, false)) {

                            String WA_grp = "";
                            String WA_name = "";
                            String WA_msg = "";
                            String phoneNumber = "";

                            //Yeah, here happens magic and stuff  ¯\_(ツ)_/¯
                            if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_WHATSAPP_OLD, false)) {
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
                            } else {
                                if (textline_whatsapp == null && !ticker.equals("") && !text.equals("")) {
                                    if (ticker.endsWith(" @ " + title) && text.contains(": ")) {
                                        WA_grp = title;
                                        WA_name = text.substring(0, text.indexOf(": "));
                                        WA_msg = text.substring(text.indexOf(": ") + 2, text.length());
                                    } else {
                                        WA_grp = "";
                                        WA_name = title;
                                        WA_msg = text;
                                    }
                                } else if (title.equals("WhatsApp")) {
                                    //Nummer = ticker between 202a und 202c
                                    text = removeDirectionChars(textline_whatsapp[textline_whatsapp.length - 1].toString());

                                    if (ticker.contains(" @ ") && text.contains(" @ ") && text.contains(": ")) {
                                        WA_name = text.substring(0, text.indexOf(" @ "));
                                        WA_grp = text.substring(text.indexOf(" @ ") + 3, text.indexOf(": "));
                                        WA_msg = text.substring(text.indexOf(": ") + 2, text.length());
                                    } else {
                                        WA_grp = "";
                                        WA_name = text.substring(0, text.indexOf(": "));
                                        WA_msg = text.substring(text.indexOf(": ") + 2, text.length());
                                    }
                                } else if (textline_whatsapp != null) {
                                    text = removeDirectionChars(textline_whatsapp[textline_whatsapp.length - 1].toString());
                                    if (ticker.endsWith(" @ " + title)) {
                                        WA_grp = title;
                                        WA_name = text.substring(0, text.indexOf(": "));
                                        WA_msg = text.substring(text.indexOf(": ") + 2, text.length());
                                    } else {
                                        WA_grp = "";
                                        WA_name = title;
                                        WA_msg = text;
                                    }

                                }
                            }

                            if (BlockedBluetoothDialog.isWABlocked(mPrefs, WA_grp)) { return; }


                            //Check if the Name is just a Number or a Name we can search for in the Phonebook
                            if (isPhoneNumber(WA_name)) {
                                set_sender = WA_name;
                                errorCode = BluetoothHelper.BT_ERROR_CODE_WA;
                            } else {
                                try {
                                    Cursor c = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            new String[]{"data1"},
                                            "display_name = ? AND account_type = ?",
                                            new String[]{WA_name, "com.whatsapp"},
                                            null);

                                    if (c != null && c.moveToFirst()) {
                                        phoneNumber = c.getString(0);
                                    }
                                    if (c != null && !c.isClosed()) {
                                        c.close();
                                    }

                                    //Check if everything went fine, otherwise back to the roots (╯°□°）╯︵ ┻━┻
                                    if (phoneNumber.equals("")) {
                                        set_sender = "WhatsApp";
                                        if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_WHATSAPP_OLD, false) || textline_whatsapp == null) {
                                            set_content = title + ": " + text;
                                        } else {
                                            set_content = (title.equals("WhatsApp") ? "" : title + ": ") + textline_whatsapp[textline_whatsapp.length - 1].toString();
                                        }
                                    } else {
                                        set_sender = phoneNumber;
                                        errorCode = BluetoothHelper.BT_ERROR_CODE_WA;
                                    }
                                } catch (Exception e) {
                                    set_sender = "WhatsApp";
                                    if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_WHATSAPP_OLD, false) || textline_whatsapp == null) {
                                        set_content = title + ": " + text;
                                    } else {
                                        set_content = (title.equals("WhatsApp") ? "" : title + ": ") + textline_whatsapp[textline_whatsapp.length - 1].toString();
                                    }
                                }
                            }

                            //Check if necessary (see above) // Private Msg or Group-Chat Msg
                            if (set_content.equals("")) {
                                set_content = (WA_grp.equals("") ? WA_msg : EmojiParser.removeAllEmojis(WA_grp) + ": " + WA_msg);
                            }

                        } else {
                            set_sender = "WhatsApp";
                            if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_WHATSAPP_OLD, false) || textline_whatsapp == null) {
                                set_content = title + ": " + text;
                            } else {
                                set_content = (title.equals("WhatsApp") ? "" : title + ": ") + textline_whatsapp[textline_whatsapp.length - 1].toString();
                            }
                        }

                        break;

                    default:
                        if (!pack.equalsIgnoreCase(BuildConfig.APPLICATION_ID) && !pack.equalsIgnoreCase("android")) {
                            PackageManager pm = getApplicationContext().getPackageManager();
                            ApplicationInfo ai;

                            try {
                                ai = pm.getApplicationInfo(pack, 0);
                                set_sender = pm.getApplicationLabel(ai).toString();
                            } catch (PackageManager.NameNotFoundException e) {
                                set_sender = "";
                            }

                            set_content = (ticker.equals("") ? title + ": " + text : ticker);
                        }
                }


                if (!set_sender.equals("") && !set_content.equals("")) {

                    //Only if Enabled and if WA-Special-Magic-Stuff is not used
                    if (!mPrefs.getBoolean(SettingsFragment.BLUETOOTH_SHOWNAME, false) && errorCode.equals(BluetoothHelper.BT_ERROR_CODE)) {
                        set_content = set_sender + ": " + set_content;
                        set_sender  = "0049987654321";
                    }

                    set_sender  =  set_sender.substring(0, Math.min(set_sender.length(), 49));
                    set_content = set_content.substring(0, Math.min(set_content.length(), 999));
                    Long senttime = System.currentTimeMillis();


                    //Check if this msg already exist
                    String current_hash = notificationHash(set_sender, set_content);
                    if (BluetoothDatabase.searchHash(getApplicationContext(), pack, current_hash)) { return; }

                    //Enter the Data in the SMS-DB
                    BluetoothHelper.addMessageToInboxAsRead(getApplicationContext(), EmojiParser.removeAllEmojis(set_sender), emojiToNiceEmoji(set_content, mPrefs.getBoolean(SettingsFragment.BLUETOOTH_EMOJI, true)), senttime, (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MARKREAD, false) && !mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MARKREAD_DELAYED, false)), errorCode);

                    //Delayed Mark-as-Read
                    if (mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MARKREAD, true) && mPrefs.getBoolean(SettingsFragment.BLUETOOTH_MARKREAD_DELAYED, false)) {
                        ContentValues cv = new ContentValues();
                        cv.put("read", true);

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> getApplicationContext().getContentResolver().update(SmsHelper.RECEIVED_MESSAGE_CONTENT_PROVIDER, cv, SmsHelper.COLUMN_DATE_SENT + " = " + senttime + " AND (" + SmsHelper.COLUMN_ERROR_CODE + " = " + BluetoothHelper.BT_ERROR_CODE + " OR " + SmsHelper.COLUMN_ERROR_CODE + " = " + BluetoothHelper.BT_ERROR_CODE_WA + ")", null), 500);
                    }
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d("SMS", "onNotificationRemoved");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
