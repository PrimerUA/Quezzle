package com.skylion.quezzle.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.datastorage.table.MessageTable;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roman
 * Date: 3/28/14
 * Time: 5:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class NetworkService extends IntentService {
    private static final String ACTION_EXTRA = "com.skylion.quezzle.service.NetworkService.ACTION";
    private static final String CHAT_KEY_EXTRA = "com.skylion.quezzle.service.NetworkService.CHAT_KEY";
    private static final String MESSAGE_EXTRA = "com.skylion.quezzle.service.NetworkService.MESSAGE";
    private static final int LOAD_MESSAGES_LIMIT = 20;

    private static final int ACTION_SEND_MESSAGE = 0;
    private static final int ACTION_RELOAD_CHAT = 1;
    private static final int ACTION_REFRESH_CHAT = 2;

    public static void sendMessage(Context context, String chatKey, String message) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(ACTION_EXTRA, ACTION_SEND_MESSAGE);
        intent.putExtra(CHAT_KEY_EXTRA, chatKey);
        intent.putExtra(MESSAGE_EXTRA, message);

        context.startService(intent);
    }

    public static void reloadChat(Context context, String chatKey) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(ACTION_EXTRA, ACTION_RELOAD_CHAT);
        intent.putExtra(CHAT_KEY_EXTRA, chatKey);

        context.startService(intent);
    }

    public static void refreshChat(Context context, String chatKey) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(ACTION_EXTRA, ACTION_REFRESH_CHAT);
        intent.putExtra(CHAT_KEY_EXTRA, chatKey);

        context.startService(intent);
    }

    public NetworkService() {
        super("NetworkService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getIntExtra(ACTION_EXTRA, -1)) {
            case ACTION_SEND_MESSAGE :
                doSendMessage(intent);
                break;
            case ACTION_RELOAD_CHAT :
                doReloadChat(intent);
                break;
            case ACTION_REFRESH_CHAT :
                doRefreshChat(intent);
                break;
        }
    }

    private void doSendMessage(Intent intent) {
        ParseObject chatMessage = new ParseObject("ChatMessage");
        chatMessage.put("message", intent.getStringExtra(MESSAGE_EXTRA));
        chatMessage.put("chatId", intent.getStringExtra(CHAT_KEY_EXTRA));

        try {
            chatMessage.save();
        } catch (ParseException pe) {
            Log.e("KVEST_TAG", "Error sending message: " + pe.getMessage());
        }
    }

    private void doRefreshChat(Intent intent) {
        //load needed data from db
        String chatKey = intent.getStringExtra(CHAT_KEY_EXTRA);
        long chatId = getChatIdByKey(chatKey);
        Date lastMessageDate = getChatLastMessageDate(chatId);
        Uri messagesUri = QuezzleProviderContract.getMessagesUri(getChatIdByKey(chatKey));

        //load new data
        loadChatMessages(chatKey, messagesUri, lastMessageDate);
    }

    private void doReloadChat(Intent intent) {
        String chatKey = intent.getStringExtra(CHAT_KEY_EXTRA);
        Uri messagesUri = QuezzleProviderContract.getMessagesUri(getChatIdByKey(chatKey));

        //remove old data
        getContentResolver().delete(messagesUri, null, null);

        //load new data
        loadChatMessages(chatKey, messagesUri, null);
    }

    private void loadChatMessages(String chatKey, Uri messagesUri, Date lastMessageDate) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatMessage");
        query.whereEqualTo("chatId", chatKey);
        query.addAscendingOrder("updatedAt");
        if (lastMessageDate != null) {
            query.whereGreaterThan("updatedAt", lastMessageDate);
        }
        query.setLimit(LOAD_MESSAGES_LIMIT);
        int offset = 0;
        try {
            boolean hasMoreData = true;
            while (hasMoreData) {
                query.setSkip(offset);
                List<ParseObject> messages = query.find();

                if (!messages.isEmpty()) {
                    ContentValues[] values = new ContentValues[messages.size()];

                    for(int i = 0; i < messages.size(); ++i) {
                        ParseObject message = messages.get(i);

                        values[i] = new ContentValues(4);
                        values[i].put(MessageTable.OBJECT_ID_COLUMN, message.getObjectId());
                        values[i].put(MessageTable.CREATED_AT_COLUMN, message.getCreatedAt().getTime());
                        values[i].put(MessageTable.UPDATED_AT_COLUMN, message.getUpdatedAt().getTime());
                        values[i].put(MessageTable.MESSAGE_COLUMN, message.getString("message"));
                    }

                    getContentResolver().bulkInsert(messagesUri, values);
                }

                offset += messages.size();
                hasMoreData = !messages.isEmpty();
            }
        } catch (ParseException pe) {
            Log.e("KVEST_TAG", "Error updating chat: " + pe.getMessage());
        }
    }

    private long getChatIdByKey(String chatKey) {
        Cursor cursor = getContentResolver().query(QuezzleProviderContract.CHAT_PLACES_URI, new String[]{ChatPlaceTable._ID},
                                                   ChatPlaceTable.OBJECT_ID_COLUMN + "=?", new String[]{chatKey}, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(ChatPlaceTable._ID));
            } else {
                return -1;
            }
        } finally {
            cursor.close();
        }
    }

    private Date getChatLastMessageDate(long chatId) {
        Cursor cursor = getContentResolver().query(QuezzleProviderContract.getMessagesUri(chatId),
                                                   new String[]{MessageTable.UPDATED_AT_COLUMN}, null,
                                                   null, MessageTable.UPDATED_AT_COLUMN + " DESC");
        try {
            if (cursor.moveToFirst()) {
                return new Date(cursor.getLong(cursor.getColumnIndex(MessageTable.UPDATED_AT_COLUMN)));
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }
}
