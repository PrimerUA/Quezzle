package com.skylion.quezzle.network;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datamodel.Message;
import com.skylion.quezzle.datamodel.ChatPlace;
import com.skylion.quezzle.datamodel.QuezzleUserMetadata;
import com.skylion.quezzle.datamodel.Subscriber;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.datastorage.table.MessageTable;
import com.skylion.quezzle.datastorage.table.UserTable;
import com.skylion.quezzle.utility.Constants;
import com.skylion.quezzle.utility.Utils;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 04.04.14
 * Time: 22:21
 * To change this template use File | Settings | File Templates.
 */
public abstract class NetworkHelper {
    private static final int LOAD_MESSAGES_LIMIT = 20;
    private static final int LOAD_CHATS_LIMIT = 20;

    public static void loadAllChats(ContentResolver contentResolver, OnResultListener listener) {
        ParseQuery<ChatPlace> query = ParseQuery.getQuery(ChatPlace.class);
        query.setLimit(LOAD_CHATS_LIMIT);
        int offset = 0;
        try {
            boolean hasMoreData = true;
            while (hasMoreData) {
                query.setSkip(offset);
                List<ChatPlace> chats = query.find();

                if (!chats.isEmpty()) {
                    ContentValues[] values = new ContentValues[chats.size()];
                    for (int i = 0; i < values.length; ++i) {
                        ChatPlace chatPlace = chats.get(i);
                        values[i] = new ContentValues(5);
                        values[i].put(ChatPlaceTable.OBJECT_ID_COLUMN, chatPlace.getObjectId());
                        values[i].put(ChatPlaceTable.CREATED_AT_COLUMN, chatPlace.getCreatedAt().getTime());
                        values[i].put(ChatPlaceTable.UPDATED_AT_COLUMN, chatPlace.getUpdatedAt().getTime());
                        values[i].put(ChatPlaceTable.NAME_COLUMN, chatPlace.getName());
                        values[i].put(ChatPlaceTable.DESCRIPTION_COLUMN, chatPlace.getDescription());
                    }

                    contentResolver.bulkInsert(QuezzleProviderContract.CHAT_PLACES_URI, values);
                }

                offset += chats.size();
                hasMoreData = !chats.isEmpty();
            }

            //emit success result
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (ParseException pe) {
            Log.e(Constants.LOG_TAG, "Error updating chat: " + pe.getMessage());

            //emit error result
            if (listener != null) {
                listener.onError(pe.getLocalizedMessage());
            }
        }
    }

    public static String uploadImage(Bitmap bitmap, String fileName) {
        //get raw data of the image
        byte[] data = Utils.bitmapToBytearray(bitmap);

        ParseFile parseFile = new ParseFile(fileName, data);
        try {
            parseFile.save();
            return parseFile.getUrl();
        } catch (ParseException pe) {
            Log.e(Constants.LOG_TAG, "Error updating image: " + pe.getMessage());

            return null;
        }
    }

    public static int loadAllChatMessages(String chatKey, Uri messagesUri,
                                          Date lastMessageDate, ContentResolver contentResolver,
                                          OnResultListener listener) {
        int createdCount = 0;
        Map<String, ContentValues> users = new HashMap<String, ContentValues>();
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        query.include(Message.AUTHOR_FIELD);
        query.whereEqualTo(Message.CHAT_ID_FIELD, chatKey);
        query.addDescendingOrder("updatedAt");
        if (lastMessageDate != null) {
            query.whereGreaterThan("updatedAt", lastMessageDate);
        }
        query.setLimit(LOAD_MESSAGES_LIMIT);
        int offset = 0;
        try {
            boolean hasMoreData = true;
            while (hasMoreData) {
                query.setSkip(offset);
                List<Message> messages = query.find();

                if (!messages.isEmpty()) {
                    users.clear();
                    ContentValues[] values = new ContentValues[messages.size()];

                    for (int i = 0; i < messages.size(); ++i) {
                        Message message = messages.get(i);
                        ParseUser author = message.getAuthor();

                        if (!users.containsKey(author.getObjectId())) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(UserTable.OBJECT_ID_COLUMN, author.getObjectId());
                            contentValues.put(UserTable.CREATED_AT_COLUMN, author.getCreatedAt().getTime());
                            contentValues.put(UserTable.UPDATED_AT_COLUMN, author.getUpdatedAt().getTime());
                            contentValues.put(UserTable.USERNAME_COLUMN, author.getUsername());
                            contentValues.put(UserTable.AVATAR_COLUMN, author.getString(QuezzleUserMetadata.AVATAR_URL));
                            users.put(author.getObjectId(), contentValues);
                        }

                        values[i] = new ContentValues(6);
                        values[i].put(MessageTable.OBJECT_ID_COLUMN, message.getObjectId());
                        values[i].put(MessageTable.CREATED_AT_COLUMN, message.getCreatedAt().getTime());
                        values[i].put(MessageTable.UPDATED_AT_COLUMN, message.getUpdatedAt().getTime());
                        values[i].put(MessageTable.MESSAGE_COLUMN, message.getMessage());
                        values[i].put(MessageTable.AUTHOR_ID_COLUMN, author.getObjectId());
                    }

                    //insert users
                    Collection<ContentValues> usersCollection = users.values();
                    ContentValues[] usersValues = new ContentValues[usersCollection.size()];
                    contentResolver.bulkInsert(QuezzleProviderContract.USERS_URI, usersCollection.toArray(usersValues));

                    //insert messages
                    createdCount += contentResolver.bulkInsert(messagesUri, values);
                }

                offset += messages.size();
                hasMoreData = !messages.isEmpty();
            }
        } catch (ParseException pe) {
            Log.e(Constants.LOG_TAG, "Error updating chat: " + pe.getMessage());

            //emit error result
            if (listener != null) {
                listener.onError(pe.getLocalizedMessage());
            }
        }

        return createdCount;
    }

