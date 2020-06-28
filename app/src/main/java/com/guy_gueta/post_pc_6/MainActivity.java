package com.guy_gueta.post_pc_6;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity  implements DialogSms.SmsDialogListener {
    public final static String ACTION_SMS = "SEND_SMS";
    public final static String NAME_OF_SP = "post pc 6 ";
    private final static String TRACKING_KEY = "tracking_key";

    private final static String TEST_MSG = "Honey I sent  a Test Message!";
    private final static String CALLER_NAME = "main";
    private final static int SMS_KEY = 1;
    private final static int LOCATION_KEY = 0;

    private String smsNum;
    private boolean isTracking = false;
    private LocationTracker locationTracker;
    private LocationInfo locationInfo;
    private TextView homeInfoText;
    private TextView locationInfoText;
    private Button setHome;
    private Button clearHome;
    private Button trackButton;
    private Button testSmsButton;
    private Button setNumberButton;
    private SharedPreferences sp;


    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra("tracking", false)) {
                locationInfo = locationTracker.getLocationInfo();
                locationInfoText.setText(locationInfo.getLocInfo());
                if (locationInfo.get_accuracy() < 50) {
                    setHome.setVisibility(View.VISIBLE);
                } else {
                    setHome.setVisibility(View.INVISIBLE);
                }
            } else {
                locationInfoText.setText("");
                setHome.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initItems();
        setButtons();
        registerReceiver(locationReceiver, new IntentFilter(LocationTracker.FETCHING + CALLER_NAME));
        setSmsNum();
        setHomeInfo();
    }

    protected void setSmsNum()
    {
        smsNum = sp.getString( "PHONE_KEY", null);
        if (smsNum != null) {
            testSmsButton.setVisibility(View.VISIBLE);
        }
    }

    protected void setButtons()
    {
        testSmsButton = findViewById(R.id.sms_test_button);
        testSmsButton.setOnClickListener(testSms());
        trackButton = findViewById(R.id.track_button);
        trackButton.setOnClickListener(startTrack());
        clearHome = findViewById(R.id.clear_home_button);
        clearHome.setOnClickListener(clearHome());
        setHome = findViewById(R.id.set_home_button);
        setHome.setOnClickListener(setHome());
        setNumberButton = findViewById(R.id.set_sms_button);
        setNumberButton.setOnClickListener(setNumber());
    }


    private void setHomeInfo()
    {
        double lat = sp.getFloat("LAT", -1);
        double longt = sp.getFloat("LONG", -1);
        isTracking = sp.getBoolean(TRACKING_KEY, false);
        if (isTracking) {
            track();
        }
        if (lat != -1) {
            @SuppressLint("DefaultLocale") String homeInfo =
                    String.format( "your current home location\n Latitude: %1$f\nLongitude: %2$f\n", lat, longt);
            homeInfoText.setText(homeInfo);
            clearHome.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTracking) {
            track();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationTracker.stopTrack();
    }

    private View.OnClickListener startTrack() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                track();
            }
        };
    }

    private View.OnClickListener stopTrack() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationTracker.stopTrack();
                trackButton.setText("stop tracking");
                trackButton.setOnClickListener(startTrack());
                isTracking = false;
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(TRACKING_KEY, isTracking);
                editor.apply();
            }
        };
    }

    private View.OnClickListener setHome() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String homeInfo = locationInfo.getHomeInfo();
                homeInfoText.setText(homeInfo);
                clearHome.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putFloat("LAT", (float) locationInfo.get_latitude());
                editor.putFloat("LONG", (float) locationInfo.get_longitude());
                editor.apply();
            }
        };
    }

    private View.OnClickListener clearHome() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeInfoText.setText("");
                SharedPreferences.Editor editor = sp.edit();
                editor.remove("LAT");
                editor.remove("LONG");
                editor.apply();
                clearHome.setVisibility(View.INVISIBLE);
            }
        };
    }

    private View.OnClickListener setNumber() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hasSmsPermission =
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) ==
                                PackageManager.PERMISSION_GRANTED;
                if (hasSmsPermission) {
                    DialogSms dialog = new DialogSms();
                    dialog.show(getSupportFragmentManager(), "sms dialog");
                }
                else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.SEND_SMS}, SMS_KEY);
                }
            }
        };
    }

    private View.OnClickListener testSms() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(ACTION_SMS);
                intent.putExtra( "PHONE_KEY", smsNum);
                intent.putExtra("CONTENT_KEY", TEST_MSG);
                getApplicationContext().sendBroadcast(intent);
            }
        };
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == LOCATION_KEY) {
                isTracking = true;
                locationTracker.startTrack();
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(TRACKING_KEY, isTracking);
                editor.apply();

            }
            else {
                DialogSms dialog = new DialogSms();
                dialog.show(getSupportFragmentManager(), "sms dialog");
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            int msg = requestCode == SMS_KEY ? R.string.sms_required : R.string.loc_required;
            builder.setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void track() {
        boolean hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (hasPermission) {
            tracking();
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(TRACKING_KEY, isTracking);
            editor.apply();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_KEY);
        }
    }

    private void tracking()
    {
        locationTracker.startTrack();
        trackButton.setText("stop tracking");
        trackButton.setOnClickListener(stopTrack());
        isTracking = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTracker.stopTrack();
        unregisterReceiver(locationReceiver);
    }

    @Override
    public void saveNumber(String phoneNumber) {
        if (phoneNumber.isEmpty())
        {
            deletePhoneNumber();
        }
        else {
            savePhoneNumber(phoneNumber);
        }
    }

    private void deletePhoneNumber()
    {
        SharedPreferences.Editor editor = sp.edit();
        smsNum = null;
        testSmsButton.setVisibility(View.INVISIBLE);
        editor.remove("PHONE_KEY");
        editor.apply();
    }

    private void savePhoneNumber(String phoneNumber)
    {
        SharedPreferences.Editor editor = sp.edit();
        smsNum = phoneNumber;
        testSmsButton.setVisibility(View.VISIBLE);
        editor.putString( "PHONE_KEY", phoneNumber);
        editor.apply();
    }

    protected void initItems()
    {

        locationTracker = new LocationTracker(this, CALLER_NAME);
        locationInfoText = findViewById(R.id.location_info);
        homeInfoText = findViewById(R.id.home_info);
        sp = getSharedPreferences(NAME_OF_SP, MODE_PRIVATE);
    }
}

