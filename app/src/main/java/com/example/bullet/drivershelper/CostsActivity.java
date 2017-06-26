package com.example.bullet.drivershelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bullet.drivershelper.Adapter.CostAdapter;
import com.example.bullet.drivershelper.Entity.Cost;
import com.example.bullet.drivershelper.Entity.Place;
import com.example.bullet.drivershelper.Interface.ClickListener;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import lib.DividerItemDecoration;

/**
 * Created by bullet on 14.06.2017.
 */

public class CostsActivity extends AppCompatActivity implements ClickListener{

    private SQLiteDatabase db;
    private EditText etYear;
    private RecyclerView rvCosts;
    private SharedPreferences prefs;
    private int carId, eventId;
    private CostAdapter costAdapter;
    private String eventType, carName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_costs);

        Intent i = getIntent();
        carId = i.getIntExtra("carId",-1);
        eventId = i.getIntExtra("eventId",-1);
        eventType = i.getStringExtra("eventType");
        carName = i.getStringExtra("carName");

        Log.d("tom-log","name " + carName + " carid " + Integer.toString(carId));

        Button btnMinus, btnPlus, btnOk;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        rvCosts = (RecyclerView) findViewById(R.id.rv_costs_list);
        etYear = (EditText) findViewById(R.id.et_year);
        TextView tvCarName = (TextView) findViewById(R.id.tv_car_name);

        btnMinus = (Button) findViewById(R.id.btn_minus);
        btnPlus = (Button) findViewById(R.id.btn_plus);
        btnOk = (Button) findViewById(R.id.btn_ok);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " - náklady");

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        rvCosts.setLayoutManager(new LinearLayoutManager(this));
        rvCosts.addItemDecoration(itemDecoration);

        prefs = getSharedPreferences(getString(R.string.sharedPreferences), Context.MODE_PRIVATE);

        Calendar cal = Calendar.getInstance();
        etYear.setText(Integer.toString(cal.get(Calendar.YEAR)));

        //updateData(cal.get(Calendar.YEAR));

        tvCarName.setText(getResources().getString(R.string.car) + " " + carName);

        etYear.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    if (etYear.getText().toString().length() == 4) {
                        updateData(Integer.parseInt(etYear.getText().toString()));
                        return true;
                    } else {
                        Toast.makeText(CostsActivity.this,"Délka musí být 4",Toast.LENGTH_LONG).show();
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });

        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etYear.setText(Integer.toString(Integer.parseInt(etYear.getText().toString()) - 1));
            }
        });

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etYear.setText(Integer.toString(Integer.parseInt(etYear.getText().toString()) + 1));
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateData(Integer.parseInt(etYear.getText().toString()));
            }
        });
    }

    /**Aktualizuje seznam nakladu dle predaneho roku
     *
     * @param year
     */
    private void updateData(int year){
        db = openOrCreateDatabase("driver", MODE_PRIVATE, null);
        String start_year = "'"+year + "-01-01"+"'";
        String end_year = "'"+year + "-12-31"+"'";

        /*Cursor test = db.rawQuery("Select date from refuel", null);
        test.moveToFirst();*/

        Cursor refuel = null;
        Cursor service = null;

        if (eventId == -1){
            //nejedna se o specificke tankovani, ale cely seznam
            refuel = db.rawQuery("Select r.price, r.date, r.id, r.locationname from car_refuel cr join refuel r on cr.refuel_id = r.id WHERE cr.car_id = "+carId + " AND r.date between "+ start_year + " AND "+end_year +" order by r.date DESC", null);
            if (refuel != null){
                Log.d("tom-log","v seznamu " + Integer.toString(refuel.getCount()));
            } else {
                Log.d("tom-log","neco tam nejde" );
            }
            service = db.rawQuery("Select s.price, s.date, s.id, s.locationname from car_service cs join service s on cs.service_id = s.id WHERE cs.car_id = " + carId + " AND s.date between " + start_year + " AND " + end_year +  " order by s.date DESC", null);
        } else {
            //specificka lokace
            if (eventType.equals("")) return;


            if (eventType.equals(getResources().getString(R.string.tv_new_record_type_fuel))) {
                Cursor event = db.rawQuery("SELECT lat, lng FROM refuel WHERE id = "+eventId,null);
                event.moveToFirst();
                refuel = db.rawQuery("Select r.price, r.date, r.id, r.locationname from car_refuel cr join refuel r on cr.refuel_id = r.id WHERE cr.car_id = " + carId + " AND r.lat = " + Double.toString(event.getDouble(0)) + " AND r.lng = " + Double.toString(event.getDouble(1)) + " AND r.date between " + start_year + " AND " + end_year + " order by r.date DESC", null);
            } else {
                Cursor event = db.rawQuery("SELECT lat, lng FROM service WHERE id = "+eventId,null);
                event.moveToFirst();
                refuel = db.rawQuery("Select s.price, s.date, s.id, s.locationname from car_service cs join service s on cs.service_id = s.id WHERE cs.car_id = " + carId + " AND s.lat = " + Double.toString(event.getDouble(0)) + " AND s.lng = " + Double.toString(event.getDouble(1)) + " AND s.date between " + start_year + " AND " + end_year + " order by s.date DESC", null);
            }
            /*if (refuel != null){
                Log.d("tom-log","v seznamu " + Integer.toString(refuel.getCount()) + Double.toString(event.getDouble(0)) + " " + Double.toString(event.getDouble(1)));
            } else {
                Log.d("tom-log","neco tam nejde" );
            }*/
        }



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<Cost> costList = new ArrayList<>();

        while ((refuel != null) && !refuel.isLast() && (refuel.getCount() > 0)) {
            refuel.moveToNext();

            Date date = null;
            try {
                date = sdf.parse(refuel.getString(1));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //Calendar cal = Calendar.getInstance();
            //cal.setTime(date);
            //Log.d("tom-log","dateorig: " + refuel.getString(1) + " datenew " + date.getDate());
            //Log.d("tom-log","date: " + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + "." + Integer.toString(cal.get(Calendar.MONTH)+1)+ "."  + Integer.toString(cal.get(Calendar.YEAR)));
            costList.add(new Cost(refuel.getInt(2),refuel.getString(3),refuel.getFloat(0),date));
        }
        if (refuel != null) refuel.close();

        while ((service != null) && !service.isLast() && (service.getCount() > 0)){
            service.moveToNext();
            Date date = null;
            try {
                date = sdf.parse(service.getString(1));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            costList.add(new Cost(service.getInt(2), service.getString(3),service.getFloat(0),date));
        }

        if (costList.size() > 0) {
            Collections.sort(costList, new Comparator<Cost>() {
                @Override
                public int compare(Cost cost1, Cost cost2) {

                    return cost2.getDate().compareTo(cost1.getDate());
                }
            });
        }

        costAdapter = new CostAdapter(costList,this, " " + prefs.getString(getResources().getString(R.string.prefs_currency),""));
        rvCosts.setAdapter(costAdapter);

        if (service != null) service.close();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        db = openOrCreateDatabase("driver", MODE_PRIVATE, null);
        updateData(Integer.parseInt(etYear.getText().toString()));
    }

    @Override
    public void onItemClicked(int position) {
        Intent intent = new Intent(this,CostActivity.class);
        intent.putExtra("eventId",costAdapter.getItem(position).getId());
        intent.putExtra("carId",carId);
        intent.putExtra("carName",carName);
        intent.putExtra("eventName",costAdapter.getItem(position).getName());
        startActivity(intent);
        finish();
    }


}
