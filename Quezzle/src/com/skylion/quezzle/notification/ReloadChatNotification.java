package com.skylion.quezzle.notification;

import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 12.06.14
 * Time: 22:38
 * To change this template use File | Settings | File Templates.
 */
public class ReloadChatNotification {
    public static final String ACTION = "com.skylion.quezzle.notification.ReloadChatNotification.RELOAD_CHAT";

    public static Intent createResult() {
        Intent intent = new Intent(ACTION);

        return intent;
    }
}
