package com.skylion.quezzle.contentprovider.observer;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import com.skylion.quezzle.QuezzleApplication;
import com.skylion.quezzle.service.NetworkService;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 27.04.14
 * Time: 17:43
 * To change this template use File | Settings | File Templates.
 */
public class ChatPlacesObserver extends ContentObserver {
    public ChatPlacesObserver() {
        super(null);
    }

    @Override
    public void onChange(boolean selfChange) {
        /*
         * Invoke the method signature available as of
         * Android platform version 4.1, with a null URI.
         */
        onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri changeUri) {
        NetworkService.uploadChatSubscriptions(QuezzleApplication.getApplication());
    }
}
