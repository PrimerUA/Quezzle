package com.skylion.quezzle.utility;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 15.04.14
 * Time: 21:36
 * To change this template use File | Settings | File Templates.
 */
public class Utils {
    public static String getImagePathByUri(Context context, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor.getCount() > 0) {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            if (column_index != -1) {
                cursor.moveToFirst();

                return cursor.getString(column_index);
            }
        }

        return "";
    }
}
