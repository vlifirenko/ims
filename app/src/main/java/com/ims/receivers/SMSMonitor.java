package com.ims.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.telephony.SmsMessage;

import com.ims.content.model.Item;
import com.ims.ui.MainActivity;
import com.ims.ui.SettingsActivity;

import org.androidannotations.annotations.EReceiver;

@EReceiver
public class SMSMonitor extends BroadcastReceiver {

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences(SettingsActivity.PREF_NAME, Context.MODE_PRIVATE);
        if (!settings.getBoolean("trackMessages", false))
            return;

        if (intent != null && intent.getAction() != null &&
                ACTION.compareToIgnoreCase(intent.getAction()) == 0) {
            Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
            SmsMessage[] messages = new SmsMessage[pduArray.length];
            String smsFrom = null;
            for (int i = 0; i < pduArray.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                smsFrom = messages[i].getOriginatingAddress();
            }
            if (MainActivity.service != null) {
                Item item = new Item();
                item.type = Item.TYPE_MESSAGE;
                item.created = messages[0].getTimestampMillis();
                if (messages[0].getMessageBody().length() > 100)
                    item.text = messages[0].getMessageBody().substring(0, 99);
                else
                    item.text = messages[0].getMessageBody();
                if (smsFrom != null)
                    item.extensions.put(Item.KEY_FROM, smsFrom);
                item.extensions.put(Item.KEY_TO, "me");
                try {
                    MainActivity.service.save(item, ".sms", messages[0].getMessageBody());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
