package com.example.bullet.drivershelper;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bullet.drivershelper.Entity.Car;
import com.example.bullet.drivershelper.Entity.Cost;
import com.example.bullet.drivershelper.Fragment.MapFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView tvAverageConsumption, tvKmDriven, tvRefullingCount/*, tvLowestConsumption*/, tvFuelCost, tvServiceCost, tvFuelServiceCost;
    private Spinner spinner;
    private HashMap<Integer,Integer> spinnerMapId;
    private SQLiteDatabase db;
    private SharedPreferences sharedPreferences;
    private Context ctx;
    private FloatingActionButton fab;
    private Button btnShowMap, btnShowMapService, btnCostsList;
    private String[] spinnerArrayName = new String[0];
    private ArrayAdapter<String> spinnerArrayAdapter;
    private ArrayList<Integer> idListRefuel;
    private SwipeRefreshLayout srlMain;
    private MapFragment map;
    private HashMap<Marker, Integer> eventMarkerMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvAverageConsumption = (TextView) findViewById(R.id.tv_average_consumption);
        tvKmDriven = (TextView) findViewById(R.id.tv_km_driven);
        tvRefullingCount = (TextView) findViewById(R.id.tv_refulling_count);
        //tvLowestConsumption = (TextView) findViewById(R.id.tv_lowest_consumption);
        tvFuelCost = (TextView) findViewById(R.id.tv_refulling_cost);
        tvServiceCost = (TextView) findViewById(R.id.tv_service_cost);
        tvFuelServiceCost = (TextView) findViewById(R.id.tv_total_cost);

        btnCostsList = (Button) findViewById(R.id.btn_cost_list);
        btnShowMap = (Button) findViewById(R.id.btn_map);
        btnShowMapService = (Button) findViewById(R.id.btn_map_service);

        srlMain = (SwipeRefreshLayout) findViewById(R.id.srl_main);

        toolbar.setTitleTextColor(Color.WHITE);

        sharedPreferences = getSharedPreferences(getString(R.string.sharedPreferences), Context.MODE_PRIVATE);

        if (!databaseExist(this,"driver")){
            db = openOrCreateDatabase("driver",MODE_PRIVATE,null);

            db.execSQL("PRAGMA foreign_keys=ON;");
            db.execSQL("CREATE TABLE IF NOT EXISTS car(id INTEGER PRIMARY KEY, Producer VARCHAR NOT NULL, Model VARCHAR NOT NULL, Power VARCHAR NOT NULL, Fuel VARCHAR NOT NULL);");
            db.execSQL("CREATE TABLE IF NOT EXISTS refuel(id INTEGER PRIMARY KEY, amount REAL NOT NULL, distance REAL NOT NULL, price REAL NOT NULL, date DATE NOT NULL, lat VARCHAR, lng VARCHAR, note VARCHAR, rating REAL NOT NULL, locationname VARCHAR);");
            db.execSQL("CREATE TABLE IF NOT EXISTS service(id INTEGER PRIMARY KEY, price REAL NOT NULL, date DATE NOT NULL, lat VARCHAR, lng VARCHAR, note VARCHAR, rating REAL NOT NULL, locationname VARCHAR);");
            db.execSQL("CREATE TABLE IF NOT EXISTS car_refuel(id INTEGER PRIMARY KEY, car_id INTEGER REFERENCES car(id) ON DELETE CASCADE, refuel_id INTEGER REFERENCES refuel(id) ON DELETE CASCADE);");
            db.execSQL("CREATE TABLE IF NOT EXISTS car_service(id INTEGER PRIMARY KEY, car_id INTEGER REFERENCES car(id) ON DELETE CASCADE, service_id INTEGER REFERENCES service(id) ON DELETE CASCADE);");
            db.execSQL("INSERT INTO car (id, producer, model, power, fuel) VALUES(null,'Skoda','Octavia', '2.0 103kw', 'diesel');");
            db.execSQL("INSERT INTO car (id, producer, model, power, fuel) VALUES(null,'Skoda','Rapid', '1.2 66kw', 'benzin');");
            db.execSQL("INSERT INTO car (id, producer, model, power, fuel) VALUES(null,'Ford','Focus', '1.6 67kw', 'benzín');");
            db.execSQL("INSERT INTO refuel (id, amount, distance, price, date, lat, lng, note, rating, locationname) VALUES(null,50.9,1000, 38, '2017-05-10', 50.221977, 15.788457,'pozn',4,'UNO-HK,a.s.');");
            db.execSQL("INSERT INTO refuel (id, amount, distance, price, date, lat, lng, note, rating, locationname) VALUES(null,50.9,2000, 38, '2017-05-15', 50.221977, 15.788457,'pozn',4,'UNO-HK,a.s.');");
            db.execSQL("INSERT INTO refuel (id, amount, distance, price, date, lat, lng, note, rating, locationname) VALUES(null,50,3000, 38, '2017-02-15', 50, 16.1,'pozn',4,'Tankování');");
            db.execSQL("INSERT INTO service (id, price, date, lat, lng, note, rating, locationname) VALUES(null, 3800, '2017-02-11', 50.2, 16.15,'pozn',4,'Servis');");
            db.execSQL("INSERT INTO service (id, price, date, lat, lng, note, rating, locationname) VALUES(null, 3800, '2017-03-12', 50.3, 16.15,'pozn',4,'Servis');");
            db.execSQL("INSERT INTO service (id, price, date, lat, lng, note, rating, locationname) VALUES(null, 3800, '2017-04-15', 50.4, 16.2,'pozn',4,'Servis');");
            db.execSQL("INSERT INTO car_refuel (id, car_id, refuel_id) VALUES(null, 1, 1);");
            db.execSQL("INSERT INTO car_refuel (id, car_id, refuel_id) VALUES(null, 1, 2);");
            db.execSQL("INSERT INTO car_refuel (id, car_id, refuel_id) VALUES(null, 1, 3);");
            db.execSQL("INSERT INTO car_service (id, car_id, service_id) VALUES(null, 1, 1);");
            db.execSQL("INSERT INTO car_service (id, car_id, service_id) VALUES(null, 1, 2);");
            db.execSQL("INSERT INTO car_service (id, car_id, service_id) VALUES(null, 1, 3);");

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getResources().getString(R.string.prefs_currency),"CZK");
            editor.commit();
        } else {
            db = openOrCreateDatabase("driver",MODE_PRIVATE,null);

           /* db.execSQL("DROP TABLE car_refuel;");
            db.execSQL("DROP TABLE car_service;");
            db.execSQL("DROP TABLE car;");
            db.execSQL("DROP TABLE refuel;");
            db.execSQL("DROP TABLE service;");

            db.execSQL("PRAGMA foreign_keys=ON;");
            db.execSQL("CREATE TABLE IF NOT EXISTS car(id INTEGER PRIMARY KEY, Producer VARCHAR NOT NULL, Model VARCHAR NOT NULL, Power VARCHAR NOT NULL, Fuel VARCHAR NOT NULL);");
            db.execSQL("CREATE TABLE IF NOT EXISTS refuel(id INTEGER PRIMARY KEY, amount REAL NOT NULL, distance REAL NOT NULL, price REAL NOT NULL, date DATE NOT NULL, lat VARCHAR, lng VARCHAR, note VARCHAR, rating REAL NOT NULL, locationname VARCHAR);");
            db.execSQL("CREATE TABLE IF NOT EXISTS service(id INTEGER PRIMARY KEY, price REAL NOT NULL, date DATE NOT NULL, lat VARCHAR, lng VARCHAR, note VARCHAR, rating REAL NOT NULL, locationname VARCHAR);");
            db.execSQL("CREATE TABLE IF NOT EXISTS car_refuel(id INTEGER PRIMARY KEY, car_id INTEGER REFERENCES car(id) ON DELETE CASCADE, refuel_id INTEGER REFERENCES refuel(id) ON DELETE CASCADE);");
            db.execSQL("CREATE TABLE IF NOT EXISTS car_service(id INTEGER PRIMARY KEY, car_id INTEGER REFERENCES car(id) ON DELETE CASCADE, service_id INTEGER REFERENCES service(id) ON DELETE CASCADE);");
            db.execSQL("INSERT INTO car (id, producer, model, power, fuel) VALUES(null,'Skoda','Octavia', '2.0 103kw', 'diesel');");
            db.execSQL("INSERT INTO car (id, producer, model, power, fuel) VALUES(null,'Skoda','Rapid', '1.2 66kw', 'benzin');");
            db.execSQL("INSERT INTO car (id, producer, model, power, fuel) VALUES(null,'Ford','Focus', '1.6 67kw', 'benzín');");
            db.execSQL("INSERT INTO refuel (id, amount, distance, price, date, lat, lng, note, rating, locationname) VALUES(null,50.9,1000, 38, '2017-05-10', 50.221977, 15.788457,'pozn',4,'UNO-HK,a.s.');");
            db.execSQL("INSERT INTO refuel (id, amount, distance, price, date, lat, lng, note, rating, locationname) VALUES(null,50.9,2000, 38, '2017-05-15', 50.221977, 15.788457,'pozn',4,'UNO-HK,a.s.');");
            db.execSQL("INSERT INTO refuel (id, amount, distance, price, date, lat, lng, note, rating, locationname) VALUES(null,50,3000, 38, '2017-02-15', 50, 16.1,'pozn',4,'Tankování');");
            db.execSQL("INSERT INTO service (id, price, date, lat, lng, note, rating, locationname) VALUES(null, 3800, '2017-02-11', 50.2, 16.15,'pozn',4,'Servis');");
            db.execSQL("INSERT INTO service (id, price, date, lat, lng, note, rating, locationname) VALUES(null, 3800, '2017-03-12', 50.3, 16.15,'pozn',4,'Servis');");
            db.execSQL("INSERT INTO service (id, price, date, lat, lng, note, rating, locationname) VALUES(null, 3800, '2017-04-15', 50.4, 16.2,'pozn',4,'Servis');");
            db.execSQL("INSERT INTO car_refuel (id, car_id, refuel_id) VALUES(null, 1, 1);");
            db.execSQL("INSERT INTO car_refuel (id, car_id, refuel_id) VALUES(null, 1, 2);");
            db.execSQL("INSERT INTO car_refuel (id, car_id, refuel_id) VALUES(null, 1, 3);");
            db.execSQL("INSERT INTO car_service (id, car_id, service_id) VALUES(null, 1, 1);");
            db.execSQL("INSERT INTO car_service (id, car_id, service_id) VALUES(null, 1, 2);");
            db.execSQL("INSERT INTO car_service (id, car_id, service_id) VALUES(null, 1, 3);");*/
        }


      /*  db.execSQL("DROP TABLE car_refuel;");
        db.execSQL("DROP TABLE car_service;");
        db.execSQL("DROP TABLE car;");
        db.execSQL("DROP TABLE refuel;");
        db.execSQL("DROP TABLE service;");*/





        //Vozovy park
        spinner = (Spinner) findViewById(R.id.spinner_car_select);


        updateSpinner();

        //spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArrayName);

        //spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               updateInfo();
           }

           @Override
           public void onNothingSelected(AdapterView<?> adapterView) {

           }
        });

        updateInfo();

        srlMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateSpinner();
                updateInfo();
            }
        });

        btnCostsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,CostsActivity.class);
                //Toast.makeText(MainActivity.this,"id: " + Integer.toString(getSelectedItemIdFromSpinner()) + " " +  spinnerArrayName[getSelectedItemIdFromSpinner()],Toast.LENGTH_LONG).show();
                intent.putExtra("carId",getSelectedItemIdFromSpinner());
                intent.putExtra("carName",spinnerArrayName[spinner.getSelectedItemPosition()]);
                startActivity(intent);
            }
        });

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map = MapFragment.newInstance();

