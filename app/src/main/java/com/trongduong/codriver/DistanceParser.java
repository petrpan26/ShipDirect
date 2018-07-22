package com.trongduong.codriver;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by trongduong on 7/21/2018.
 */

public class DistanceParser {
    /**
     * Returns a list of lists containing latitude and longitude from a JSONObject
     */
    public double parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        Double dist = 0.0;

        Log.i("DBG1", jObject.toString());

        try {

            jRoutes = jObject.getJSONArray("routes");

            JSONObject routes2 = jRoutes.getJSONObject(0);

            JSONArray legs = routes2.getJSONArray("legs");

            JSONObject steps = legs.getJSONObject(0);

            JSONObject distance = steps.getJSONObject("distance");

            Log.i("Distance", distance.toString());
            dist = Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]", ""));
            Log.i("Distance", dist.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return dist;
    }
}
