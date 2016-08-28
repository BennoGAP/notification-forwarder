package org.groebl.sms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class BluetoothDatabase {

    private static final String DATABASE = "ForwardMessages.db";
    private static final String TABLE = "messages";


    public static void init(Context context) {
        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE + " (app STRING(70) NOT NULL, hash STRING(32) NOT NULL, time DATE DEFAULT (datetime('now', 'localtime')) );";

        try {
            db.execSQL(CREATE_TABLE);
        } catch (SQLiteException e) {
            //catch
        }
        db.close();

    }

    public static void deleteAll(Context context) {
        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE, SQLiteDatabase.CREATE_IF_NECESSARY, null);
        db.delete(TABLE, null, null);
        db.close();
    }

    public static void deleteAfterTime(Context context) {
        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE, SQLiteDatabase.CREATE_IF_NECESSARY, null);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.HOUR_OF_DAY, -12);

        db.execSQL("DELETE from " + TABLE + " WHERE time <= " + "'" + dateFormat.format(c.getTime()) + "';");
        db.close();
    }

    public static boolean searchHash(Context context, String app, String hash) {
        SQLiteDatabase db = context.openOrCreateDatabase(DATABASE, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        new Thread(() -> {
            deleteAfterTime(context);
        }).start();

        try {
            //Check if hash is in Database
            Cursor c  = db.rawQuery("select * from " + TABLE + " where app = ? AND hash = ?", new String[]{app, hash});

            if (c.getCount() > 0) {
                //In Database, return true
                db.close();
                return true;
            } else {
                //Not in Database, so write it in DB and return false
                ContentValues values = new ContentValues();
                values.put("app", app);
                values.put("hash", hash);

                try {
                    db.insertOrThrow(TABLE, null, values);
                } catch (Exception e) {
                    //catch code
                }

                db.close();
                return false;
            }

        } catch (SQLiteException e) {
            return false;
        }

    }

}
