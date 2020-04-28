package com.kci.base;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kci.MyApplication;
import com.kci.activity.MainActivity;
import com.kci.network.ApiHandler;


public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    public ApiHandler mApiHandler = new ApiHandler();
    public MyApplication app ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApplication)this.getApplication();
    }

    public void onNetworkTaskCompleted(String response, int msgCode) {}

    public void showToast(String strMessage) {
        Toast.makeText(this, strMessage, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    void gotoHome(){
        startActivity(new Intent(this, MainActivity .class));
    }

}
