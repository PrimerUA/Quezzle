package com.skylion.quezzle.contentprovider;

import android.net.Uri;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 23.03.14
 * Time: 0:26
 * To change this template use File | Settings | File Templates.
 */
public abstract class QuezzleProviderContract {
    public static final String AUTHORITY = "com.skylion.quezzle.contentprovider.QuezzleProvider";

    public static final String CHAT_PLACES_PATH = "chat_place";

    public static final Uri CHAT_PLACES_URI = Uri.parse("content://" + AUTHORITY + "/" + CHAT_PLACES_PATH);
}
