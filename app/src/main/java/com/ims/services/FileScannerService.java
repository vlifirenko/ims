package com.ims.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.ims.helpers.FileScanner;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

@EService
public class FileScannerService extends Service {
    public static final String LOG_TAG = FileScannerService.class.getName();

    private static Timer timer;

    public class LocalBinder extends Binder {
        public FileScannerService getService() {
            return FileScannerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        Calendar date = new GregorianCalendar();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.add(Calendar.DAY_OF_MONTH, 1);
        timer.scheduleAtFixedRate(new mainTask(), date.getTime(), 24 * 60 * 60 * 1000);
    }

    private class mainTask extends TimerTask {
        public void run() {
            notificationHandler.sendEmptyMessage(0);
        }
    }

    private final Handler notificationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            scan();
        }
    };

    @Background
    void scan() {
        FileScanner.scan(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (timer != null)
            timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();
}
