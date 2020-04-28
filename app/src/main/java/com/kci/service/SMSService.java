package com.kci.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.kci.activity.MainActivity;
import com.kci.base.SMSInterface;
import com.kci.network.ApiHandler;
import com.kci.network.ApiParameter;
import com.kci.network.NetworkTask;
import com.kci.smsreceiver.R;
import com.kci.smsreceiver.SmsReceiver;
import com.kci.utils.JSonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class SMSService extends Service implements SMSInterface{
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private String myphone = "";
    private HashMap<String, String> authParam;
    public ApiHandler mApiHandler = new ApiHandler();
    private boolean isNetworkTaskCompleted = false;
    private int tryCount = 0;
    Handler mHandler;
    Runnable mHandlerTask ;
    private ArrayList<HashMap<String, String>> paramArray = new ArrayList<>();

    private SmsReceiver smsReceiver;

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        smsReceiver = new SmsReceiver();
        registerReceiver(smsReceiver, new IntentFilter(SMS_RECEIVED));
        smsReceiver.setOnSmsReceiveListener(this);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            myphone = "Cannot Read Phone number";
        }
        else{
            myphone = telephonyManager.getLine1Number();
        }

        initHandlerTask();
    }

    private void initHandlerTask(){
        mHandler = new Handler();
        tryCount = 0;
        mHandlerTask = new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(mHandlerTask, 1000 * 15);
                if(isNetworkTaskCompleted){
                    tryCount++;
                    sendAuthMessage();
                    isNetworkTaskCompleted = false;
                }
            }
        };
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForground();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForground(){
        Intent notificationIntent  = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(NOTIF_CHANNEL_ID);
            if(channel == null)
            {
                channel = new NotificationChannel(NOTIF_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
            Notification notification = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID).setContentIntent(pendingIntent).build();
            startForeground(NOTIF_ID, notification);
        }else{

        }
    }

    @Override
    public void onSMSReceived(String content, String sender, long date) {
        Date date1 = date == 0 ? new Date() : new Date(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(date1);
        SimpleDateFormat timeInfo = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeInfo.format(date1);


        HashMap<String, String> param = new HashMap<>();

        param.put(ApiParameter.API_REQ_PHONE_NUMBER, myphone);
        param.put(ApiParameter.API_REQ_SENT_NO, sender);
        param.put(ApiParameter.API_REQ_SENT_DATE, currentDate);
        param.put(ApiParameter.API_REQ_SENT_TIME, currentTime);
        param.put(ApiParameter.API_REQ_SMS_CONTENT, content);

        paramArray.add(param);

        if (authParam == null) {
            authParam = paramArray.remove(0);
            sendAuthMessage();
        }
    }

    private void sendAuthMessage(){
        NetworkTask task = new NetworkTask(ApiParameter.SERVER_ADDR, true,
                ApiParameter.CODE_AUTH_MSG, R.string.load_txt_auth, mApiHandler, this);
        mApiHandler.setTargetService(ApiParameter.CODE_AUTH_MSG, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, authParam);
    }

    public void onNetworkTaskCompleted(String response, int msgCode) {
        if(msgCode == ApiParameter.CODE_AUTH_MSG){
            try {
                JSONObject jsonObject = new JSONObject(response);
                String status = JSonParser.getString(jsonObject, ApiParameter.API_RESP_STATUS);
                if(status.equalsIgnoreCase("0"))
                {
                    // to do here add code
                    showToast(R.string.txt_success_auth);
                    tryCount = 0;
                    mHandler.removeCallbacks(mHandlerTask);
                    if (paramArray.size() > 0) {
                        authParam = paramArray.remove(0);
                        sendAuthMessage();
                        isNetworkTaskCompleted = false;
                    } else {
                        authParam = null;
                    }
                    return;
                }else{
                    showToast(R.string.txt_fail_auth);
                    isNetworkTaskCompleted = true;
                    if(tryCount == 0){
                        mHandlerTask.run();
                    }else if(tryCount >= 6){
                        mHandler.removeCallbacks(mHandlerTask);
                        if (paramArray.size() > 0) {
                            authParam = paramArray.remove(0);
                            sendAuthMessage();
                            isNetworkTaskCompleted = false;
                        } else {
                            authParam = null;
                        }
                    }
                    return;
                }
            }catch (JSONException e){
                isNetworkTaskCompleted = true;
                showToast("Cannot connect to server.");
                if (tryCount == 0) {
                    mHandlerTask.run();
                }
            }
        }
    }

    public void showToast(String strMessage) {
        Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}
