package com.example.bullet.drivershelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.bullet.drivershelper.Adapter.CarAdapter;
import com.example.bullet.drivershelper.Entity.Car;
import com.example.bullet.drivershelper.Interface.ClickListener;

import java.util.ArrayList;
import java.util.HashMap;

import lib.DividerItemDecoration;

public class SettingActivity extends AppCompatActivity implements ClickListener {

    private CarAdapter carAdapter;
    private SQLiteDatabase db;
    private FloatingActionButton fabAddCar;
    private Context ctx;
    private ArrayList<Car> carList = new ArrayList<>();
    private int clickCheck = 0;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ctx = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_car_list);
        Switch svKmMile = (Switch) findViewById(R.id.sw_km_mi);

        fabAddCar = (FloatingActionButton) findViewById(R.id.fab_add_car);

        sharedPreferences = getSharedPreferences(getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        //Switch jednotky vzdalenosti
        svKmMile.setChecked(sharedPreferences.getBoolean(getResources().getString(R.string.prefs_km_mile), true));
        svKmMile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                recalculateUnits(b);
                editor.putBoolean(getString(R.string.prefs_km_mile),b);
                editor.commit();
            }
        });

        //Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " - nastavení");

        //Vozidla
        carAdapter = new CarAdapter(carList,this);

        openDB();
        updateList();

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(itemDecoration);

        rv.setAdapter(carAdapter);

        //Měna
        final ArrayList<String> currency = new ArrayList<>();
        currency.add("CZK") ;
        currency.add("EUR");
        currency.add("USD");

        Spinner spinner = (Spinner) findViewById(R.id.spinner_currency);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currency);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setSelection(sharedPreferences.getInt(getResources().getString(R.string.prefs_currency_spinner_position),0));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (clickCheck++ > 0) {
                    recalculatePrice(currency.get(i), sharedPreferences.getString(getResources().getString(R.string.prefs_currency), ""));
                    editor.putString(getResources().getString(R.string.prefs_currency), currency.get(i));
                    editor.putInt(getResources().getString(R.string.prefs_currency_spinner_position), i);
                    editor.commit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        fabAddCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder2;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder2 = new AlertDialog.Builder(ctx, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder2 = new AlertDialog.Builder(ctx);
                }

                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_add_car, null);
                builder2.setView(dialogView);

                builder2.setTitle("Přidat vozidlo")
                        //.setMessage("es")
                        .setPositiveButton("Uložit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                EditText etproducer = (EditText) dialogView.findViewById(R.id.et_producer);
                                EditText etmodel = (EditText) dialogView.findViewById(R.id.et_model);
                                EditText etpower = (EditText) dialogView.findViewById(R.id.et_power);
                                RadioGroup rgFuel = (RadioGroup) dialogView.findViewById(R.id.rg_fuel_type);
                                RadioButton rb = (RadioButton) dialogView.findViewById(rgFuel.getCheckedRadioButtonId());

                                if (etproducer.length() < 1 || etmodel.length() < 1 || etpower.length() < 1){
                                    Toast.makeText(ctx,"Vyplňte všechny údaje",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                    db.execSQL("INSERT INTO car (id, producer, model, power, fuel) VALUES(null,'" + etproducer.getText().toString() +"','"+ etmodel.getText().toString() +"', '"+ etpower.getText().toString() +"', '"+ rb.getText().toString() +"');");
                                    Toast.makeText(ctx,"Záznam úspěšně vložen",Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    updateList();

                            }
                        })
                        .setNegativeButton("Zavřít", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_input_add)
                        .show();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(int position) {
        final int pos = position;
        final Car car = carAdapter.getItem(position);

        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(ctx, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(ctx);
        }
        builder.setTitle("Smazat záznam?")
                .setMessage("Vozidlo: " + car.getProducer() + " " + car.getModel() + " " + car.getPower() + " (" + car.getFuel() + ")")
                .setPositiveButton("Smazat", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCar(car.getId());
                        updateList();
                    }
                })
                .setNegativeButton("Zavřít", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.stat_sys_warning)
                .show();
    }

    private void deleteCar(int carId){
        Cursor carservice = db.rawQuery("Select service_id from car_service where car_id = "+carId,null);
        Cursor carrefuel = db.rawQuery("Select refuel_id from car_refuel where car_id = "+carId,null);

        while (!carservice.isLast() && (carservice != null) && (carservice.getCount() > 0)) {
            carservice.moveToNext();
            db.execSQL("Delete from service where id = " + carservice.getInt(0));
            //Log.d("tom-log","Delete service");
        }
        carservice.close();

        while (!carrefuel.isLast() && (carrefuel != null) && (carrefuel.getCount() > 0)) {
            carrefuel.moveToNext();
            db.execSQL("Delete from refuel where id = " + carrefuel.getInt(0));
            //Log.d("tom-log","Delete refuel");
        }
        carrefuel.close();

        db.execSQL("Delete from car_service where car_id = "+carId);
        db.execSQL("Delete from car_refuel where car_id = "+carId);
        db.execSQL("Delete from car where id = " + carId);

        /*Cursor my = db.rawQuery("select * from car_refuel",null);
        while (!my.isLast() && (my != null) && (my.getCount() > 0)) {
            my.moveToNext();
            Log.d("tom-log","zbylo: " + Integer.toString(my.getInt(0)) + " "  + Integer.toString(my.getInt(1)) + " "  + Integer.toString(my.getInt(2)));
        }
        my.close();*/

    }

    private void updateList(){
        carList = new ArrayList<>();

        Cursor resultSet = db.rawQuery("Select * from car",null);

        if (resultSet != null && resultSet.getCount() > 0) {

            while (!resultSet.isLast()) {
                resultSet.moveToNext();
                carList.add(new Car(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4), resultSet.getInt(0)));
                //Log.d("tom-log","producer "+carList.get().getProducer() + " model " + carList.get(0).getModel() + " power " + carList.get(0).getPower() + " fuel " + carList.get(0).getFuel());
            }

            carAdapter.setData(carList);
            carAdapter.notifyDataSetChanged();
        }
    }

    private void recalculatePrice(String oldPriceName, String newPriceName){
        final AlertDialog.Builder builder2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder2 = new AlertDialog.Builder(ctx, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder2 = new AlertDialog.Builder(ctx);
        }
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_recalculate_price, null);
        builder2.setView(dialogView);

        builder2.setTitle("Přepočet měny")
                .setCancelable(false)
                .setMessage("Zadejte dostanete  " + oldPriceName + " za 1 " + newPriceName)
                .setPositiveButton("Uložit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText etRate = (EditText) dialogView.findViewById(R.id.et_rate);
                        if (etRate.getText().toString().length() == 0){
                            Toast.makeText(ctx,"Proběhne změna měny bez přepočtu kurzu",Toast.LENGTH_LONG).show();
                        } else {
                            db.execSQL("UPDATE refuel set price = price * " + Double.parseDouble(etRate.getText().toString()));
                            db.execSQL("UPDATE service set price = price * " + Double.parseDouble(etRate.getText().toString()));
                            Toast.makeText(ctx, "Měna přepočítána", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }

                    }
                })
                .setNegativeButton("Zavřít", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ctx, "Proběhne změna měny bez přepočtu kurzu", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_input_add)
                .show();
    }

    private void recalculateUnits(Boolean switched){
        double mileToKm = 1.609344;
        double kmToMile = 0.621371192;
        if (switched){
            db.execSQL("UPDATE refuel set distance = distance * " + Double.toString(mileToKm));
        }else {
            db.execSQL("UPDATE refuel set distance = distance * " + Double.toString(kmToMile));
        }

    }

    private void openDB(){
        if (db == null || !db.isOpen()){
            db = openOrCreateDatabase("driver",Context.MODE_PRIVATE,null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        openDB();
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }
}
