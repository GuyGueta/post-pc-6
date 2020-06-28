package com.guy_gueta.post_pc_6;



import android.app.Application;
import android.content.IntentFilter;

import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(new LocalSendSmsBroadcastReceiver(), new IntentFilter(MainActivity.ACTION_SMS));
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(Constraints.NONE)
                .build();
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(request);
    }
}
