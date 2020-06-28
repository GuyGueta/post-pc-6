package com.guy_gueta.post_pc_6;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.Executor;

class LocationTracker {

    static  final  String FETCHING = "fetching_location";
    private Context _context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationInfo locationInfo;
    private LocationCallback locationCallback;

    LocationTracker(Context context, String caller) {
        _context = context;
        fusedLocationProviderClient = new FusedLocationProviderClient(context);
        locationCallback = trackLocation(caller);
    }


    void startTrack() {
        final LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
        SettingsClient client = LocationServices.getSettingsClient(_context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        Executor executor = ContextCompat.getMainExecutor(_context);
        task.addOnSuccessListener(executor, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
            }
        });
        task.addOnFailureListener(executor, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult((Activity)_context, 0);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.e("EXCEPTION", sendEx.getMessage());
                    }
                }
            }
        });
    }

    void stopTrack() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        locationInfo = null;
        Intent intent = new Intent();
        intent.setAction(FETCHING);
        intent.putExtra("tracking", false);
        _context.sendBroadcast(intent);
    }

    private LocationCallback trackLocation(final String caller) {
        return new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Intent intent = new Intent();
                intent.setAction(FETCHING + caller);
                if (locationResult == null) {
                    intent.putExtra("tracking", false);
                    _context.sendBroadcast(intent);
                    return;
                }
                for (Location loc : locationResult.getLocations()) {
                    locationInfo = new LocationInfo(loc.getAccuracy(), loc.getLatitude(), loc.getLongitude());
                    intent.putExtra("tracking", true);
                }
                _context.sendBroadcast(intent);
            }
        };
    }

    LocationInfo getLocationInfo() {
        return locationInfo;
    }
}

