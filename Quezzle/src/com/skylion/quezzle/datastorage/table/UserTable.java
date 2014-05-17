package com.skylion.quezzle.datastorage.table;

import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 07.04.14
 * Time: 21:51
 * To change this template use File | Settings | File Templates.
 */
public class UserTable implements BaseColumns {
    public static final String TABLE_NAME = "users";

    public static final String OBJECT_ID_COLUMN = "object_id";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String USERNAME_COLUMN = "username";
    public static final String AVATAR_COLUMN = "avatar";
    public static final String GPLUS_LINK_COLUMN = "gplus_link";
    public static final String IS_ADMIN_COLUMN = "is_admin";

    public static final String[] FULL_PROJECTION = {_ID, OBJECT_ID_COLUMN, CREATED_AT_COLUMN, UPDATED_AT_COLUMN,
                                                    USERNAME_COLUMN, AVATAR_COLUMN, GPLUS_LINK_COLUMN, IS_ADMIN_COLUMN};

    public static final String CREATE_TABLE_SQL = "CREATE TABLE \"" + TABLE_NAME + "\" (\"" +
            _ID + "\" INTEGER PRIMARY KEY, \"" +
            OBJECT_ID_COLUMN + "\" TEXT UNIQUE NOT NULL, \"" +
            CREATED_AT_COLUMN + "\" INTEGER DEFAULT 0, \"" +
            UPDATED_AT_COLUMN + "\" INTEGER DEFAULT 0, \"" +
            USERNAME_COLUMN + "\" TEXT NOT NULL, \"" +
            AVATAR_COLUMN + "\" TEXT, \"" +
            GPLUS_LINK_COLUMN + "\" TEXT, \"" +
            IS_ADMIN_COLUMN  + "\" INTEGER DEFAULT 0);";

    public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS \"" + TABLE_NAME + "\";";
}
