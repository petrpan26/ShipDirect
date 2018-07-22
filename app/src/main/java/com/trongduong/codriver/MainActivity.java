package com.trongduong.codriver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    GoogleMap mMap;
    private static final int LOCATION_REQUEST = 500;
    public static final int LOCATION_UPDATE_MIN_TIME = 10;
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 5000;

    ArrayList<LatLng> listPoints;


    Button btnShowCoord;
    EditText edtAddress;
    TextView txtCoord;

    Button btnAddAddress;
    Button btnRemoveAddress;
    Button btnStart;

    String assembledString = "";
    View tempView;
    LinearLayout listAddress;
    LinearLayout SieuBuLayout;
    LinearLayout ButtonLayout;
    int num_of_Address = 1;
    Coordinate[] arrayAddress = new Coordinate[100];
    int current_index;
    public double[][] distance_matrix;
    LatLng origin, destination;
    boolean onDistance;
    int org_index, des_index;

    private LocationManager mLocationManager;
    LocationManager locationManager;
    Button btnNext;
    TextView txtview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);

        Arrays.fill(arrayAddress, new Coordinate(0, 0));

        listPoints = new ArrayList<>();

        btnShowCoord = (Button) findViewById(R.id.btnShowCoordinates);
        edtAddress = (EditText) findViewById(R.id.edtAddress);
        txtCoord = (TextView) findViewById(R.id.txtCoordinates);
        SieuBuLayout = (LinearLayout) findViewById(R.id.viewbunhat);

        btnShowCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetCoordinates().execute(edtAddress.getText().toString().replace(" ", "+"));
            }
        });

        Log.i("Vai lon", Double.toString(arrayAddress[0].lat) + Double.toString(arrayAddress[0].lon));

        btnAddAddress = (Button) findViewById(R.id.btnAddAddress);
        btnRemoveAddress = (Button) findViewById(R.id.btnRemoveAddress);
        btnStart = (Button) findViewById(R.id.btnStart);
        listAddress = (LinearLayout) findViewById(R.id.addressList);
        ButtonLayout = (LinearLayout) findViewById(R.id.ButtonLayout);
        btnNext = (Button) findViewById(R.id.btnNext);
        txtview = (TextView) findViewById(R.id.txtOrder);


        btnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = new EditText(MainActivity.this);
                et.setHint("Input address here");
                et.setInputType(InputType.TYPE_CLASS_TEXT);
                listAddress.addView(et);
                num_of_Address++;
            }
        });

        btnRemoveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (num_of_Address > 1) {
                    listAddress.removeViewAt(num_of_Address - 1);
                    num_of_Address--;
                }
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {

            //private ArrayList<Edit> getAllChildren(View v) {

                 /*(!(v instanceof ViewGroup)) {
                    ArrayList<View> viewArrayList = new ArrayList<>();
                    viewArrayList.add(v);
                    return viewArrayList;
                }

                ArrayList<View> result = new ArrayList<>();

                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {

                    View child = vg.getChildAt(i);

                    ArrayList<View> viewArrayList = new ArrayList<View>();
                    viewArrayList.add(v);
                    viewArrayList.addAll(getAllChildren(child));

                    result.addAll(viewArrayList);
                }
                return result;*/


                 /*ArrayList<EditText> myEditTextList = new ArrayList<>();

                for(int i = 0; i < listAddress.getChildCount(); i++)
                    if (listAddress.getChildAt( i ) instanceof EditText)
                        myEditTextList.add( (EditText) listAddress.getChildAt(i));
                return myEditTextList;*/
            //}

            @Override
            public void onClick(View v) {
                btnNext.setVisibility(View.VISIBLE);
                //Get current location
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();

                Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                Coordinate current_location = new Coordinate(location.getLatitude(),location.getLongitude());
                arrayAddress[0] = new Coordinate(location.getLatitude(), location.getLongitude());
                //ArrayList<View> allViews = getAllChildren(SieuBuLayout);

                ArrayList<EditText> myEditTextList = new ArrayList<>();
//                Log.i("số children", String.valueOf(num_of_Address));
//                for(int i = 0; i < num_of_Address; i++)
//                    if (listAddress.getChildAt( i ) instanceof EditText)
//                    {
//                        myEditTextList.add( (EditText) listAddress.getChildAt(i));
//                        current_index = i+1;
//                        if (i>=3) {
//                            new GetCoordinates().execute(((EditText)myEditTextList.get(i)).getText().toString().replace(" ", "+"));
//                        }
//                    }



                for (int i = 1; i<4; i++)
                {
                    if (i==1){
                        arrayAddress[i] = new Coordinate(10.772545, 106.698041);
                        arrayAddress[i].getdata();
                    } else if (i==2){
                        arrayAddress[i] = new Coordinate(10.730269, 106.703612);
                        arrayAddress[i].getdata();
                    } else if (i==3) {
                        arrayAddress[i] = new Coordinate(10.780174, 106.678754);
                        arrayAddress[i].getdata();
                    }
                }


                /*for(int i = 0; i < allViews.size(); i++){
                    tempView = allViews.get(i);
                    if(tempView instanceof EditText){
                        current_index = i;
                        new GetCoordinates().execute(((EditText) tempView).getText().toString().replace(" ", "+"));
                    }
                }*/

                distance_matrix = new double[10][10];
                for (double[] row: distance_matrix)
                    Arrays.fill(row, 0.0);

                /*for (int i=0; i < num_of_Address; i++){
                    for (int j=0; j < num_of_Address; j++) {
                        if (i==j) {
                            distance_matrix[i][j] = 0;
                        } else if (i>j) {
                            distance_matrix[i][j] = distance_matrix[j][i];
                        }
                        else {
                            origin = new LatLng(arrayAddress[i].lat, arrayAddress[i].lon);
                            destination = new LatLng(arrayAddress[j].lat, arrayAddress[j].lon);
                            onDistance = true;
                            org_index = i; des_index = j;
                            String url = getRequestUrl(origin, destination);
                            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                            taskRequestDirections.execute(url);
                        }
                    }
                }*/

                distance_matrix[0][1] = 1;
                distance_matrix[0][2] = 6.6;
                distance_matrix[0][3] = 2;
                distance_matrix[1][2] = 6.7;
                distance_matrix[1][3] = 2.5;
                distance_matrix[2][3] = 7.5;

                for (int i=0; i<=3; i++){
                    for (int j=0; j<=3; j++) {
                        if (i==j) {
                            distance_matrix[i][j] = 0;
                        } else if (i>j) {
                            distance_matrix[i][j] = distance_matrix[j][i];
                        }
                        Log.i("Distance", String.valueOf(distance_matrix[i][j]));
                    }
                }

                for (int i=0; i < num_of_Address; i++) {
                    for (int j=0; j < num_of_Address; j++) {
                        Log.i("Distance", String.valueOf(distance_matrix[i][j]));
                    }
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        mMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria,false));
        Coordinate current_location = new Coordinate(location.getLatitude(),location.getLongitude());

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // Reset marker when 2 already
                if (listPoints.size() == 2) {
                    listPoints.clear();
                    mMap.clear();
                }
                //Save first point select
                listPoints.add(latLng);
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (listPoints.size() == 1) {
                    // Add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }
                else {
                    // Add second marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);

                //TODO: request get direction code below
                if (listPoints.size() == 2) {
                    // Create the URL to get request from first maker to second marker
                    String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
            }
        });
    }

    /*private void getCurrrentLocation() {
        boolean isGPSenabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location location = null;
        if (!(isGPSenabled || isNetworkEnabled))
        {
            Snackbar.make(mMap, R.string.error_location_provider, Snackbar.LENGTH_INDEFINITE).show();;
        } else {
            if (isNetworkEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (isGPSenabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,LOCATION_UPDATE_MIN_TIME, LOCATION_UPDATE_MIN_DISTANCE, mLocationListener);
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }
        if (location != null) {
            Logger.d(String.format("getCurrentLocation(%f, %f)",location.getLatitude(),location.getLongitude()));
        }
    }*/

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("vailon", location.getLatitude() + " " + location.getLongitude());
        //locationText.setText("Latitude: " + location.getLatitude() + "\n Longitude: " + location.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            //locationText.setText(locationText.getText() + "\n"+addresses.get(0).getAddressLine(0)+", "+
                    //addresses.get(0).getAddressLine(1)+", "+addresses.get(0).getAddressLine(2));
            Log.i("vailon", location.getLatitude() + " " + location.getLongitude());
        }catch(Exception e)
        {

        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    private class GetCoordinates extends AsyncTask<String,Void,String> {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait....");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try{
                String address = strings[0];
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s",address);
                response = http.getHTTPData(url);
                return response;
            }
            catch (Exception ex)
            {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                JSONObject jsonObject = new JSONObject(s);

                String lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lat").toString();
                String lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry")
                        .getJSONObject("location").get("lng").toString();

                txtCoord.setText(String.format("Coordinates : %s / %s ",lat,lng));
                arrayAddress[current_index] = new Coordinate(Double.parseDouble(lat),Double.parseDouble(lng));

                if(dialog.isShowing())
                    dialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class Coordinate {
        double lat,lon;
        private Coordinate(double x, double y){
            lat = x;
            lon = y;
        }
        private void getdata(){
        }
    }

    private String getRequestUrl(LatLng origin, LatLng dest) {

        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;

        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;

        //Set value enable the sensor
        String sensor = "sensor=false";

        //Mode for find direction, NOT USED
        String mode = "mode=walking";

        //Adding allternative parameter
        //String alternative = "alternatives=true";

        //Build the full param
        String param = str_org +"&" + str_dest + "&" +sensor; //+ alternative;

        //Output format
        String output = "json";

        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;

        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new  URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            // Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            if (!onDistance) {
                TaskParser taskParser = new TaskParser();
                taskParser.execute(s);
            } else {
                TaskDistanceParser(s);
            }
        }
    }

    public void TaskDistanceParser(String... strings){
        JSONObject jsonObject = null;
        double distance = 0;
        try {
            jsonObject = new JSONObject(strings[0]);
            DistanceParser distanceParser = new DistanceParser();
            distance = distanceParser.parse(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        distance_matrix[org_index][des_index] = distance;
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList<LatLng> points = null;
            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList<LatLng>();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                //polylineOptions.geodesic(true);

                mMap.addPolyline(polylineOptions);
            }

        }
    }

}
