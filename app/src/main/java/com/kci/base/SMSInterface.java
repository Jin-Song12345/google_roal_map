package com.kci.base;

public interface SMSInterface  {
    public void onSMSReceived(String content, String sender, long date);
}
