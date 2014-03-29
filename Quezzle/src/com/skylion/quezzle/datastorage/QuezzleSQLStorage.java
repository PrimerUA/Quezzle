package com.skylion.quezzle.datastorage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.datastorage.table.MessageTable;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 23.03.14
 * Time: 0:32
 * To change this template use File | Settings | File Templates.
 */
public class QuezzleSQLStorage extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "quezzle.db";
    private static final int DATABASE_VERSION = 1;

    public QuezzleSQLStorage(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create DB structure
        db.execSQL(ChatPlaceTable.CREATE_TABLE_SQL);
        db.execSQL(MessageTable.CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Nothing to do yet
    }
}
