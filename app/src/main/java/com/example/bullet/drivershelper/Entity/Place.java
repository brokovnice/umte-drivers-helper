package com.example.bullet.drivershelper.Entity;

/**
 * Created by bullet on 22.06.2017.
 */

public class Place {

    public String name, reference, address;
    public Double lat, lng;

    public Place() {
    }

    public Place(String name, String reference, String address, Double lat, Double lng) {
        this.name = name;
        this.reference = reference;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
