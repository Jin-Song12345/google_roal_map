package com.kci.network;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.kci.service.SMSService;

import java.util.HashMap;

public class ApiHandler extends Handler {

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, SMSService> mMapService = new HashMap<>();

    public void setTargetService(int msgCode, SMSService service) {
        mMapService.put(msgCode, service);
    }

    public SMSService getTargetService(int msgCode) {
        return mMapService.get(msgCode);
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        String response = data.getString("data");

        SMSService service = getTargetService(msg.what);
        if (service != null) {
            service.onNetworkTaskCompleted(response, msg.what);
        }
    }
}