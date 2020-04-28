package com.kci.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kci.MyApplication;

public class LocationReceiver  extends BroadcastReceiver
{
        @Override
        public void onReceive(Context context, Intent intent) {
            MyApplication app = (MyApplication) context.getApplicationContext();
            if (app.Location_Update_Action.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();

                if (extras != null) {
                    String latutide = extras.getString("latutide");
                    String longitude = extras.getString("longitude");
                    try {

                        app.mCurrentLat = Double.parseDouble(latutide);
                        app.mCurrentLng = Double.parseDouble(longitude);
                    } catch (NumberFormatException e) {
                        // p did not contain a valid double
                    }

                }

             }
        }

}
