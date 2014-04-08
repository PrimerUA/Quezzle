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

    public static final String CHAT_PLACES_PATH = "chat_places";
    public static final String MESSAGES_PATH = "messages";
    public static final String USERS_PATH = "users";

    public static final Uri CHAT_PLACES_URI = Uri.parse("content://" + AUTHORITY + "/" + CHAT_PLACES_PATH);
    public static final Uri USERS_URI = Uri.parse("content://" + AUTHORITY + "/" + USERS_PATH);

    public static Uri getMessagesUri(String chatKey) {
        return  Uri.parse("content://" + AUTHORITY + "/" + CHAT_PLACES_PATH + "/" + chatKey + "/" + MESSAGES_PATH);
    }
}
