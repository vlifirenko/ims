package com.ims.content.model;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public abstract class Model {
    protected String id;

    public String getId() {
        return id;
    }

    public abstract ContentValues toCV();

    public abstract String getPrimaryKeyName();

    public abstract Uri getContentUri();

    public int update(final Context context) {
        if (getId() != null) {
            return context.getContentResolver().update(getContentUri(), toCV(), getPrimaryKeyName() + "=?", new String[]{this.id});
        } else return 0;
    }

    public Uri insert(final Context context) {
        return context.getContentResolver().insert(getContentUri(), toCV());
    }

    protected void setId(String id) {
        this.id = id;
    }

    public void save(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                saveSyncronous(context);
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void saveSyncronous(final Context context) {
        int affectedRows;
        if (getId() != null) {
            affectedRows = update(context);
            if (affectedRows == 0) {
                Uri insertUri = insert(context);
                id = String.valueOf(ContentUris.parseId(insertUri));
            }
        } else {
            Uri insertUri = insert(context);
            id = String.valueOf(ContentUris.parseId(insertUri));
        }
    }
}
