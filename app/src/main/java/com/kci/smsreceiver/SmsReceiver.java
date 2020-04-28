package com.kci.smsreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.kci.MyApplication;
import com.kci.activity.MainActivity;
import com.kci.activity.MapsActivityCurrentPlace;
import com.kci.base.SMSInterface;
import com.kci.utils.GeoPosition;

public class SmsReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private SMSInterface smsInterface;
    public SmsReceiver() {
    }

    public void setOnSmsReceiveListener(SMSInterface i){ smsInterface = i;}

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals(SMS_RECEIVED)) {
            StringBuilder sms = new StringBuilder();
            Bundle bundle = intent.getExtras();
            long date = 0;
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                String sentNumber = "";
                if (pdusObj != null) {

                    SmsMessage[] messages = new SmsMessage[pdusObj.length];
                    for (int i = 0; i < pdusObj.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    }

                    for (SmsMessage smsMessage : messages) {
                        sms.append(smsMessage.getMessageBody());
                    }

                    if (messages.length > 0) {
                        sentNumber = messages[0].getOriginatingAddress();
                        date = messages[0].getTimestampMillis();
                    }

                    String address = messages[0].getOriginatingAddress();

                    Intent newintent = new Intent(context.getApplicationContext(), MapsActivityCurrentPlace.class);
                    newintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                    GeoPosition geo = new GeoPosition();
                    geo.init(context,newintent,sms.toString(),sentNumber,date);


                }
                //smsInterface.onSMSReceived(sms.toString(), sentNumber, date);
            }
        }
    }
}
