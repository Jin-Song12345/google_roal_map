package com.kci;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.kci.activity.MainActivity;
import com.kci.service.GoogleService;
import com.kci.service.SMSService;
import com.kci.structure.POSITION;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class MyApplication extends Application {

    public static final int M_MAX_ENTRIES = 10;
    public static final float M_MAX_RANGE = 500;//km
    POSITION[] m_PlaceList;
    public int nIndex = -1;
    int nCount = 0;
    public boolean mLocationPermissionGranted;
    public final String Location_Update_Action = "com.kci.receiver.updatelocation";
    public Double mCityLat = 46.3376853;//3210 Slovenske Konjice Slovenia
    public Double mCityLng = 15.4118707;
    public Double mCurrentLat = 0.0;
    public Double mCurrentLng = 0.0;
    public POSITION mCurPos ;
    public List<String> m_AddressList;
    public MainActivity main;
    @Override
    public void onCreate() {
        super.onCreate();
        m_PlaceList = new POSITION[M_MAX_ENTRIES];
        //mCurPos = new POSITION();
        startService();
        m_AddressList = new ArrayList<String>();
        mCurPos = null;
    }
    public void startService(){
        Intent smsserviceIntent = new Intent(this, SMSService.class);
        Intent locationserviceIntent = new Intent(this, GoogleService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(smsserviceIntent);
            //startForegroundService(locationserviceIntent);
        }else{
            startService(smsserviceIntent);
            //startService(locationserviceIntent);
        }

    }
    public POSITION getPosition(int i) {

        return m_PlaceList[i];
    }

    public void setCurrentPosition(POSITION pos) {

        mCurPos = pos;


    }
    public void addPosition(POSITION pos) {
        if(nCount >= M_MAX_ENTRIES){
            nIndex = -1;
            nCount= 0 ;
        }
        nCount ++;
        nIndex ++;
        m_PlaceList[nIndex] = pos;
        mCurPos = pos;


    }
    public int getPositionCount() {
        return nCount;
    }
    public POSITION currentPosition() {
        return mCurPos;
    }
}