package com.ims.content.provider;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class ImsContentProvider extends ContentProvider {
    private static final String LOG_TAG = ImsContentProvider.class.getName();

    private Context context;
    private DatabaseHelper dbHelper;

    public static final UriMatcher URI_MATCHER = buildUriMatcher();

    public static final int ITEM_TOKEN = 0;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Contract.AUTHORITY, Contract.Item.PATH, ITEM_TOKEN);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        context = getContext();
        dbHelper = new DatabaseHelper(context);
        return true;
    }

    private String getContentDirType(Class clazz) {
        return "vnd.android.cursor.dir/vnd." + Contract.AUTHORITY + "." + Contract.getTableName(clazz);
    }

    private String getContentItemType(Class clazz) {
        return "vnd.android.cursor.item/vnd." + Contract.AUTHORITY + "." + Contract.getTableName(clazz);
    }

    @Override
    public String getType(Uri uri) {
        final int token = URI_MATCHER.match(uri);
        switch (token) {
            case ITEM_TOKEN:
                return getContentDirType(Contract.Item.class);
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = URI_MATCHER.match(uri);
        long id = 0;
        switch (token) {
            case ITEM_TOKEN:
                id = db.insertWithOnConflict(Contract.Item.TABLENAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null, false);
                return Contract.Item.CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int token = URI_MATCHER.match(uri);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (token) {
            case ITEM_TOKEN:
                builder.setTables(Contract.Item.TABLENAME);
                break;
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = URI_MATCHER.match(uri);
        int count = 0;
        switch (token) {
            case ITEM_TOKEN:
                count = db.updateWithOnConflict(Contract.Item.TABLENAME,
                        values, selection, selectionArgs, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
        if (count > 0)
            getContext().getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int token = URI_MATCHER.match(uri);
        int count = 0;
        switch (token) {
            case ITEM_TOKEN:
                count = db.delete(Contract.Item.TABLENAME, whereClause, whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
        if (count > 0) getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}