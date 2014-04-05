package com.skylion.quezzle.datastorage.table;

import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 28.03.14
 * Time: 23:17
 * To change this template use File | Settings | File Templates.
 */
public class MessageTable implements BaseColumns {
    public static final String TABLE_NAME = "messages";

    public static final String OBJECT_ID_COLUMN = "object_id";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String MESSAGE_COLUMN = "message";
    public static final String AUTHOR_COLUMN = "author";
    public static final String CHAT_KEY_COLUMN = "chat_key";


    public static final String[] FULL_PROJECTION = {_ID, OBJECT_ID_COLUMN, CREATED_AT_COLUMN, UPDATED_AT_COLUMN,
            MESSAGE_COLUMN, CHAT_KEY_COLUMN, AUTHOR_COLUMN};

    public static final String CREATE_TABLE_SQL = "CREATE TABLE \"" + TABLE_NAME + "\" (\"" +
            _ID + "\" INTEGER PRIMARY KEY, \"" +
            OBJECT_ID_COLUMN + "\" TEXT UNIQUE NOT NULL, \"" +
            CREATED_AT_COLUMN + "\" INTEGER DEFAULT 0, \"" +
            UPDATED_AT_COLUMN + "\" INTEGER DEFAULT 0, \"" +
            MESSAGE_COLUMN + "\" TEXT NOT NULL, \"" +
            CHAT_KEY_COLUMN + "\" TEXT NOT NULL, \"" +
            AUTHOR_COLUMN + "\" TEXT NOT NULL);";

    public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS \"" + TABLE_NAME + "\";";
}