/*
                findViewById(R.id.fragmentMap).bringToFront();
                findViewById(R.id.fragmentMap).invalidate();*/

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragmentMap, map, "mapa_g")
                        .addToBackStack(null)
                        .commitAllowingStateLoss();

                Runnable r = new Runnable() {
                    @Override
                    public void run(){
                        ArrayList<MarkerOptions> listMarkersOptions = loadMarkers(getSelectedItemIdFromSpinner(),getResources().getString(R.string.tv_new_record_type_fuel));
                        for (int i = 0; i < listMarkersOptions.size(); i++){
                            eventMarkerMap.put(map.addMarker(listMarkersOptions.get(i)),idListRefuel.get(i));
                        }

                        //Double lat = 50.20923, lng = 15.83277;
                        //LatLng latLng = new LatLng(lat, lng);
                        if (listMarkersOptions.size() > 0) {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(listMarkersOptions.get(listMarkersOptions.size() - 1).getPosition(), 7);
                            map.moveMap(cameraUpdate);
                        }

                        map.getMap().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                            @Override
                            public void onInfoWindowClick(Marker arg0) {
                                //Log.d("tom-log",Integer.toString(eventMarkerMap.get(arg0)));
                                //db.rawQuery("select name, amount, price, date WHERE lat = "+ + " AND lng = "+);
                                Intent i = new Intent(MainActivity.this, CostsActivity.class);
                                //i.putExtra("lat",eventMarkerMap.get());
                                //i.putExtra("eventName",getResources().getString(R.string.list_item_fuel));
                                i.putExtra("eventType",getResources().getString(R.string.tv_new_record_type_fuel));
                                i.putExtra("eventId",eventMarkerMap.get(arg0));
                                i.putExtra("carId",getSelectedItemIdFromSpinner());
                                i.putExtra("carName",spinnerArrayName[spinner.getSelectedItemPosition()]);

                                //Log.d("tom-log","eventid " + Integer.toString(eventMarkerMap.get(arg0)) + " carid " + Integer.toString(getSelectedItemIdFromSpinner()) + " carname " + spinnerArrayName[spinner.getSelectedItemPosition()]);
                                //Log.d("tom-log","eventname " +getResources().getString(R.string.list_item_fuel) + "eventid" +eventMarkerMap.get(arg0) + "carid" +Integer.toString(getSelectedItemIdFromSpinner()));
                                startActivity(i);
                                //showBackground();
                            }
                        });


                    }
                };

                Handler h = new Handler();
                h.postDelayed(r, 1500);

                hideBackground();


            }
        });

        btnShowMapService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map = MapFragment.newInstance();

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragmentMap, map, "mapa_g")
                        .addToBackStack(null)
                        .commitAllowingStateLoss();

                Runnable r = new Runnable() {
                    @Override
                    public void run(){

                        ArrayList<MarkerOptions> listMarkersOptions = loadMarkers(getSelectedItemIdFromSpinner(),getResources().getString(R.string.tv_new_record_type_service));

                        for (int i = 0; i < listMarkersOptions.size(); i++){
                            eventMarkerMap.put(map.addMarker(listMarkersOptions.get(i)),idListRefuel.get(i));
                        }

                        if (listMarkersOptions.size() > 0) {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(listMarkersOptions.get(listMarkersOptions.size() - 1).getPosition(), 7);
                            map.moveMap(cameraUpdate);
                        }

                        map.getMap().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                            @Override
                            public void onInfoWindowClick(Marker arg0) {
                                //Log.d("tom-log",Integer.toString(eventMarkerMap.get(arg0)));
                                //db.rawQuery("select name, amount, price, date WHERE lat = "+ + " AND lng = "+);
                                Intent i = new Intent(MainActivity.this, CostsActivity.class);
                                //i.putExtra("lat",eventMarkerMap.get());
                                //i.putExtra("eventName",getResources().getString(R.string.list_item_fuel));
                                i.putExtra("eventId",eventMarkerMap.get(arg0));
                                i.putExtra("eventType",getResources().getString(R.string.tv_new_record_type_service));
                                i.putExtra("carId",getSelectedItemIdFromSpinner());
                                i.putExtra("carName",spinnerArrayName[spinner.getSelectedItemPosition()]);

                                //Log.d("tom-log","eventid " + Integer.toString(eventMarkerMap.get(arg0)) + " carid " + Integer.toString(getSelectedItemIdFromSpinner()) + " carname " + spinnerArrayName[spinner.getSelectedItemPosition()]);
                                //Log.d("tom-log","eventname " +getResources().getString(R.string.list_item_fuel) + "eventid" +eventMarkerMap.get(arg0) + "carid" +Integer.toString(getSelectedItemIdFromSpinner()));
                                startActivity(i);
                                //showBackground();
                            }
                        });


                    }
                };

                Handler h = new Handler();
                h.postDelayed(r, 1500);

                hideBackground();


            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(ctx, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(ctx);
                }
                builder.setTitle("Výběr záznamu")
                        .setMessage("Vytvořit nový záznam o tankování nebo servisu?")
                        .setPositiveButton("Tankování", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this,CostActivity.class);
                                i.putExtra("eventName",getResources().getString(R.string.tv_new_record_type_fuel));
                                i.putExtra("carName",spinnerArrayName[spinner.getSelectedItemPosition()]);
                                i.putExtra("carId",getSelectedItemIdFromSpinner());
                                startActivity(i);
                            }
                        })
                        .setNeutralButton("Zrušit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setNegativeButton("Servis", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this,CostActivity.class);
                                i.putExtra("eventName",getResources().getString(R.string.list_item_service));
                                i.putExtra("carName",spinnerArrayName[spinner.getSelectedItemPosition()]);
                                i.putExtra("carId",getSelectedItemIdFromSpinner());
                                startActivity(i);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        });
    }

    private int getSelectedItemIdFromSpinner(){
        return spinnerMapId.get(spinner.getSelectedItemPosition());
    }


    /**
     * Ukaž mapu místo view activity. Aktivita má svůj view s TextView, Button.. + Framelayout pro fragment s mapou a ten potřebuje překrýt ostatní věci
     */
    private void hideBackground(){
        btnCostsList.setVisibility(View.GONE);
        btnShowMap.setVisibility(View.GONE);
        btnShowMapService.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
    }

    /**
     * Útěk z map fragmentu, ukaž pozadí
     */
    private void showBackground(){
        btnCostsList.setVisibility(View.VISIBLE);
        btnShowMap.setVisibility(View.VISIBLE);
        btnShowMapService.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
    }

    private ArrayList<MarkerOptions> loadMarkers(int carId, String type){
        ArrayList<MarkerOptions> markers = new ArrayList<>();
        idListRefuel = new ArrayList<>();
        String currency = " " + sharedPreferences.getString(getResources().getString(R.string.prefs_currency),"");

        Cursor refuel, service = null;

       if (type.equals(getResources().getString(R.string.tv_new_record_type_fuel))) {
                refuel = db.rawQuery("select r.distance, r.lat, r.lng, r.date, r.price, r.amount, r.id, r.locationname from car_refuel cr join refuel r on cr.refuel_id = r.id where cr.car_id = " + carId + " GROUP BY r.lat, r.lng",null);
        } else {
                refuel = db.rawQuery("select s.price, s.lat, s.lng, s.date, s.rating, s.note, s.id, s.locationname from car_service cs join service s on cs.service_id = s.id where cs.car_id = " + carId + " GROUP BY s.lat, s.lng",null);
       }

        while ((refuel != null) && !refuel.isLast() &&  (refuel.getCount() > 0)) {
            refuel.moveToNext();
            //Log.d("tom-log","souradnice: " + Double.toString(refuel.getDouble(1)) + " " + Double.toString(refuel.getDouble(2)));
            /*MarkerOptions m =new MarkerOptions()
                    .position(new LatLng(refuel.getDouble(1), refuel.getDouble(2)))
                    .title(refuel.getString(7)+" (" + refuel.getString(3) + ")")
                    .snippet(Double.toString(refuel.getDouble(5)) + "l (" + Double.toString(refuel.getDouble(4)) + " " + currency + ")");*/

            MarkerOptions m =new MarkerOptions()
                    .position(new LatLng(refuel.getDouble(1), refuel.getDouble(2)))
                    .title(refuel.getString(7));

            idListRefuel.add(refuel.getInt(6));

            markers.add(m);
            eventMarkerMap = new HashMap<>();
            //eventMarkerMap.put(m.,refuel.getInt(6));
        }
        //Log.d("tom-log","pocet stanic: "+ Integer.toString(markers.size()));
        refuel.close();


        /*Cursor service = db.rawQuery("select s.lat, s.lng from car_service cs join service s on cs.service_id = s.id where cs.car_id = " + carId,null);

        while (!service.isLast() && (service != null) && (service.getCount() > 0)) {
            service.moveToNext();

            markers.add(new MarkerOptions()
                    .position(new LatLng(service.getDouble(0), service.getDouble(1)))
                    .title("Tankování"));
        }
        service.close();*/

        return markers;
    }

    /**Aktualizuje zobrazene informace o aute
     *
     */
    private void updateInfo(){
        Resources res = getResources();

        Cursor test = db.rawQuery("Select * from refuel",null);
        Cursor anyData = db.rawQuery("Select * from car",null);

        if (anyData != null && anyData.getCount() > 0){
        while (!test.isLast() && (test.getCount() > 0)) {
            test.moveToNext();
            //Log.d("tom-log","id " + Integer.toString(test.getInt(0)) + " amount " + Double.toString(test.getDouble(1)) + " distance " + Double.toString(test.getDouble(2)) + " price " + Double.toString(test.getDouble(3)) + " date " + test.getString(4) + " lat " + Double.toString(test.getDouble(5)) + " lng" + Double.toString(test.getDouble(6)) + " note " + test.getString(7) + " rating " + Double.toString(test.getDouble(8)));

        }

        if (spinnerMapId.size() > 0) {
            String currency = " " + sharedPreferences.getString(res.getString(R.string.prefs_currency), "");

            //pocet tankovani
            Cursor refuelingCount = db.rawQuery("Select COUNT(*) AS pocet from car_refuel WHERE car_id = " + getSelectedItemIdFromSpinner(), null);
            refuelingCount.moveToFirst();
            tvRefullingCount.setText(res.getString(R.string.tv_refulling_count) + " " + Integer.toString(refuelingCount.getInt(0)));

            //prumerna spotreba, ujete km, celkemz a palivo
            Cursor lastRefuel = db.rawQuery("SELECT id from refuel ORDER BY id DESC LIMIT 1",null);
            int lastId = -1;
            if (lastRefuel.getCount() > 0) {
                lastRefuel.moveToFirst();
                 lastId = lastRefuel.getInt(0);
            }
            Cursor averageConsumption = db.rawQuery("Select COALESCE(MIN(r.distance),0) AS minkm, COALESCE(MAX(r.distance),0) AS maxkm, SUM(CASE WHEN r.id = "+lastId+" THEN 0 ELSE r.amount END) AS fuel, SUM(r.price) FROM refuel r join car_refuel cr on cr.refuel_id = r.id WHERE cr.car_id = " + getSelectedItemIdFromSpinner(), null);
            averageConsumption.moveToFirst();

            float fuelcost = Math.round(averageConsumption.getDouble(3) * 100) / 100;

            if ((averageConsumption.getDouble(1) - averageConsumption.getDouble(0)) != 0) {
                //Log.d("tom-log","max km: "+Double.toString(averageConsumption.getDouble(1)) + " min km " + Double.toString(averageConsumption.getDouble(0)) + " fuel " + Double.toString(averageConsumption.getDouble(2)));
                tvAverageConsumption.setText(res.getString(R.string.tv_average_consumption) + " " +
                        Double.toString(Math.round(averageConsumption.getDouble(2) / (averageConsumption.getDouble(1) - averageConsumption.getDouble(0)) * 100 * 100) / 100.0) +
                        "l");

                tvKmDriven.setText(res.getString(R.string.tv_km_driven) + " " + Double.toString(Math.round((averageConsumption.getDouble(1) - averageConsumption.getDouble(0)) * 100) / 100.0) + (sharedPreferences.getBoolean(res.getString(R.string.prefs_km_mile), true) ? " km" : " mile"));
                tvFuelCost.setText(res.getString(R.string.tv_refulling_cost) + " " + fuelcost + currency);
            } else {
                tvAverageConsumption.setText(res.getString(R.string.tv_average_consumption) + " nedostatek dat");
                tvKmDriven.setText(res.getString(R.string.tv_km_driven) + " 0" + (sharedPreferences.getBoolean(res.getString(R.string.prefs_km_mile), true) ? " km" : " mile"));
                tvFuelCost.setText(res.getString(R.string.tv_refulling_cost) + " 0" + currency);
            }

            //za servis
            Cursor service = db.rawQuery("Select SUM(s.price) AS service FROM service s join car_service cs on cs.service_id = s.id WHERE cs.car_id = " + getSelectedItemIdFromSpinner(), null);
            service.moveToFirst();

            float servicecost = Math.round(service.getDouble(0) * 100) / 100;
            tvServiceCost.setText(res.getString(R.string.tv_service_cost) + " " + servicecost + currency);

            //celkem
            tvFuelServiceCost.setText(res.getString(R.string.tv_total_cost) + " " + Float.toString(servicecost + fuelcost) + currency);


        }
        }
        srlMain.setRefreshing(false);
    }

    private void updateSpinner(){
        ArrayList<Car> carList = new ArrayList<>();

        Cursor resultSet = db.rawQuery("Select * from car",null);

        if (resultSet != null && resultSet.getCount() > 0) {

            while (!resultSet.isLast()) {
                resultSet.moveToNext();
                //Log.d("tom-log","cars: "+resultSet.getString(1));
                carList.add(new Car(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getInt(0)));
            }
            resultSet.close();

            spinnerArrayName = new String[carList.size()];

            spinnerMapId = new HashMap<>();
            for (int i = 0; i < carList.size(); i++) {
                spinnerMapId.put(i, carList.get(i).getId());
                spinnerArrayName[i] = carList.get(i).getProducer() + " " + carList.get(i).getModel() + " " + carList.get(i).getPower() + " " + carList.get(i).getFuel();
            }

            spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArrayName);

            //spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArrayName);

            //spinnerArrayAdapter.addAll(spinnerArrayName);
            spinner.setAdapter(spinnerArrayAdapter);
            //spinnerArrayAdapter.notifyDataSetChanged();
            //spinner.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            default: break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        db = openOrCreateDatabase("driver",MODE_PRIVATE,null);
        updateSpinner();
        updateInfo();
        spinner.setSelection(sharedPreferences.getInt(getResources().getString(R.string.prefs_last_car),0));
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getResources().getString(R.string.prefs_last_car),spinner.getSelectedItemPosition());
        editor.commit();
        super.onPause();

    }

    @Override
    public void onBackPressed() {
        MapFragment mapFragment = (MapFragment)getSupportFragmentManager().findFragmentByTag("mapa_g");
        if (mapFragment != null && mapFragment.isVisible()) {
            showBackground();
        } else {
            hideBackground();
        }
        super.onBackPressed();
    }

    private static boolean databaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }
}