    public static boolean subscribeToChat(String chatKey, String subscriberId) {
        Subscriber subscriber = new Subscriber();
        subscriber.setChat(ChatPlace.createWithoutData(ChatPlace.class, chatKey));
        subscriber.setSubscriber(ParseUser.createWithoutData(ParseUser.class, subscriberId));

        try {
            subscriber.save();

            return true;
        } catch (ParseException pe) {
            Log.e(Constants.LOG_TAG, "Error in subscribeToChat: " + pe.getMessage());
        }

        return false;
    }

    public static boolean unsubscribeFromChat(String chatKey, String subscriberId) {
        ParseQuery<Subscriber> query = ParseQuery.getQuery(Subscriber.class);
        query.whereEqualTo(Subscriber.CHAT_FIELD, ChatPlace.createWithoutData(ChatPlace.class, chatKey));
        query.whereEqualTo(Subscriber.SUBSCRIBER_FIELD, ParseUser.createWithoutData(ParseUser.class, subscriberId));
        try {
            List<Subscriber> subscribers = query.find();
            for (Subscriber subscriber : subscribers) {
                subscriber.delete();
            }

            return true;
        } catch (ParseException pe) {
            Log.e(Constants.LOG_TAG, "Error in unsubscribeFromChat: " + pe.getMessage());
        }

        return false;
    }

    public static void sendMessage(String message, String chatId, String authorId, OnResultListener listener) {
        Message chatMessage = new Message();
        chatMessage.setMessage(message);
        chatMessage.setChatId(chatId);
        chatMessage.setAuthor(ParseUser.createWithoutData(ParseUser.class, authorId));

        try {
            chatMessage.save();

            //emit success result
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (ParseException pe) {
            Log.e(Constants.LOG_TAG, "Error sending message: " + pe.getMessage());

            //emit error result
            if (listener != null) {
                listener.onError(pe.getLocalizedMessage());
            }
        }
    }

    public static void createChat(String chatName, String chatDescription,
                                  ContentResolver contentResolver, OnResultListener listener) {
        ChatPlace chatPlace = new ChatPlace();
        chatPlace.setName(chatName);
        chatPlace.setDescription(chatDescription);

        try {
            //save chat in server
            chatPlace.save();

            //save chat in local cache
            ContentValues values = new ContentValues(5);
            values.put(ChatPlaceTable.OBJECT_ID_COLUMN, chatPlace.getObjectId());
            values.put(ChatPlaceTable.CREATED_AT_COLUMN, chatPlace.getCreatedAt().getTime());
            values.put(ChatPlaceTable.UPDATED_AT_COLUMN, chatPlace.getUpdatedAt().getTime());
            values.put(ChatPlaceTable.NAME_COLUMN, chatPlace.getName());
            values.put(ChatPlaceTable.DESCRIPTION_COLUMN, chatPlace.getDescription());
            contentResolver.insert(QuezzleProviderContract.CHAT_PLACES_URI, values);

            //emit success result
            if (listener != null) {
                listener.onSuccess();
            }
        } catch (ParseException pe) {
            Log.e(Constants.LOG_TAG, "Error sending message: " + pe.getMessage());

            //emit error result
            if (listener != null) {
                listener.onError(pe.getLocalizedMessage());
            }
        }
    }

    public interface OnResultListener {
        public void onSuccess();
        public void onError(String message);
    }
}
