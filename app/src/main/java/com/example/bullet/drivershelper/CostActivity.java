package com.example.bullet.drivershelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.bullet.drivershelper.Adapter.CarAdapter;
import com.example.bullet.drivershelper.Entity.Car;
import com.example.bullet.drivershelper.Fragment.CostFragment;
import com.example.bullet.drivershelper.Fragment.MapFragment;
import com.example.bullet.drivershelper.Interface.ClickListener;

import java.util.ArrayList;

import lib.DividerItemDecoration;

public class CostActivity extends AppCompatActivity implements ClickListener {

    private CarAdapter carAdapter;

    private MapFragment mapFragment;
    private CostFragment costFragment;
    private FragmentManager fragmentManager;
    private Intent intent;
    private String carName;

   // private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cost);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        intent = getIntent();

        carName = intent.getStringExtra("carName");

       // db = openOrCreateDatabase("driver",MODE_PRIVATE,null);

        //Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " - nový záznam");

        //rozliseni tankovani / servis
        //tvName.setText(getIntent().getStringExtra("eventName"));


        fragmentManager = getSupportFragmentManager();

        initializaceFragments();

    }

    private void initializaceFragments() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //na vysku

            //udaje
            if (costFragment != null) {
                fragmentManager.beginTransaction()
                        .detach(costFragment)
                        .commit();
            }

            costFragment = CostFragment.newInstance(intent.getStringExtra("eventName"),intent.getIntExtra("carId",-1),intent.getIntExtra("eventId",-1));

            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentValues, costFragment, "novy_zaznam")
                    //.addToBackStack(null)
                    .commit();


        } else {
            //na sirku

            //udaje
            if (costFragment != null) {
                fragmentManager.beginTransaction()
                        .detach(costFragment)
                        .commit();
            }

            costFragment = CostFragment.newInstance(intent.getStringExtra("eventName"),intent.getIntExtra("carId",-1),intent.getIntExtra("eventId",-1));

            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentValues, costFragment, "novy_zaznam")
                    //.addToBackStack(null)
                    .commit();

            //mapa
            if (mapFragment != null) {
                fragmentManager.beginTransaction()
                        .detach(mapFragment)
                        .commitAllowingStateLoss();
            }

            mapFragment = MapFragment.newInstance();

            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentMap, mapFragment, "mapa_g")
                    .addToBackStack(null)
                    .commitAllowingStateLoss();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goHome();
        }

        return super.onOptionsItemSelected(item);
    }

    private void goHome(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToCosts(){
        if (intent.getIntExtra("carId",-1) != -1) {

            Intent i = new Intent(this, CostsActivity.class);
            i.putExtra("carId", intent.getIntExtra("carId", -1));
            //i.putExtra("eventId",intent.getIntExtra("eventId",-1));
            //i.putExtra("eventType",intent.getStringExtra("eventName"));
            i.putExtra("carName", carName);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onItemClicked(int position) {
        //Log.d("tom-log","click" + Integer.toString(position));
    }

    @Override
    public void onBackPressed() {
        goToCosts();
        super.onBackPressed();
    }
}
