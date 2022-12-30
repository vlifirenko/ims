package com.ims.receivers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.ims.content.model.Item;
import com.ims.ui.MainActivity;
import com.ims.ui.SettingsActivity;

public class SmsObserver extends ContentObserver {

    private Context mContext;

    private String smsBodyStr = "", phoneNoStr = "";
    private long smsDatTime = System.currentTimeMillis();
    static final Uri SMS_STATUS_URI = Uri.parse("content://sms");

    public SmsObserver(Handler handler, Context ctx) {
        super(handler);
        mContext = ctx;
    }

    public boolean deliverSelfNotifications() {
        return true;
    }

    public void onChange(boolean selfChange) {
        SharedPreferences settings = mContext.getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE);
        if (!settings.getBoolean("trackMessages", false))
            return;
        try {
            Cursor sms_sent_cursor = mContext.getContentResolver().query(SMS_STATUS_URI, null, null, null, null);
            if (sms_sent_cursor != null) {
                if (sms_sent_cursor.moveToFirst()) {
                    String protocol = sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("protocol"));
                    if (protocol == null) {
                        int type = sms_sent_cursor.getInt(sms_sent_cursor.getColumnIndex("type"));
                        if (type == 2) {
                            smsBodyStr = sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("body")).trim();
                            phoneNoStr = sms_sent_cursor.getString(sms_sent_cursor.getColumnIndex("address")).trim();
                            smsDatTime = sms_sent_cursor.getLong(sms_sent_cursor.getColumnIndex("date"));

                            if (MainActivity.service != null) {
                                Item item = new Item();
                                item.type = Item.TYPE_MESSAGE;
                                item.created = Long.valueOf(smsDatTime);
                                if (smsBodyStr.length() > 100)
                                    item.text = smsBodyStr.substring(0, 99);
                                else
                                    item.text = smsBodyStr;
                                item.extensions.put(Item.KEY_FROM, phoneNoStr);
                                item.extensions.put(Item.KEY_TO, "me");
                                MainActivity.service.save(item, ".sms", smsBodyStr);
                            }

                            Log.e("Info", "SMS Content : " + smsBodyStr);
                            Log.e("Info", "SMS Phone No : " + phoneNoStr);
                            Log.e("Info", "SMS Time : " + smsDatTime);
                        }
                    }
                }
            } else
                Log.e("Info", "Send Cursor is Empty");
        } catch (Exception sggh) {
            Log.e("Error", "Error on onChange : " + sggh.toString());
        }
        super.onChange(selfChange);
    }

}
