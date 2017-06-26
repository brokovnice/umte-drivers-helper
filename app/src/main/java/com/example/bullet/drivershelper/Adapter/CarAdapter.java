package com.example.bullet.drivershelper.Adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bullet.drivershelper.Entity.Car;
import com.example.bullet.drivershelper.Interface.ClickListener;
import com.example.bullet.drivershelper.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bullet on 13.06.2017.
 */

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.ViewHolder>{

    private ArrayList<Car> carList;
    private ClickListener clickListener;


    public CarAdapter(ArrayList<Car> data, ClickListener clickListener) {
        carList = data;
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.listview_car, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, clickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvPosition.setText(Integer.toString(carList.get(position).getId()));
        holder.tvProducer.setText(carList.get(position).getProducer() + " " + carList.get(position).getModel());
        //holder.tvModel.setText(carList.get(position).getModel());
        holder.tvPower.setText(carList.get(position).getPower() + " (" + carList.get(position).getFuel() + ")");
        //holder.tvFuel.setText(carList.get(position).getFuel());
    }

    public void setData(List<Car> data) {
        carList = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public Car getItem(int position){
        return carList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ClickListener clickListener;
        private TextView tvPosition, tvProducer, tvModel, tvPower, tvFuel;
       // private ImageView ivDelete;

        public ViewHolder(View itemView, ClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;

            tvPosition = (TextView) itemView.findViewById(R.id.tv_position);
            tvProducer = (TextView) itemView.findViewById(R.id.tv_producer);
            //tvModel = (TextView) itemView.findViewById(R.id.tv_model);
            tvPower = (TextView) itemView.findViewById(R.id.tv_power);
            //tvFuel = (TextView) itemView.findViewById(R.id.tv_fuel);

            //ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null)
                clickListener.onItemClicked(getAdapterPosition());
        }
    }




}
