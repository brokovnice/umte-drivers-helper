package com.example.bullet.drivershelper.Entity;

/**
 * Created by bullet on 13.06.2017.
 */

public class Car {

    String producer, model, power, fuel;
    int id;

    public Car(String producer, String model, String power, String fuel, int id) {
        this.producer = producer;
        this.model = model;
        this.power = power;
        this.fuel = fuel;
        this.id = id;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
