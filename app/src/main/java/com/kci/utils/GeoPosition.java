package com.kci.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.kci.MyApplication;
import com.kci.activity.MainActivity;
import com.kci.activity.MapsActivityCurrentPlace;
import com.kci.base.BaseActivity;
import com.kci.smsreceiver.R;
import com.kci.structure.POSITION;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class GeoPosition  {
    MyApplication app;
    Context mContext;
    String mcontent;
    String msender;
    long mdate;
    Intent mintent;
    public void init(Context context,Intent intent,String content, String sender, long date){

        mContext = context;
        mcontent = content ;
        msender = sender;
        mintent = intent;
        mdate = date;
        app = (MyApplication)mContext.getApplicationContext();
        //getPosition();
        getLocationAddress();

    }
    public void getPosition() {
        POSITION curpos = null;


        LatLng curLatLng = new LatLng(app.mCurrentLat,app.mCurrentLng);
        //LatLng curLatLng = new LatLng(app.mCurrentLat+0.01*app.getPositionCount(),app.mCurrentLng);//testing
        POSITION befPos = app.currentPosition();
        if (befPos != null) {
            //if (!(befPos.mPlaceLatLng.latitude == curLatLng.latitude && befPos.mPlaceLatLng.longitude == curLatLng.longitude))
            {
                curpos = new POSITION();
                curpos.mPlaceLatLng = curLatLng;
                getAddress(curpos);

            }

        }
        else {
            curpos = new POSITION();
            curpos.mPlaceLatLng = curLatLng;
            getAddress(curpos);

        }



    }
    private  void getAddress(final POSITION pos){
        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {

                String straddre = "";
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(mContext, Locale.getDefault());



                try{
                    addresses = geocoder.getFromLocation(pos.mPlaceLatLng.latitude, pos.mPlaceLatLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();
                    straddre = address;//+","+city+" "+state+" "+country;
                    pos.mPlaceAddresse = straddre;
                    pos.mPlaceName = knownName;
                    pos.mSender  = msender;
                    pos.mContent = mcontent;
                    pos.mDate = getDate(mdate);
                    pos.mTime = getTime(mdate);
                    app.addPosition(pos);
                    //String msg = getSMSContentAndAddressMsg(pos);
                    //Toast.makeText(app, msg ,Toast.LENGTH_LONG).show();

                    //app.main.refresh_list();
                    return;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(r, 1000);

    }
    public  void getAddressFromMultiPos(final MyApplication arg_app,final List<LatLng> arg_path){
        final Handler handler = new Handler();

        app = arg_app;
        mContext = app.getApplicationContext();
        if(app.m_AddressList.size()>0)
            app.m_AddressList.clear();
        final Runnable r = new Runnable() {
            public void run() {

                String straddre = "";
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(mContext, Locale.getDefault());
                for(int i=0;i<arg_path.size();){
                    LatLng pos = arg_path.get(i);
                    try{
                        straddre = "";
                        addresses = geocoder.getFromLocation(pos.latitude, pos.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        if(addresses.size()>0){

                            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String country = addresses.get(0).getCountryName();
                            String postalCode = addresses.get(0).getPostalCode();
                            String knownName = addresses.get(0).getFeatureName();
                            straddre = address;//+","+city+" "+state+" "+country;

                        }
                        app.m_AddressList.add(straddre);
                        i++;

                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    handler.postDelayed(this, 1000);
                }
                handler.removeCallbacksAndMessages(null);
                return;



            }
        };

        handler.postDelayed(r, 1000);

    }

    private  void getLocationAddress(){
        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                if(mcontent.isEmpty()) return;
                String straddre = "";
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(mContext, Locale.getDefault());
                Boolean isFlag = false;
                int n=0;

                String name = mcontent.replaceAll("\n", " ");
                mcontent = "";
                String init_name = name;
                 while(!isFlag){
                     try{

                         addresses = geocoder.getFromLocationName(name, 10);
                         if(addresses == null) return;
                         if(addresses.size() == 0){
                             name = name +" , 3210 Slovenske Konjice Slovenia ";
                             continue;
                         }
                         n++;
                         POSITION pos = new POSITION();

                         ArrayList<Address > list = new ArrayList<Address>();
                         String dis_address = "";
                         for(int i=0;i<addresses.size();i++){
                             float distance =  distance(app.mCityLat,app.mCityLng,addresses.get(i).getLatitude(),addresses.get(i).getLongitude());
                             distance = distance /1000;//Km
                             if(distance < app.M_MAX_RANGE){//if distance is in range
                                 list.add(addresses.get(i));
                                 String address = addresses.get(i).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                 dis_address = address;
                                 //if match address in sms and address in google, current address is set.
                                 if(address.toLowerCase().contains(init_name.toLowerCase()) || name.toLowerCase().contains(address.toLowerCase())){
                                     String city = addresses.get(i).getLocality();
                                     String state = addresses.get(i).getAdminArea();
                                     String country = addresses.get(i).getCountryName();
                                     String postalCode = addresses.get(i).getPostalCode();
                                     String knownName = addresses.get(i).getFeatureName();
                                     LatLng latLng = new LatLng(addresses.get(i).getLatitude(),addresses.get(0).getLongitude());
                                     pos.mPlaceLatLng = latLng;
                                     straddre = address;//+",Lat:"+latLng.latitude+",Lng: "+latLng.longitude;

                                     pos.mPlaceAddresse = straddre;
                                     pos.mPlaceName = knownName;
                                     pos.mSender  = msender;
                                     pos.mContent = mcontent;
                                     pos.mDate = getDate(mdate);
                                     pos.mTime = getTime(mdate);
                                     app.addPosition(pos);
                                     isFlag = true;
                                     break;
                                 }//if

                             }//if
                         }// for
                         if(isFlag == false ) name = name +" , Slovenske Konjice Slovenia ";//dis_address;//if it is the different after get address,  again get the address
                         if(isFlag == false && n >1){//if there is no match address, set first round address
                             if(list.size()>0){
                                 int i=0;
                                 String address = list.get(i).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                 String knownName = list.get(i).getFeatureName();
                                 LatLng latLng = new LatLng(list.get(i).getLatitude(),list.get(0).getLongitude());
                                 pos.mPlaceLatLng = latLng;
                                 straddre = address;//+",Lat:"+latLng.latitude+",Lng: "+latLng.longitude;

                                 pos.mPlaceAddresse = straddre;
                                 pos.mPlaceName = knownName;
                                 pos.mSender  = msender;
                                 pos.mContent = mcontent;
                                 pos.mDate = getDate(mdate);
                                 pos.mTime = getTime(mdate);
                                 app.addPosition(pos);
                                 isFlag = true;
                             }
                             app.main.refresh_list();
                         }

                         if(isFlag)
                         {
                             mContext.startActivity(mintent);
                             handler.postDelayed(this, 60000);
                             return;
                         }
                         if(n>1){
                             handler.postDelayed(this, 60000);
                             return;
                         }

                     }
                     catch (IOException e) {
                         e.printStackTrace();
                     }

                 }//while
            }
        };

        handler.postDelayed(r, 1000);

    }

    private  String getDate(long date){
        Date date1 = date == 0 ? new Date() : new Date(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(date1);
        return currentDate;
    }

    private  String getTime(long date){
        Date date1 = date == 0 ? new Date() : new Date(date);
        SimpleDateFormat timeInfo = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeInfo.format(date1);
        return currentTime;
    }
    private  String getSMSContentAndAddressMsg(POSITION pos){
        String strMessage = "";
        strMessage = " You Received SMS from " + pos.mSender +" , Message : "+pos.mContent;
        strMessage += " Address : " + pos.mPlaceAddresse;
        return strMessage;
    }
    public float distance (double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;// Radius in Kilometers default
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();//meter
    }

}
