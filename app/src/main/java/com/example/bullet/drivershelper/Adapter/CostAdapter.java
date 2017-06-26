package com.example.bullet.drivershelper.Adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bullet.drivershelper.Entity.Cost;
import com.example.bullet.drivershelper.Interface.ClickListener;
import com.example.bullet.drivershelper.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by bullet on 13.06.2017.
 */

public class CostAdapter extends RecyclerView.Adapter<CostAdapter.ViewHolder>{

    private ArrayList<Cost> costList;
    private ClickListener clickListener;
    private String currency;

    public CostAdapter(ArrayList<Cost> data, ClickListener clickListener, String currency) {
        costList = data;
        this.clickListener = clickListener;
        this.currency = currency;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.listview_costs, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, clickListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(costList.get(position).getDate());

        holder.tvName.setText(costList.get(position).getName());
        holder.tvDate.setText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + ". " + Integer.toString(cal.get(Calendar.MONTH)+1) + ". " + cal.get(Calendar.YEAR));
        holder.tvPrice.setText(Float.toString(Math.round(costList.get(position).getPrice()*100)/100) + currency);
    }

    public void setData(List<Cost> data) {
        costList = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return costList.size();
    }

    public Cost getItem(int position){
        return costList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ClickListener clickListener;
        private TextView tvName, tvDate, tvPrice;

        public ViewHolder(View itemView, ClickListener clickListener) {
            super(itemView);
            this.clickListener = clickListener;

            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvPrice = (TextView) itemView.findViewById(R.id.tv_price);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null)
                clickListener.onItemClicked(getAdapterPosition());
        }
    }




}
