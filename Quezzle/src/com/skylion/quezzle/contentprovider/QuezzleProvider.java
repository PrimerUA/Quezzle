package com.skylion.quezzle.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import com.skylion.quezzle.datastorage.QuezzleSQLStorage;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 23.03.14
 * Time: 0:16
 * To change this template use File | Settings | File Templates.
 */
public class QuezzleProvider extends ContentProvider {
    private QuezzleSQLStorage sqlStorage;

    private static final int CHAT_PLACES_URI_INDICATOR = 1;
    private static final int CHAT_PLACE_URI_INDICATOR = 2;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(QuezzleProviderContract.AUTHORITY, QuezzleProviderContract.CHAT_PLACES_PATH, CHAT_PLACES_URI_INDICATOR);
        uriMatcher.addURI(QuezzleProviderContract.AUTHORITY, QuezzleProviderContract.CHAT_PLACES_PATH + "/#", CHAT_PLACE_URI_INDICATOR);
    }

    @Override
    public boolean onCreate() {
        sqlStorage = new QuezzleSQLStorage(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case CHAT_PLACES_URI_INDICATOR :
                queryBuilder.setTables(ChatPlaceTable.TABLE_NAME);
                break;
            case CHAT_PLACE_URI_INDICATOR :
                queryBuilder.setTables(ChatPlaceTable.TABLE_NAME);
                queryBuilder.appendWhere(ChatPlaceTable._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown uri = " + uri);
        }

        //make query
        SQLiteDatabase db = sqlStorage.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = sqlStorage.getWritableDatabase();
        long rowId = 0;

        switch (uriMatcher.match(uri)) {

            case CHAT_PLACES_URI_INDICATOR :
                rowId = db.insert(ChatPlaceTable.TABLE_NAME, null, values);
                if (rowId > 0)
                {
                    Uri resultUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(resultUri, null);
                    return resultUri;
                }
                break;
        }

        throw new IllegalArgumentException("Faild to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;
        boolean hasSelection = !TextUtils.isEmpty(selection);
        SQLiteDatabase db = sqlStorage.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case CHAT_PLACES_URI_INDICATOR :
                rowsDeleted = db.delete(ChatPlaceTable.TABLE_NAME, selection, selectionArgs);
                break;
            case CHAT_PLACE_URI_INDICATOR :
                rowsDeleted = db.delete(ChatPlaceTable.TABLE_NAME, ChatPlaceTable._ID + "=" + uri.getLastPathSegment() +
                        (hasSelection ? (" AND " + selection) : ""), (hasSelection ? selectionArgs : null));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        boolean hasSelection = !TextUtils.isEmpty(selection);
        SQLiteDatabase db = sqlStorage.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case CHAT_PLACE_URI_INDICATOR :
                rowsUpdated = db.update(ChatPlaceTable.TABLE_NAME, values, ChatPlaceTable._ID + "=" +
                        uri.getLastPathSegment() + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("QuezzleProvider doesn't implements getType() method");
    }
}
