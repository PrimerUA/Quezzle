package com.skylion.quezzle.datastorage.table;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 08.04.14
 * Time: 22:11
 * To change this template use File | Settings | File Templates.
 */
public class FullMessageTable {
    public static final String TABLE_NAME = "\"" + MessageTable.TABLE_NAME + "\" LEFT OUTER JOIN \"" + UserTable.TABLE_NAME +
            "\" ON \"" + MessageTable.TABLE_NAME + "\".\"" + MessageTable.AUTHOR_ID_COLUMN + "\"=\"" + UserTable.TABLE_NAME +
            "\".\"" + UserTable.OBJECT_ID_COLUMN + "\"";

    public static final String _ID = MessageTable.TABLE_NAME + "." + MessageTable._ID + " as " + MessageTable._ID;
    public static final String OBJECT_ID_COLUMN = MessageTable.TABLE_NAME + "." + MessageTable.OBJECT_ID_COLUMN;
    public static final String CREATED_AT_COLUMN = MessageTable.TABLE_NAME + "." + MessageTable.CREATED_AT_COLUMN;
    public static final String UPDATED_AT_COLUMN = MessageTable.TABLE_NAME + "." + MessageTable.UPDATED_AT_COLUMN;
    public static final String MESSAGE_COLUMN = MessageTable.TABLE_NAME + "." + MessageTable.MESSAGE_COLUMN;
    public static final String AUTHOR_ID_COLUMN = MessageTable.TABLE_NAME + "." + MessageTable.AUTHOR_ID_COLUMN;
    public static final String CHAT_KEY_COLUMN = MessageTable.TABLE_NAME + "." + MessageTable.CHAT_KEY_COLUMN;
    public static final String USERNAME_COLUMN = UserTable.TABLE_NAME + "." + UserTable.USERNAME_COLUMN;
    public static final String USER_AVATAR_COLUMN = UserTable.TABLE_NAME + "." + UserTable.AVATAR_COLUMN;
    public static final String GPLUS_LINK_COLUMN = UserTable.TABLE_NAME + "." + UserTable.GPLUS_LINK_COLUMN;
    public static final String IS_ADMIN_COLUMN = UserTable.TABLE_NAME + "." + UserTable.IS_ADMIN_COLUMN;

    public static final String[] FULL_PROJECTION = {_ID, OBJECT_ID_COLUMN, CREATED_AT_COLUMN, UPDATED_AT_COLUMN,
                                MESSAGE_COLUMN, AUTHOR_ID_COLUMN, CHAT_KEY_COLUMN, USERNAME_COLUMN, USER_AVATAR_COLUMN,
                                GPLUS_LINK_COLUMN, IS_ADMIN_COLUMN};
}
