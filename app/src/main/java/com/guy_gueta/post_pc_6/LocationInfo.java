package com.guy_gueta.post_pc_6;


import android.annotation.SuppressLint;

class LocationInfo {
    private float _accuracy;
    private double _latitude;
    private double _longitude;

    LocationInfo(float accuracy, double latitude, double longitude) {
        _accuracy = accuracy;
        _latitude = latitude;
        _longitude = longitude;
    }

    float get_accuracy() {
        return _accuracy;
    }

    double get_latitude() {
        return _latitude;
    }

    double get_longitude() {
        return _longitude;
    }

    void setInfo(double latitude, double longitude)
    {
        _latitude = latitude;
        _longitude = longitude;
    }

    @SuppressLint("DefaultLocale")
    String getHomeInfo(){
        return String.format( "your current home location\n Latitude: %1$f\nLongitude: %2$f\n", _latitude,_longitude);
    }

    @SuppressLint("DefaultLocale")
    String getLocInfo()
    {
        return String.format("your current location is \n latitude: %1$f \n longitude:" +
                " %2$f \n accuracy: %3$f meters",_latitude, _longitude, _accuracy);
    }
}

