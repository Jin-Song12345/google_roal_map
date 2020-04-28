package com.kci.activity;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.kci.MyApplication;
import com.kci.base.BaseActivity;
import com.kci.network.ApiParameter;
import com.kci.network.NetworkTask;
import com.kci.smsreceiver.R;
import com.kci.structure.POSITION;
import com.kci.utils.GeoPosition;
import com.kci.utils.SettingsActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivityCurrentPlace extends BaseActivity
        implements OnMapReadyCallback {

    private static final String TAG = MapsActivityCurrentPlace.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    // The entry point to the Places API.
    private PlacesClient mPlacesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;


    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 16;
    private static final int ROAD_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private float m_RealDistance;//Km
    private static DecimalFormat dfLatLng = new DecimalFormat("0.00000");
    private static DecimalFormat df = new DecimalFormat("0.00");
    private static DecimalFormat dfm = new DecimalFormat("0");
    GeoApiContext mGeocontext;
    private int requestCode = 9999;
    //TextView m_txtAddress;
    TextView m_txtDis;
    ImageView m_imgLeftArrow;
    ImageView m_imgRightArrow;
    ImageView m_imgTarget;
    Integer m_PolyIndex = 0;
    Integer m_pathIndex = 0;
    List<LatLng> path = new ArrayList();
    List<LatLng> m_roadpath = new ArrayList();
    GeoPosition m_geo = new GeoPosition();
    Marker  m_disMaker= null;
    Marker  m_myPosmarker= null;
    boolean m_isLeft = false;
    boolean m_isTest = true;
    boolean m_isRun = false;
    boolean m_isStop = false;
    float m_dissum = 0;
    String m_addresstxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_RealDistance = 0;
        // Retrieve location and camera position from saved instance state.
          if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
       // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        m_addresstxt = "";
        //m_txtAddress = (TextView)findViewById(R.id.txtSmsAddress);
        //m_txtAddress.setText("");
        m_txtDis = (TextView)findViewById(R.id.txtDis);
        m_txtDis.setText("");
        m_imgLeftArrow  = (ImageView) findViewById(R.id.imgLeftArrow);
        m_imgRightArrow  = (ImageView) findViewById(R.id.imgRightArrow);
        m_imgTarget  = (ImageView) findViewById(R.id.imgTarget);

        m_imgLeftArrow.setOnClickListener(clickListener);
        m_imgRightArrow.setOnClickListener(clickListener);
        m_imgTarget.setOnClickListener(clickListener);
        m_imgLeftArrow.setEnabled(true);

        m_imgRightArrow.setEnabled(true);
        m_disMaker= null;

        m_isRun = false;

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mGeocontext = new GeoApiContext.Builder()
                //.apiKey(getString(R.string.google_maps_key))
                .apiKey("AIzaSyAWT4tKjeC6fSKGJAHU4qyj5CELMIKXi-0")
                .build();
        //showCurrentPlace();
    }
    View.OnClickListener clickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void onClick(View v) {
            m_dissum = 0;
            if (v.equals(m_imgLeftArrow)) {
                //onClickLeftArrow();
                if(m_isRun){
                    onClickLeftAutoMove();

                    if(m_isStop)
                    {
                        m_imgLeftArrow.setImageDrawable(getDrawable(R.drawable.stop));
                        m_isStop = false;
                    }
                    else{
                        m_imgLeftArrow.setImageDrawable(getDrawable(R.drawable.replay));
                        m_isStop = true;
                    }
                }
                else{
                    m_isStop = false;
                    onClickLeftAutoMove();
                    m_imgLeftArrow.setImageDrawable(getDrawable(R.drawable.stop));
                }

            }
            else if (v.equals(m_imgRightArrow)) {
                if(m_isRun){
                    onClickRightAutoMove();

                    if(m_isStop)
                    {
                        m_imgRightArrow.setImageDrawable(getDrawable(R.drawable.stop));
                        m_isStop = false;
                    }
                    else{
                        m_imgRightArrow.setImageDrawable(getDrawable(R.drawable.replay));
                        m_isStop = true;
                    }


                }
                else{
                    m_isStop = false;
                    onClickRightAutoMove();
                    m_imgRightArrow.setImageDrawable(getDrawable(R.drawable.stop));
                }

            }
            else if (v.equals(m_imgTarget)) {
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(app.currentPosition().mPlaceLatLng, ROAD_ZOOM));
                openRealGoogleMap();
            }
        }
    };
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            if(m_isStop){
                timerHandler.removeCallbacks(timerRunnable);
                return;
            }

            if(m_isLeft)        onClickLeftArrow();
            else        onClickRightArrow();
            //m_imgRightArrow.setEnabled(false);
            //m_imgLeftArrow.setEnabled(false);

            if(m_pathIndex >= path.size()-1 || m_pathIndex < 0){
                m_imgRightArrow.setEnabled(true);
                m_imgLeftArrow.setEnabled(true);
                m_imgLeftArrow.setImageDrawable(getDrawable(R.drawable.left_arrow));
                m_imgRightArrow.setImageDrawable(getDrawable(R.drawable.right_arrow));
                timerHandler.removeCallbacks(timerRunnable);
                m_isRun = false;
            }

            timerHandler.postDelayed(this, 100);
        }
    };
    private void onClickRightAutoMove(){
        if(m_isRun){

        }
        else{
            m_PolyIndex = 1;
            m_pathIndex = 0;
            m_isRun = true;
        }
        m_isLeft = false;
        timerHandler.postDelayed(timerRunnable, 1000);
    }
    private void onClickLeftAutoMove(){

        if(m_isRun){

        }
        else{
            m_PolyIndex = m_roadpath.size()-2;
            m_pathIndex = path.size() -1;
            m_isRun = true;
        }
        m_isLeft = true;

        timerHandler.postDelayed(timerRunnable, 1000);
    }
    private void onClickRightArrow(){
        if(path.size()<2){
            m_imgLeftArrow.setEnabled(false);
            m_imgRightArrow.setEnabled(false);
            return;
        }
        m_pathIndex++;

        m_imgLeftArrow.setEnabled(false);
        if(m_pathIndex>path.size()-1) return;
        LatLng bef_pos = path.get(m_pathIndex-1);
        LatLng cur_pos = path.get(m_pathIndex);
        if(m_PolyIndex >1 && m_PolyIndex<m_roadpath.size())
        if(cur_pos.latitude == m_roadpath.get(m_PolyIndex).latitude && cur_pos.longitude == m_roadpath.get(m_PolyIndex).longitude)
            m_PolyIndex++;
        setTxtDisandAddress(cur_pos,bef_pos);
        double angle = bearingBetweenLocations(cur_pos,bef_pos);
        if(m_disMaker != null) m_disMaker.setRotation((float) angle);
        moveMap(cur_pos);

    }
    private void onClickLeftArrow(){
        if(path.size()<2){
            m_imgLeftArrow.setEnabled(false);
            m_imgRightArrow.setEnabled(false);
            return;
        }
        m_pathIndex--;
        if(m_pathIndex < 0) return;
        m_imgRightArrow.setEnabled(false);
        LatLng bef_pos = path.get(m_pathIndex+1);
        LatLng cur_pos = path.get(m_pathIndex);
        if(m_PolyIndex >1 && m_PolyIndex<m_roadpath.size())
        if(cur_pos.latitude == m_roadpath.get(m_PolyIndex).latitude && cur_pos.longitude == m_roadpath.get(m_PolyIndex).longitude)
            m_PolyIndex--;
        setTxtDisandAddress(cur_pos,bef_pos);
        double angle = bearingBetweenLocations(cur_pos,bef_pos);
        if(m_disMaker != null) m_disMaker.setRotation((float) angle);
        moveMap(cur_pos);

    }
    private void setTxtDisandAddress(LatLng cur_pos,LatLng bef_pos){
        m_dissum += m_geo.distance(bef_pos.latitude,bef_pos.longitude,cur_pos.latitude,cur_pos.longitude);

        float dis = m_dissum;
        String unit = " m";
        if(dis > 1000)
        {
            dis = dis/1000; unit = " Km";
            m_txtDis.setText(df.format(dis)+unit);
        }
        else m_txtDis.setText(dfm.format(dis)+unit);


        if(m_PolyIndex<app.m_AddressList.size())
            m_addresstxt = app.m_AddressList.get(m_PolyIndex);
        //else m_txtAddress.setText("");
        if(m_myPosmarker != null && m_myPosmarker.getSnippet().equals(""))
        {
            m_myPosmarker.setSnippet(app.m_AddressList.get(app.m_AddressList.size()-1));
            m_myPosmarker.showInfoWindow();
        }
    }
    private void moveMap(LatLng pos){
        //if(m_disMaker != null) m_disMaker.remove();
        m_disMaker = addText(getApplicationContext(),mMap,pos,(String)m_txtDis.getText(),3,17);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos,
                ROAD_ZOOM));
    }
    private void  openRealGoogleMap(){
        LatLng curpos = new LatLng(app.mCurrentLat,app.mCurrentLng);
        if(m_isTest)
            curpos = new LatLng(app.mCityLat,app.mCityLng);//for testing

        String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+curpos.latitude+","+curpos.longitude
                +"&daddr="+app.currentPosition().mPlaceLatLng.latitude+","+app.currentPosition().mPlaceLatLng.longitude;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(intent, "Select an application"));
        finish();
    }
    private void  openRealGoogleMap_Drive(){
        LatLng curpos = new LatLng(app.mCurrentLat,app.mCurrentLng);
        if(m_isTest)
            curpos = new LatLng(app.mCityLat,app.mCityLng);//for testing

        String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+curpos.latitude+","+curpos.longitude
                +"&daddr="+app.currentPosition().mPlaceLatLng.latitude+","+app.currentPosition().mPlaceLatLng.longitude;
        uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+curpos.latitude+","+curpos.longitude
                +"&daddr="+app.currentPosition().mPlaceLatLng.latitude+","+app.currentPosition().mPlaceLatLng.longitude;

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse(uri+"&dirflg=d"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");
        startActivity(intent);
        finish();
    }
    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                app.mCurrentLat = mLastKnownLocation.getLatitude();
                                app.mCurrentLng = mLastKnownLocation.getLongitude();
                                //openRealGoogleMap();
                            }
                            else{
                                app.mCurrentLat = app.mCityLat;
                                app.mCurrentLng = app.mCityLng;
                            }
                        } else {
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }

                        showCurrentPlace();
                        openRealGoogleMap_Drive();
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.return_menu, menu);
        return true;
    }
    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.return_main) {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();

        }

        return true;
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */


    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        //map.setPadding(10,10,10,10);
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

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
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    updateLocationUI();
                    getDeviceLocation();
                }
            }
        }
        //updateLocationUI();
    }






    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if(app.getPositionCount() == 0) return;
        LatLng markerLatLng ;
        String markerSnippet = "" ;//
        LatLng curpos = new LatLng(app.mCurrentLat,app.mCurrentLng);
        String title = "My Position";
        if(m_isTest)
            curpos = new LatLng(app.mCityLat,app.mCityLng);//for testing
        POSITION pos = app.currentPosition();
        m_myPosmarker = mMap.addMarker(new MarkerOptions()
                            .title(title)
                            .position(curpos)
                            .snippet(markerSnippet));
        if(pos != null) DrawRoadMap(pos.mPlaceLatLng,curpos);
        /*
                    POSITION bef_pos = null;
                    for(int which =0 ;which < app.getPositionCount();which ++){
                        pos = app.getPosition(which);
                        if(bef_pos != null)
                            DrawRoadMap(bef_pos.mPlaceLatLng,pos.mPlaceLatLng);
                        if(which == 0 && app.mCurrentLat!= 0 && app.mCurrentLng!=0)   DrawRoadMap(curpos,pos.mPlaceLatLng);
                        markerLatLng = pos.mPlaceLatLng;
                        markerSnippet = pos.mPlaceAddresse;//

                        // Add a marker for the selected place, with an info window
                        // showing information about that place.
                        String title = "Phone : " + pos.mSender +","+"Message : "+pos.mContent;
                        mMap.addMarker(new MarkerOptions()
                                .title(title)
                                .position(markerLatLng)
                                .snippet(markerSnippet));
                        bef_pos = pos;

                    }*/
                pos = app.currentPosition();
                title = "Phone : " + pos.mSender +","+"Message : "+pos.mContent;
                markerSnippet = "Lat:"+dfLatLng.format(pos.mPlaceLatLng.latitude)+",Lng: "+dfLatLng.format(pos.mPlaceLatLng.longitude);
                markerSnippet += " , R="+ df.format(m_RealDistance)+"Km";
                markerLatLng = pos.mPlaceLatLng;
                m_addresstxt = pos.mContent;
                m_txtDis.setText(df.format(m_RealDistance)+"Km");
                mMap.addMarker(new MarkerOptions()
                        .title(title)
                        .position(pos.mPlaceLatLng)
                        .snippet(markerSnippet)).showInfoWindow();
                    // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                            DEFAULT_ZOOM));




    }
    private void DrawRoadMap(LatLng pos1,LatLng pos2){
        if (mMap == null) {
            return;
        }
        //Define list to get all latlng for the route

        //Execute Directions API request
        path.clear();
        m_PolyIndex = 0;
        DirectionsApiRequest req = DirectionsApi.getDirections(mGeocontext, pos1.latitude+","+pos1.longitude, pos2.latitude+","+pos2.longitude);
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                DirectionsRoute route = res.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));

                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {

                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
            path.add(pos1);
            path.add(pos2);
        }


        // calculate distance
        LatLng bef_pos = null;
        LatLng cur_pos = null;
        List<PatternItem> pattern = Arrays.<PatternItem>asList(
                new Dot(), new Gap(10), new Dash(30), new Gap(10));



        //Draw the polyline
        if (path.size() > 1) {
            cur_pos = path.get(1);
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.rgb(189,231,24)).width(15).pattern(pattern);
            mMap.addPolyline(opts);
        }
        else{
            path.add(pos1);
            path.add(pos2);
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(15).pattern(pattern);;
            mMap.addPolyline(opts);
        }



        m_RealDistance = 0;
        float dis = 0;
        m_roadpath.clear();
        for(int i=0;i<path.size();){
            cur_pos = path.get(i);
            m_roadpath.add(cur_pos);
            if(bef_pos != null)
                dis += m_geo.distance(bef_pos.latitude,bef_pos.longitude,cur_pos.latitude,cur_pos.longitude)/1000;

            while(dis<500){
                i++;

                if(i >= path.size()-1){
                    if(i>path.size()-1) break;
                    bef_pos = path.get(i-1);
                    cur_pos = path.get(i);
                    dis += m_geo.distance(bef_pos.latitude,bef_pos.longitude,cur_pos.latitude,cur_pos.longitude);
                    break;
                }

                bef_pos = path.get(i-1);
                cur_pos = path.get(i);
                dis += m_geo.distance(bef_pos.latitude,bef_pos.longitude,cur_pos.latitude,cur_pos.longitude);
            }
            m_RealDistance += dis/1000;
            dis = 0;

            bef_pos = cur_pos;
        }
        //m_roadpath.add(cur_pos);


        m_geo.getAddressFromMultiPos(app,m_roadpath);
        m_disMaker = addText(getApplicationContext(),mMap,cur_pos,df.format(m_RealDistance)+" Km",3,17);
    }
    public Marker addText(final Context context, final GoogleMap map,
                          final LatLng location, final String text, final int padding,
                          final int fontSize) {
        Marker marker = null;

        if (context == null || map == null || location == null || text == null
                || fontSize <= 0) {
            return marker;
        }

        final TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(fontSize);

        final Paint paintText = textView.getPaint();

        final Rect boundsText = new Rect();
        paintText.getTextBounds(text, 0, textView.length(), boundsText);
        paintText.setTextAlign(Paint.Align.CENTER);

        final Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        final Bitmap bmpText = Bitmap.createBitmap(boundsText.width() + 2
                * padding, boundsText.height() + 2 * padding, conf);

        final Canvas canvasText = new Canvas(bmpText);
        paintText.setColor(Color.rgb(0,0,255));

        canvasText.drawText(text, canvasText.getWidth() / 2,
                canvasText.getHeight() - padding - boundsText.bottom, paintText);
         if(m_disMaker != null)
         {
             m_disMaker.setPosition(location);
             m_disMaker.setTitle(m_addresstxt);
             return m_disMaker;
         }
        final MarkerOptions markerOptions = new MarkerOptions()
                .position(location).title(text)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.road_arrow));
                //.icon(BitmapDescriptorFactory.fromBitmap(bmpText));

        marker = map.addMarker(markerOptions);

        //marker.setRotation(90);
        return marker;
    }
    private double bearingBetweenLocations(LatLng latLng1,LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng+180;
    }
    private void Setting() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }



}
