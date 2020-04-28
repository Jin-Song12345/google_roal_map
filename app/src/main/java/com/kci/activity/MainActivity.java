package com.kci.activity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kci.adapter.SMSAdapter;
import com.kci.base.BaseActivity;
import com.kci.receiver.LocationReceiver;
import com.kci.smsreceiver.R;
import com.kci.structure.POSITION;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private String[] permissions = { Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS};
    private int requestCode = 9999;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    LocationReceiver location_receiver;
    IntentFilter intentFilter;
    ListView listview;
    ArrayList<POSITION> sms_list;
    SMSAdapter sms_adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPermission();
        getLocationPermission();
        location_receiver = new LocationReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(app.Location_Update_Action);
        registerReceiver(location_receiver, intentFilter);
        init_list();
        app.main = this;

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // Test for the code you have used to start the Activity
        }
    }
    public void refresh_list(){
        sms_list.clear();
        for (int i = 0; i < app.getPositionCount(); ++i) {
            sms_list.add(app.getPosition(i));
        }
        sms_adapter.notifyDataSetChanged();
    }
    private void init_list(){
        listview = (ListView) findViewById(R.id.id_listview);
        sms_list = new ArrayList<POSITION>();
        for (int i = 0; i < app.getPositionCount(); ++i) {
            sms_list.add(app.getPosition(i));
        }
        sms_adapter = new SMSAdapter(getApplicationContext(),
                android.R.layout.simple_list_item_1, sms_list);
        listview.setAdapter(sms_adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final POSITION item = (POSITION) parent.getItemAtPosition(position);
                view.animate().setDuration(300).alpha(0)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                //sms_list.remove(item);
                                sms_adapter.notifyDataSetChanged();
                                app.setCurrentPosition(item);
                                view.setAlpha(1);
                                Intent newintent = new Intent(getApplicationContext(), MapsActivityCurrentPlace.class);
                                newintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(newintent);
                            }
                        });
            }

        });
    }
    private void setPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }

    }
    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        app.mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    app.mLocationPermissionGranted = true;
                }
            }
        }
        //updateLocationUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }
    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            Intent intent = new Intent(getApplicationContext(),MapsActivityCurrentPlace.class);
            startActivityIfNeeded(intent,0);
            finish();
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh_list();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            app.mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

}
