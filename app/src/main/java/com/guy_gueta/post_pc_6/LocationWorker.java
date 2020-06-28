package com.guy_gueta.post_pc_6;



import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.ActivityCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

public class LocationWorker extends ListenableWorker {
    private final static String SMS_MSG = "Honey I am Home!";
    private final static String CALLER_NAME = "worker";
    private Context _context;
    private LocationTracker tracker;
    private CallbackToFutureAdapter.Completer<Result> callback = null;
    private BroadcastReceiver receiver = null;
    private String phone_num = null;
    private float latitude = -1;
    private float longitude = -1;
    private SharedPreferences sp;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        _context = context;
        tracker = new LocationTracker(_context, CALLER_NAME);
        sp = _context.getSharedPreferences(MainActivity.NAME_OF_SP, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ListenableFuture<Result> future = CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull CallbackToFutureAdapter.Completer<Result> completer) {
                callback = completer;
                return null;
            }
        });
        placeReceiver();
        boolean hasSmsPermission = ActivityCompat.checkSelfPermission(_context, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED;
        boolean hasLocationPermission = ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!(hasSmsPermission && hasLocationPermission)) {
            callback.set(Result.success());
        }
        set_callback();
        return future;
    }

    private void set_callback()
    {

        latitude = sp.getFloat("LAT", -1);
        longitude = sp.getFloat("LONG", -1);
        phone_num = sp.getString("PHONE_KEY", null);
        if (latitude == -1 || longitude == -1 || phone_num == null || phone_num.isEmpty()) {
            callback.set(Result.success());
        }
        else {
            tracker.startTrack();
        }
    }

    private void placeReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra("tracking", false)) {
                    LocationInfo info = tracker.getLocationInfo();
                    if (info != null && info.get_accuracy() < 50) {
                        onReceiveBroadcast();
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(LocationTracker.FETCHING + CALLER_NAME);
        _context.registerReceiver(receiver, intentFilter);
    }

    private void onReceiveBroadcast() {
        _context.unregisterReceiver(receiver);
        double curLat = tracker.getLocationInfo().get_latitude();
        double curLon = tracker.getLocationInfo().get_longitude();
        tracker.stopTrack();
        double prevLat = sp.getFloat("LAT_PREV", -1);
        double prevLon = sp.getFloat("LON_PREV", -1);
        float[] result = new float[1];
        Location.distanceBetween(prevLat, prevLon, curLat, curLon, result);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat("LAT_PREV", (float) curLat);
        editor.putFloat("LON_PREV", (float) curLon);
        editor.apply();
        if (prevLat == -1 || result[0] < 50) {
            callback.set(Result.success());
            return;
        }
        Location.distanceBetween(curLat, curLon, latitude, longitude, result);
        if (result[0] < 50) {
            saveData();
        }
        callback.set(Result.success());
    }

    private void saveData()
    {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_SMS);
        intent.putExtra("PHONE_KEY", phone_num);
        intent.putExtra("CONTENT_KEY", SMS_MSG);
        _context.sendBroadcast(intent);
    }
}
