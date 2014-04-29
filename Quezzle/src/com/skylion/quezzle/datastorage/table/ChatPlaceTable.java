package com.skylion.quezzle.datastorage.table;

import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 23.03.14
 * Time: 0:34
 * To change this template use File | Settings | File Templates.
 */
public class ChatPlaceTable implements BaseColumns {
    public static final String TABLE_NAME = "chat_places";

    public static final String OBJECT_ID_COLUMN = "object_id";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String NAME_COLUMN = "name";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String IS_SUBSCRIBED_COLUMN = "is_subscribed";
    public static final String SYNC_STATUS_COLUMN = "sync_status";


    public static final String[] FULL_PROJECTION = {_ID, OBJECT_ID_COLUMN, CREATED_AT_COLUMN, UPDATED_AT_COLUMN,
                                                    NAME_COLUMN, DESCRIPTION_COLUMN};

    public static final String CREATE_TABLE_SQL = "CREATE TABLE \"" + TABLE_NAME + "\" (\"" +
            _ID + "\" INTEGER PRIMARY KEY, \"" +
            OBJECT_ID_COLUMN + "\" TEXT UNIQUE NOT NULL, \"" +
            CREATED_AT_COLUMN + "\" INTEGER DEFAULT 0, \"" +
            UPDATED_AT_COLUMN + "\" INTEGER DEFAULT 0, \"" +
            NAME_COLUMN + "\" TEXT NOT NULL, \"" +
            DESCRIPTION_COLUMN + "\" TEXT DEFAULT \"\", \"" +
            IS_SUBSCRIBED_COLUMN + "\" INTEGER DEFAULT 0, \"" +
            SYNC_STATUS_COLUMN + "\" INTEGER DEFAULT 0);";

    public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS \"" + TABLE_NAME + "\";";
}
