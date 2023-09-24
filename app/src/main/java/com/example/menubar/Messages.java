package com.example.menubar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class Messages extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())){
            for(SmsMessage smsMessage: Telephony.Sms.Intents.getMessagesFromIntent(intent)){
                String message=smsMessage.getMessageBody();
                Toast.makeText(context, "received message :"+message, Toast.LENGTH_SHORT).show();
            }
        }
    }
}