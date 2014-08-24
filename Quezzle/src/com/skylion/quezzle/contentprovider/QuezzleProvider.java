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
import android.util.Log;
import com.skylion.quezzle.datastorage.QuezzleSQLStorage;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.datastorage.table.FullMessageTable;
import com.skylion.quezzle.datastorage.table.MessageTable;
import com.skylion.quezzle.datastorage.table.UserTable;

import java.net.URI;

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
    private static final int CHAT_MESSAGES_URI_INDICATOR = 3;
    private static final int CHAT_MESSAGE_URI_INDICATOR = 4;
    private static final int USERS_URI_INDICATOR = 5;
    private static final int USER_URI_INDICATOR = 6;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(QuezzleProviderContract.AUTHORITY, QuezzleProviderContract.CHAT_PLACES_PATH, CHAT_PLACES_URI_INDICATOR);
        uriMatcher.addURI(QuezzleProviderContract.AUTHORITY, QuezzleProviderContract.CHAT_PLACES_PATH + "/*", CHAT_PLACE_URI_INDICATOR);

        uriMatcher.addURI(QuezzleProviderContract.AUTHORITY, QuezzleProviderContract.CHAT_PLACES_PATH + "/*/" +
                                                             QuezzleProviderContract.MESSAGES_PATH, CHAT_MESSAGES_URI_INDICATOR);
        uriMatcher.addURI(QuezzleProviderContract.AUTHORITY, QuezzleProviderContract.CHAT_PLACES_PATH + "/*/" +
                                                             QuezzleProviderContract.MESSAGES_PATH + "/*", CHAT_MESSAGE_URI_INDICATOR);
        uriMatcher.addURI(QuezzleProviderContract.AUTHORITY, QuezzleProviderContract.USERS_PATH, USERS_URI_INDICATOR);
        uriMatcher.addURI(QuezzleProviderContract.AUTHORITY, QuezzleProviderContract.USERS_PATH + "/*", USER_URI_INDICATOR);
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
                queryBuilder.appendWhere(ChatPlaceTable.OBJECT_ID_COLUMN + "=" + addQuotes(uri.getLastPathSegment()));
                break;
            case CHAT_MESSAGES_URI_INDICATOR :
                queryBuilder.setTables(FullMessageTable.TABLE_NAME);
                queryBuilder.setProjectionMap(FullMessageTable.PROJECTION_MAP);
                queryBuilder.appendWhere(FullMessageTable.CHAT_KEY_COLUMN + "=" + addQuotes(uri.getPathSegments().get(1)));
                break;
            case CHAT_MESSAGE_URI_INDICATOR :
                queryBuilder.setTables(FullMessageTable.TABLE_NAME);
                queryBuilder.setProjectionMap(FullMessageTable.PROJECTION_MAP);
                queryBuilder.appendWhere(FullMessageTable.CHAT_KEY_COLUMN + "=" + addQuotes(uri.getPathSegments().get(1)));
                queryBuilder.appendWhere(FullMessageTable.OBJECT_ID_COLUMN + "=" + addQuotes(uri.getLastPathSegment()));
                break;
            case USERS_URI_INDICATOR :
                queryBuilder.setTables(UserTable.TABLE_NAME);
                break;
            case USER_URI_INDICATOR :
                queryBuilder.setTables(UserTable.TABLE_NAME);
                queryBuilder.appendWhere(UserTable.OBJECT_ID_COLUMN + "=" + addQuotes(uri.getLastPathSegment()));
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
                //replace works as insert or update
                rowId = db.replace(ChatPlaceTable.TABLE_NAME, null, values);
                if (rowId > 0)
                {
                    Uri resultUri = Uri.withAppendedPath(uri, values.getAsString(ChatPlaceTable.OBJECT_ID_COLUMN));
                    getContext().getContentResolver().notifyChange(uri, null);
                    return resultUri;
                }
                break;
            case CHAT_MESSAGES_URI_INDICATOR :
                //add chat id if it is not exists
                if (!values.containsKey(MessageTable.CHAT_KEY_COLUMN)) {
                    values.put(MessageTable.CHAT_KEY_COLUMN, uri.getPathSegments().get(1));
                }
                //replace works as insert or update
                rowId = db.replace(MessageTable.TABLE_NAME, null, values);
                if (rowId > 0)
                {
                    Uri resultUri = Uri.withAppendedPath(uri, values.getAsString(MessageTable.OBJECT_ID_COLUMN));
                    getContext().getContentResolver().notifyChange(uri, null);
                    return resultUri;
                }
                break;
            case USERS_URI_INDICATOR :
                //replace works as insert or update
                rowId = db.replace(UserTable.TABLE_NAME, null, values);
                if (rowId > 0)
                {
                    Uri resultUri = Uri.withAppendedPath(uri, values.getAsString(UserTable.OBJECT_ID_COLUMN));
                    getContext().getContentResolver().notifyChange(uri, null);
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
                rowsDeleted = db.delete(ChatPlaceTable.TABLE_NAME, ChatPlaceTable.OBJECT_ID_COLUMN + "=" +
                        addQuotes(uri.getLastPathSegment()) +
                        (hasSelection ? (" AND " + selection) : ""), (hasSelection ? selectionArgs : null));
                break;
            case CHAT_MESSAGES_URI_INDICATOR :
                rowsDeleted = db.delete(MessageTable.TABLE_NAME, MessageTable.CHAT_KEY_COLUMN + "=" +
                                        addQuotes(uri.getPathSegments().get(1)) +
                                        (hasSelection ? (" AND " + selection) : ""), (hasSelection ? selectionArgs : null));
                break;
            case CHAT_MESSAGE_URI_INDICATOR :
                rowsDeleted = db.delete(MessageTable.TABLE_NAME, MessageTable.CHAT_KEY_COLUMN + "=" +
                                        addQuotes(uri.getPathSegments().get(1)) +
                                        " AND " + MessageTable.OBJECT_ID_COLUMN + "=" +
                                        addQuotes(uri.getLastPathSegment()) +
                                        (hasSelection ? (" AND " + selection) : ""), (hasSelection ? selectionArgs : null));
                break;
            case USERS_URI_INDICATOR :
                rowsDeleted = db.delete(UserTable.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_URI_INDICATOR :
                rowsDeleted = db.delete(UserTable.TABLE_NAME, UserTable.OBJECT_ID_COLUMN + "=" +
                        addQuotes(uri.getLastPathSegment()) +
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
            case CHAT_PLACES_URI_INDICATOR :
                rowsUpdated = db.update(ChatPlaceTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CHAT_PLACE_URI_INDICATOR :
                rowsUpdated = db.update(ChatPlaceTable.TABLE_NAME, values, ChatPlaceTable.OBJECT_ID_COLUMN + "=" +
                        addQuotes(uri.getLastPathSegment()) + (hasSelection ? (" AND " + selection) : ""),
                        (hasSelection ? selectionArgs : null));
                break;
            case CHAT_MESSAGES_URI_INDICATOR :
                rowsUpdated = db.update(MessageTable.TABLE_NAME, values,
                        MessageTable.CHAT_KEY_COLUMN + "=" + addQuotes(uri.getPathSegments().get(1)) +
                        (hasSelection ? (" AND " + selection) : ""), (hasSelection ? selectionArgs : null));
                break;
            case CHAT_MESSAGE_URI_INDICATOR :
                rowsUpdated = db.update(MessageTable.TABLE_NAME, values,
                        MessageTable.CHAT_KEY_COLUMN + "=" + addQuotes(uri.getPathSegments().get(1)) +
                        " AND " + MessageTable.OBJECT_ID_COLUMN + "=" + addQuotes(uri.getLastPathSegment()) +
                        (hasSelection ? (" AND " + selection) : ""), (hasSelection ? selectionArgs : null));
                break;
            case USERS_URI_INDICATOR :
                rowsUpdated = db.update(UserTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case USER_URI_INDICATOR :
                rowsUpdated = db.update(UserTable.TABLE_NAME, values, UserTable.OBJECT_ID_COLUMN + "=" +
                        addQuotes(uri.getLastPathSegment()) + (hasSelection ? (" AND " + selection) : ""),
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

    private String addQuotes(String target) {
        return "\"" + target + "\"";
    }
}
