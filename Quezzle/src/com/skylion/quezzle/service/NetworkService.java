package com.skylion.quezzle.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * Created with IntelliJ IDEA.
 * User: roman
 * Date: 3/28/14
 * Time: 5:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetworkService extends IntentService {
    private static final String ACTION_EXTRA = "com.skylion.quezzle.service.NetworkService.ACTION";
    private static final String CHAT_ID_EXTRA = "com.skylion.quezzle.service.NetworkService.CHAT_ID";
    private static final String MESSAGE_EXTRA = "com.skylion.quezzle.service.NetworkService.MESSAGE";

    private static final int ACTION_SEND_MESSAGE = 0;
    private static final int ACTION_GET_MESSAGES = 1;

    public static void sendMessage(Context context, String chatId, String message) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(ACTION_EXTRA, ACTION_SEND_MESSAGE);
        intent.putExtra(CHAT_ID_EXTRA, chatId);
        intent.putExtra(MESSAGE_EXTRA, message);

        context.startService(intent);
    }

    public NetworkService() {
        super("NetworkService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getIntExtra(ACTION_EXTRA, -1)) {
            case ACTION_SEND_MESSAGE :
                sendMessage(intent);
                break;
        }
    }

    private void sendMessage(Intent intent) {
        ParseObject chatMessage = new ParseObject("ChatMessage");
        chatMessage.put("message", intent.getStringExtra(MESSAGE_EXTRA));
        chatMessage.put("chatId", intent.getStringExtra(CHAT_ID_EXTRA));

        try {
            chatMessage.save();
        } catch (ParseException pe) {
            Log.e("KVEST_TAG", "Error sending message: " + pe.getMessage());
        }
    }
}
