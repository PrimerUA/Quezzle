package com.skylion.quezzle.notification;

import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 03.04.14
 * Time: 23:33
 * To change this template use File | Settings | File Templates.
 */
public class ReloadChatListNotification {
    public static final String ACTION = "com.skylion.quezzle.notification.ReloadChatListNotification.RELOAD_CHAT_LIST";

    private  static final String RESULT_DATA = "com.skylion.quezzle.notification.ReloadChatListNotification.RESULT";
    private  static final String ERROR_MESSAGE_DATA = "com.skylion.quezzle.notification.ReloadChatListNotification.ERROR_MESSAGE";

    public static Intent createSuccessResult() {
        Intent intent = new Intent(ACTION);
        intent.putExtra(RESULT_DATA, true);

        return intent;
    }

    public static Intent createErrorsResult(String errorMessage) {
        Intent intent = new Intent(ACTION);
        intent.putExtra(RESULT_DATA, false);
        intent.putExtra(ERROR_MESSAGE_DATA, errorMessage);

        return intent;
    }

    public static boolean isSuccessful(Intent intent) {
        return intent.getBooleanExtra(RESULT_DATA, false);
    }

    public static String getErrorMessage(Intent intent) {
        if (intent.hasExtra(ERROR_MESSAGE_DATA)) {
            return intent.getStringExtra(ERROR_MESSAGE_DATA);
        } else {
            return "";
        }
    }
}
