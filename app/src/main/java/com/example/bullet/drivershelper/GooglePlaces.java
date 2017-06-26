package com.example.bullet.drivershelper;


import android.os.AsyncTask;
import android.util.Log;

import com.example.bullet.drivershelper.Entity.Place;
import com.example.bullet.drivershelper.Entity.PlaceRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by bullet on 22.06.2017.
 */

public class GooglePlaces extends AsyncTask<PlaceRequest, Integer, ArrayList<Place>>{

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyDkvPkah1-YV09u9ozLaBKt1C8TqIlZmiY";
    private static final String TYPE_SEARCH = "/search";

    private ArrayList<Place> find(String type, double lat, double lng, int radius) {
        ArrayList<Place> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_SEARCH);
            sb.append(OUT_JSON);
            sb.append("?sensor=false");
            sb.append("&key=" + API_KEY);
            sb.append("&type=" + type);//URLEncoder.encode(keyword, "utf8"));
            sb.append("&location=" + String.valueOf(lat) + "," + String.valueOf(lng));
            sb.append("&radius=" + String.valueOf(radius));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("tom-log", "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e("tom-log", "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("results");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<Place>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                Place place = new Place();
                place.reference = predsJsonArray.getJSONObject(i).getString("reference");
                place.address = predsJsonArray.getJSONObject(i).getString("vicinity");
                place.lat = predsJsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                place.lng = predsJsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                place.name = predsJsonArray.getJSONObject(i).getString("name");
                resultList.add(place);
            }
        } catch (JSONException e) {
            Log.e("tom-log", "Error processing JSON results", e);
        }

        return resultList;
    }

    @Override
    protected ArrayList<Place> doInBackground(PlaceRequest... placeRequests) {
        ArrayList<Place> places = find(placeRequests[0].getType(),placeRequests[0].getLat(),placeRequests[0].getLng(),placeRequests[0].getRadius());
        return places;
    }
}
