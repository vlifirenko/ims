package com.ims.content.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.lang.reflect.Field;

public class Contract {
    public static final String AUTHORITY = "com.ims";

    public static final Uri BASE_CONTENT_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .build();

    public static String getTableName(Class clazz) {
        try {
            Field tableNameField = clazz.getField("TABLENAME");
            String result = (String) tableNameField.get(null);
            return result;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Model contract given should have TABLENAME specified");
        } catch (IllegalAccessException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("Have no idea what happend: look at System.err stream");
        }
    }

    public static class Item {
        public static final String TABLENAME = "item";
        public static final String PATH = TABLENAME;
        public static final String PATH_SINGLE = TABLENAME + "/#";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String ID = BaseColumns._ID;
        public static final String CREATED = "created";
        public static final String UPDATED = "updated";
        public static final String TYPE = "type";
        public static final String TAGS = "tags";
        public static final String LAT = "lat";
        public static final String LNG = "lng";
        public static final String EXTENSIONS = "extensions";
        public static final String TEXT = "text";
        public static final String ACCESSED = "accessed";

        public static final String DDL = "CREATE TABLE " + TABLENAME + " ( " +
                ID + "  TEXT PRIMARY KEY, " +
                CREATED + " LONG, " +
                UPDATED + " LONG, " +
                TYPE + " TEXT, " +
                TAGS + " TEXT, " +
                LAT + " DOUBLE, " +
                LNG + " DOUBLE, " +
                EXTENSIONS + " TEXT, " +
                TEXT + " TEXT, " +
                ACCESSED + " TEXT, " +
                "UNIQUE (" + ID + ") ON CONFLICT REPLACE)";
    }

}
