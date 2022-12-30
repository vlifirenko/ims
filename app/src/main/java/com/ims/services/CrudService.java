package com.ims.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ims.ICrudService;
import com.ims.content.model.Item;
import com.ims.content.provider.Contract;
import com.ims.helpers.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CrudService extends Service implements LocationListener {
    private static final String TAG = CrudService.class.getName();

    private LocationManager locationManager;
    private String provider;

    private Double lat;
    private Double lng;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            lat = null;
            lng = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        locationManager.removeUpdates(this);
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        return new ICrudService.Stub() {

            @Override
            public boolean create(Item item) throws RemoteException {
                return saveItem(item);
            }

            @Override
            public Item get(int id) throws RemoteException {
                //TODO get item
                return null;
            }

            @Override
            public List<Item> getAll() throws RemoteException {
                Cursor c = getContentResolver().query(
                        Contract.Item.CONTENT_URI, null, null, null, null);
                List<Item> items = new ArrayList<Item>();
                try {
                    if (c != null && c.moveToFirst()) {
                        do {
                            items.add(new Item(c));
                            c.moveToNext();
                        } while (!c.isAfterLast());
                    }
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                }
                return items;
            }

            @Override
            public boolean update(Item item) throws RemoteException {
                item.save(getBaseContext());
                return false;
            }

            @Override
            public boolean delete(Item item) throws RemoteException {
                File file = new File(IOUtils.createAppFolder(""), item.getId() + ".json");
                return file.delete();
            }

            @Override
            public boolean save(Item item, String file_extension, String body) throws RemoteException {
                if (!saveItem(item)) {
                    return false;
                } else {
                    File folder = IOUtils.createAppFolder("sms");
                    if (folder != null) {
                        try {
                            File file = new File(folder, item.getId() + file_extension);
                            return IOUtils.saveFile(file, body);
                        } catch (IOException e) {
                            Log.e("Exception", "File write failed: " + e.toString());
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }

            @Override
            public List<Item> getByText(String text) throws RemoteException {
                Cursor c = getContentResolver().query(
                        Contract.Item.CONTENT_URI, null,
                        Contract.Item.TEXT + "='" + text + "'", null, null);
                List<Item> items = new ArrayList<Item>();
                try {
                    if (c != null && c.moveToFirst()) {
                        do {
                            items.add(new Item(c));
                            c.moveToNext();
                        } while (!c.isAfterLast());
                    }
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                }
                return items;
            }

            @Override
            public List<Item> find(String text) throws RemoteException {
                Cursor c = getContentResolver().query(
                        Contract.Item.CONTENT_URI, null,
                        Contract.Item.TEXT + " LIKE '%" + text + "%' OR " +
                                Contract.Item.TAGS + " LIKE '%" + text + "%' OR " +
                                Contract.Item.EXTENSIONS + " LIKE '%" + text + "%'"
                        , null, null
                );
                List<Item> items = new ArrayList<Item>();
                try {
                    if (c != null && c.moveToFirst()) {
                        do {
                            items.add(new Item(c));
                            c.moveToNext();
                        } while (!c.isAfterLast());
                    }
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                }
                return items;
            }
        };
    }

    private boolean saveItem(Item item) {
        if (lat != null)
            item.lat = lat;
        if (lng != null)
            item.lng = lng;
        item.save(this);
        return item.saveToDisk();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Enabled new provider " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Disabled provider " + provider);
    }
}
