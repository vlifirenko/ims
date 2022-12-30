package com.ims.content.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.ims.content.provider.Contract;
import com.ims.helpers.IOUtils;
import com.ims.helpers.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Item extends Model implements Parcelable {

    public static final String LOG_TAG = Item.class.getName();

    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_CONTACT = "contact";
    public static final String TYPE_PAGE = "page";
    public static final String TYPE_FILE = "file";

    public static final String KEY_FROM = "from";
    public static final String KEY_TO = "to";
    public static final String KEY_URL = "url";
    public static final String KEY_PATH = "path";

    public long created;
    public long updated;
    public String type;
    public List<String> tags = new ArrayList<String>();
    public double lat;
    public double lng;
    public Map extensions = new HashMap<String, String>();
    public String text;
    public long accessed;

    public static final Parcelable.Creator<Item> CREATOR = new
            Parcelable.Creator<Item>() {
                public Item createFromParcel(Parcel in) {
                    return new Item(in);
                }

                public Item[] newArray(int size) {
                    return new Item[size];
                }
            };

    public Item() {
        this.id = UUID.randomUUID().toString();
        this.created = new Date().getTime();
    }

    public Item(Cursor c) {
        this.id = c.getString(c.getColumnIndex(Contract.Item.ID));
        this.created = c.getLong(c.getColumnIndex(Contract.Item.CREATED));
        this.updated = c.getLong(c.getColumnIndex(Contract.Item.UPDATED));
        this.tags = StringUtils.deserializeString(c.getString(c.getColumnIndex(Contract.Item.TAGS)));
        this.type = c.getString(c.getColumnIndex(Contract.Item.TYPE));
        this.lat = c.getDouble(c.getColumnIndex(Contract.Item.LAT));
        this.lng = c.getDouble(c.getColumnIndex(Contract.Item.LNG));
        this.extensions = StringUtils.deserializeMap(c.getString(c.getColumnIndex(Contract.Item.EXTENSIONS)));
        this.text = c.getString(c.getColumnIndex(Contract.Item.TEXT));
        this.accessed = c.getLong(c.getColumnIndex(Contract.Item.ACCESSED));
    }

    public Item(String id) {
        setId(id);
    }

    public String getId() {
        return id;
    }

    public Item(Parcel in) {
        try {
            id = in.readString();
            created = in.readLong();
            updated = in.readLong();
            tags = (ArrayList<String>) in.readSerializable();
            lat = in.readDouble();
            lng = in.readDouble();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                extensions.put(in.readString(), in.readString());
            }
            text = in.readString();
            accessed = in.readLong();
        } catch (Exception e) {
            Log.e(LOG_TAG, "readFromParcel", e);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        try {
            parcel.writeString(id);
            parcel.writeLong(created);
            parcel.writeLong(updated);
            parcel.writeStringList(tags);
            parcel.writeDouble(lat);
            parcel.writeDouble(lng);
            parcel.writeMap(extensions);
            parcel.writeString(text);
            parcel.writeLong(accessed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return this.id + ".json";
    }

    @Override
    public ContentValues toCV() {
        ContentValues cv = new ContentValues();
        cv.put(Contract.Item.ID, this.id);
        cv.put(Contract.Item.CREATED, this.created);
        cv.put(Contract.Item.UPDATED, this.updated);
        cv.put(Contract.Item.TYPE, this.type);
        cv.put(Contract.Item.TAGS, StringUtils.serialize(this.tags));
        cv.put(Contract.Item.LAT, this.lat);
        cv.put(Contract.Item.LNG, this.lng);
        cv.put(Contract.Item.EXTENSIONS, StringUtils.serialize(this.extensions));
        cv.put(Contract.Item.TEXT, this.text);
        cv.put(Contract.Item.ACCESSED, this.accessed);
        return cv;
    }

    @Override
    public String getPrimaryKeyName() {
        return Contract.Item.ID;
    }

    @Override
    public Uri getContentUri() {
        return Contract.Item.CONTENT_URI;
    }

    public boolean saveToDisk() {
        File folder = IOUtils.createAppFolder("");
        if (folder != null) {
            String json = new Gson().toJson(this);
            try {
                File file = new File(folder, this.getId() + ".json");
                return IOUtils.saveFile(file, json);
            } catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
                return false;
            }
        } else {
            return false;
        }
    }

}
