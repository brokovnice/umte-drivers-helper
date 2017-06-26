package com.example.bullet.drivershelper.Fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bullet.drivershelper.CostActivity;
import com.example.bullet.drivershelper.CostsActivity;
import com.example.bullet.drivershelper.Entity.Place;
import com.example.bullet.drivershelper.Entity.PlaceRequest;
import com.example.bullet.drivershelper.GooglePlaces;
import com.example.bullet.drivershelper.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by bullet on 15.06.2017.
 */

public class CostFragment extends Fragment implements LocationListener, /*android.location.LocationListener,*/ GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText etDate, etAmount, etPrice, etNote, etDistance;
    private RatingBar ratingBar;
    private Button btnSave, btnCancel, btnDelete;
    private TextView tvName;
    private String costName;
    private int carId, eventId;
    //private double lat, lng;
    private SQLiteDatabase db;
    private LocationRequest lr;
    private GoogleApiClient googleApiClient;
    private LatLng latLng = null;
   // private Boolean displayAmount;

    //protected LocationManager locationManager;

    private final int MY_PERMISSIONS_REQUEST_LOCATION = 1;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_new_record_values, null, false);

        tvName = (TextView) v.findViewById(R.id.tv_name);
        etDate = (EditText) v.findViewById(R.id.et_date);
        etAmount = (EditText) v.findViewById(R.id.et_amount);
        etPrice = (EditText) v.findViewById(R.id.et_price);
        etNote = (EditText) v.findViewById(R.id.et_note);
        etDistance = (EditText) v.findViewById(R.id.et_distance);
        ratingBar = (RatingBar) v.findViewById(R.id.ratingbar);
        btnCancel = (Button) v.findViewById(R.id.btn_close);
        btnSave = (Button) v.findViewById(R.id.btn_save);
        btnDelete = (Button) v.findViewById(R.id.btn_delete);

        tvName.setText(costName);

        Calendar cal = Calendar.getInstance();

        openDB();

        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.sharedPreferences), Context.MODE_PRIVATE);

        //service - nepotrebujeme nektere pole
        if (costName != null && costName.equals(getResources().getString(R.string.list_item_service))){
            etAmount.setVisibility(View.GONE);
            etDistance.setVisibility(View.GONE);

            //update
            if (eventId > 0){
                Cursor service = db.rawQuery("select date, price, note, rating, lat, lng from service where id = "+eventId,null);
                service.moveToFirst();

                latLng = new LatLng(service.getDouble(4),service.getDouble(5));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Date date = null;
                try {
                    date = sdf.parse(service.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cal.setTime(date);


                etPrice.setText(Double.toString(Math.round(service.getDouble(1)*100)/100) + " " + prefs.getString(getResources().getString(R.string.prefs_currency),""));
                etNote.setText(service.getString(2));
                //svLocation.setVisibility(View.GONE);
                ratingBar.setRating((float)service.getDouble(3));
                service.close();

                etDate.setEnabled(false);
                etPrice.setEnabled(false);
                etNote.setEnabled(false);
                ratingBar.setEnabled(false);

                btnSave.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
            }

        } else {
            //tankovani update
            if (eventId > 0){
                Cursor refuel = db.rawQuery("select date, price, note, rating, amount, distance, lat, lng from refuel where id = "+eventId,null);
                refuel.moveToFirst();

                latLng = new LatLng(refuel.getDouble(6),refuel.getDouble(7));
                Log.d("tom-log","zobraz pocizi " +Double.toString(refuel.getDouble(6)) + " " + refuel.getDouble(7));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                Date date = null;
                try {
                    date = sdf.parse(refuel.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cal.setTime(date);

                //etDate.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + ". " + Integer.toString(cal.get(Calendar.MONTH)+1) + ". " + cal.get(Calendar.YEAR));
                etPrice.setText(Double.toString(Math.round(refuel.getDouble(1)*100)/100) + " " + prefs.getString(getResources().getString(R.string.prefs_currency),""));
                etNote.setText(refuel.getString(2));
                //svLocation.setVisibility(View.GONE);
                ratingBar.setRating((float)refuel.getDouble(3));
                String amount = Double.toString(refuel.getDouble(4)) + "l";
                etAmount.setText(amount);
                String distance = Double.toString(refuel.getDouble(5)) + (prefs.getBoolean(getResources().getString(R.string.prefs_km_mile),true)? " km" : " mile");
                etDistance.setText(distance);
                refuel.close();

                etDate.setEnabled(false);
                etPrice.setEnabled(false);
                etNote.setEnabled(false);
                ratingBar.setEnabled(false);
                etAmount.setEnabled(false);
                etDistance.setEnabled(false);

                btnSave.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
            }
        }

        String month;
        if (String.valueOf(cal.get(Calendar.MONTH)).length() == 1){
            month = "0"+String.valueOf(cal.get(Calendar.MONTH)+1);
        } else {
            month = Integer.toString(cal.get(Calendar.MONTH)+1);
        }
        etDate.setText(cal.get(Calendar.DAY_OF_MONTH) + "." + month + "." + cal.get(Calendar.YEAR));




        /*if (!displayAmount){
            etAmount.setVisibility(View.GONE);
        }*/


        //etPrice.requestFocus();

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (costName.equals(getResources().getString(R.string.list_item_service))) {
                    db.execSQL("delete from car_service where service_id = "+eventId);
                    db.execSQL("delete from service where id = "+eventId);
                } else {
                    db.execSQL("delete from car_refuel where refuel_id = "+eventId);
                    db.execSQL("delete from refuel where id = "+eventId);
                }
                Toast.makeText(getContext(), "Záznam smazán", Toast.LENGTH_LONG).show();
                Context ctx = getActivity();
                if (ctx instanceof CostActivity){
                    ((CostActivity) ctx).goToCosts();
                }
                //getActivity().onBackPressed();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context ctx = getActivity();
                if (ctx instanceof CostActivity){
                    ((CostActivity) ctx).goToCosts();
                }
                //getActivity().onBackPressed();
                //Toast.makeText(getContext(),"souradnice: " + Double.toString(lat) + " " + Double.toString(lng),Toast.LENGTH_LONG).show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidFormat("dd.MM.yyyy", etDate.getText().toString())) {

                    ContentValues values = new ContentValues();

                    if (etPrice.getText().toString().length() == 0) {
                        Toast.makeText(getContext(),"Zadejte cenu",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    values.put("price", Double.parseDouble(etPrice.getText().toString()));
                    values.put("locationname", tvName.getText().toString());

                    SimpleDateFormat originalFormat = new SimpleDateFormat("dd.MM.yyyy");
                    SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date oldDate = null;
                    try {
                        oldDate = originalFormat.parse(etDate.getText().toString());

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    values.put("date", newFormat.format(oldDate));

                    //if (svLocation.isChecked()) {
                    if (latLng == null) {
                        Toast.makeText(getContext(),"Nebyla nalezena GPS poloha. Počkejte prosím",Toast.LENGTH_SHORT).show();
                        return;
                    }
                        values.put("lat", latLng.latitude);
                        values.put("lng", latLng.longitude);
                  /*  } else {
                        values.put("lat", "");
                        values.put("lng", "");
                    }*/

                    values.put("note", etNote.getText().toString());
                    values.put("rating", ratingBar.getNumStars());

                    //Log.d("tom-log","bool: " + Boolean.toString(costName.equals(getResources().getString(R.string.list_item_service))));


                    if (costName.equals(getResources().getString(R.string.list_item_service))){
                        //zaznam o servisnim zasahu
                        //Log.d("tom-log","Tady jsem");
                        long rowid = db.insert("service", null,values);

                        values = new ContentValues();
                        values.put("car_id",carId);
                        values.put("service_id",rowid);

                        db.insert("car_service", null,values);
                    } else {
                        //zaznam o tankovani
                        values.put("date", newFormat.format(oldDate));
                        //Log.d("tom-log","Tady jsem2");


                        if (etAmount.getText().toString().length() == 0) {
                            Toast.makeText(getContext(),"Zadejte množství",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (etDistance.getText().toString().length() == 0) {
                            Toast.makeText(getContext(),"Zadejte stav tachometru",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        values.put("amount", Double.parseDouble(etAmount.getText().toString()));
                        values.put("distance", Double.parseDouble(etDistance.getText().toString()));

                        long rowid = db.insert("refuel", null,values);

                        values = new ContentValues();
                        values.put("car_id",carId);
                        values.put("refuel_id",rowid);

                        db.insert("car_refuel", null,values);

                    }

                    InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    Context ctx = getActivity();
                    if (ctx instanceof CostActivity){
                        ((CostActivity) ctx).goToCosts();
                    }
                    Toast.makeText(getContext(),"Záznam úspěšně uložen",Toast.LENGTH_LONG).show();

                   /* InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);*/

                    //getActivity().onBackPressed();

                } else {
                    Toast.makeText(getContext(),"Neplatný tvar datumu (dd.MM.yyyy)",Toast.LENGTH_LONG).show();
                }
            }
        });

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


       /* if (costName.equals(getResources().getString(R.string.list_item_service))){
            displayAmount = false;
        } else {
            displayAmount = true;
        }*/
    }

    /**Vytvori novy fragment. Lze pouzit jak pro pridani, tak editaci udalosti. Staci postal eventId > 1 a bere to jako edit
     *
     * @param costName
     * @param carId
     * @param eventId (> 1 pro edit else insert)
     * @return
     */
    public static CostFragment newInstance(String costName, int carId, int eventId/*, SQLiteDatabase db*/) {

        Bundle args = new Bundle();

        CostFragment fragment = new CostFragment();
        fragment.carId = carId;
        fragment.costName = costName;
        fragment.eventId = eventId;
        //fragment.db = db;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            costName = savedInstanceState.getString("costName");
            carId = savedInstanceState.getInt("carId");
        }

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("costName",costName);
        outState.putInt("carId",carId);
    }

    public static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            date = sdf.parse(value);
            if (!value.equals(sdf.format(date))) {
                date = null;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return date != null;
    }

    private void locationRequest(){


        lr = LocationRequest.create();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if ( ContextCompat.checkSelfPermission( getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( getActivity(), new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    1 );
        }

        LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, lr, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        final LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        //fetchStores("gas_station",latLng.toString());
        ArrayList<Place> places = null;
        if (eventId <= 0) {
            //novy zaznam, najdi okoli
            try {
                if (costName.equals(getResources().getString(R.string.list_item_service))) { //servis car_repair
                    places = getPlace("car_repair", new LatLng(location.getLatitude(), location.getLongitude()), 17);
                } else {
                    //tankovani
                    places = getPlace("gas_station", new LatLng(location.getLatitude(), location.getLongitude()), 17);
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        MarkerOptions marker = null;
        CameraUpdate cameraUpdate;
        MapFragment map = (MapFragment)getActivity().getSupportFragmentManager().findFragmentByTag("mapa_g");

        if (places != null && places.size() > 0){
            //novy zaznam
            final Place nearestGasStation = places.get(0);

            marker = new MarkerOptions().title(nearestGasStation.getName()).position(new LatLng(nearestGasStation.getLat(),nearestGasStation.getLng()));

            final AlertDialog.Builder builder2;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder2 = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder2 = new AlertDialog.Builder(getContext());
            }

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.dialog_gas_station, null);
            builder2.setView(dialogView);
            builder2.setCancelable(false);
            TextView tvname = (TextView) dialogView.findViewById(R.id.tv_name);
            tvname.setText(nearestGasStation.getName() + ", " + nearestGasStation.getAddress());
            builder2.setTitle("Jste na tomto místě?")
                    .setPositiveButton("Ano", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            latLng = new LatLng(nearestGasStation.getLat(), nearestGasStation.getLng());
                            tvName.setText(nearestGasStation.getName());
                            //dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            latLng = new LatLng(myLatLng.latitude,myLatLng.longitude);
                            //dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_input_add)
                    .show();

            if (map != null && map.isVisible()) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(nearestGasStation.getLat(), nearestGasStation.getLng()), 14);
                map.moveMapInstantly(cameraUpdate);
                if (marker != null){map.addMarker(marker).showInfoWindow();}
            }

        } else {
            //nenalezeno nic v okolo

            if (eventId <= 0){
                //novy zaznam moji pozice
                marker = new MarkerOptions().title(costName).position(myLatLng);

                latLng = new LatLng(myLatLng.latitude,myLatLng.longitude);

                if (map != null && map.isVisible()) {
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, 14);
                    map.moveMapInstantly(cameraUpdate);
                    if (marker != null){map.addMarker(marker).showInfoWindow();}
                }
            } else {
                //zaznam z DB
                marker = new MarkerOptions().title(costName).position(latLng);

                if (map != null && map.isVisible()) {
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                    map.moveMapInstantly(cameraUpdate);
                    if (marker != null){map.addMarker(marker).showInfoWindow();}
                }
            }
        }



        /*MarkerOptions marker = null;
        if (latLng == null){
            //novy zaznam
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
             marker = new MarkerOptions().title(costName).position(latLng);
        }

        MapFragment map = (MapFragment)getActivity().getSupportFragmentManager().findFragmentByTag("mapa_g");

        if (map != null && map.isVisible()) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
            map.moveMap(cameraUpdate);
            if (marker != null){map.addMarker(marker);}
        }*/
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            locationRequest();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationRequest();
                }
                return;
            }
        }
    }

    private ArrayList<Place> getPlace(String type, LatLng latLng, int radius) throws ExecutionException, InterruptedException {

        PlaceRequest placeRequest = new PlaceRequest(latLng.latitude, latLng.longitude,radius, type);
        GooglePlaces googlePlaces = new GooglePlaces();

        return googlePlaces.execute(placeRequest).get();

    }

    @Override
    public void onPause() {
        super.onPause();
        googleApiClient.disconnect();
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
        openDB();

    }

    private void openDB(){
        if (db == null || !db.isOpen()){
            db = getActivity().openOrCreateDatabase("driver",Context.MODE_PRIVATE,null);
        }
    }


}
