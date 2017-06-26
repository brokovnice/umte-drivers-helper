package com.example.bullet.drivershelper.Entity;

/**
 * Created by bullet on 22.06.2017.
 */

public class PlaceRequest {

    Double lat, lng;
    int radius;
    String type;

    public PlaceRequest(Double lat, Double lng, int radius, String type) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
