package com.skylion.quezzle.datastorage.table;

import android.provider.BaseColumns;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 08.04.14
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */
public class FullMessageTable implements BaseColumns {
    public static final String TABLE_NAME = "\"" + MessageTable.TABLE_NAME + "\" LEFT OUTER JOIN \"" + UserTable.TABLE_NAME +
            "\" ON \"" + MessageTable.TABLE_NAME + "\".\"" + MessageTable.AUTHOR_ID_COLUMN + "\"=\"" + UserTable.TABLE_NAME +
            "\".\"" + UserTable.OBJECT_ID_COLUMN + "\"";

    public static final String OBJECT_ID_COLUMN = "object_id";
    public static final String CREATED_AT_COLUMN = "created_at";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String MESSAGE_COLUMN = "message";
    public static final String AUTHOR_ID_COLUMN = "author_id";
    public static final String CHAT_KEY_COLUMN = "chat_key";
    public static final String USERNAME_COLUMN = "username";
    public static final String USER_AVATAR_COLUMN = "avatar";
    public static final String GPLUS_LINK_COLUMN = "gplus_link";
    public static final String IS_ADMIN_COLUMN = "is_admin";

    public static final Map<String, String> PROJECTION_MAP = new HashMap<String, String>(11);
    static {
        PROJECTION_MAP.put(_ID, MessageTable.TABLE_NAME + "." + MessageTable._ID);
        PROJECTION_MAP.put(OBJECT_ID_COLUMN, MessageTable.TABLE_NAME + "." + MessageTable.OBJECT_ID_COLUMN);
        PROJECTION_MAP.put(CREATED_AT_COLUMN, MessageTable.TABLE_NAME + "." + MessageTable.CREATED_AT_COLUMN);
        PROJECTION_MAP.put(UPDATED_AT_COLUMN, MessageTable.TABLE_NAME + "." + MessageTable.UPDATED_AT_COLUMN);
        PROJECTION_MAP.put(MESSAGE_COLUMN, MessageTable.TABLE_NAME + "." + MessageTable.MESSAGE_COLUMN);
        PROJECTION_MAP.put(AUTHOR_ID_COLUMN, MessageTable.TABLE_NAME + "." + MessageTable.AUTHOR_ID_COLUMN);
        PROJECTION_MAP.put(CHAT_KEY_COLUMN, MessageTable.TABLE_NAME + "." + MessageTable.CHAT_KEY_COLUMN);
        PROJECTION_MAP.put(USERNAME_COLUMN, UserTable.TABLE_NAME + "." + UserTable.USERNAME_COLUMN);
        PROJECTION_MAP.put(USER_AVATAR_COLUMN, UserTable.TABLE_NAME + "." + UserTable.AVATAR_COLUMN);
        PROJECTION_MAP.put(GPLUS_LINK_COLUMN, UserTable.TABLE_NAME + "." + UserTable.GPLUS_LINK_COLUMN);
        PROJECTION_MAP.put(IS_ADMIN_COLUMN, UserTable.TABLE_NAME + "." + UserTable.IS_ADMIN_COLUMN);
    }

    public static final String MESSAGES_ORDER_DESC = MessageTable.TABLE_NAME + "." + MessageTable.UPDATED_AT_COLUMN + " DESC";
}
