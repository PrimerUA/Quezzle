package com.skylion.quezzle.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.skylion.quezzle.service.NetworkService;
import com.skylion.quezzle.utility.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 29.03.14
 * Time: 20:08
 * To change this template use File | Settings | File Templates.
 */
public class NewMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Gson gson = new Gson();
        String data = intent.getExtras().getString("com.parse.Data");
        if (!TextUtils.isEmpty(data)) {
            //parse push data
            PushData pushData = gson.fromJson(data, PushData.class);
            //start updating chat
            NetworkService.refreshChat(context, pushData.chatObjectId, true);
        } else {
            Log.e(Constants.LOG_TAG, "NewMessageReceiver error - empty data");
        }
    }

    private static class PushData {
        String action;
        String chatObjectId;
    }
}
