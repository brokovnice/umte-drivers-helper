package com.example.bullet.drivershelper.Entity;

import java.util.Date;

/**
 * Created by bullet on 14.06.2017.
 */

public class Cost {

    int id;
    String name;
    float price;
    Date date;

    public Cost(int id, String name, float price, Date date) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
