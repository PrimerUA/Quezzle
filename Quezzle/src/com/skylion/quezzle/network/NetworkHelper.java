package com.skylion.quezzle.network;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.parse.*;
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
    private static final int LOAD_SUBSCRIPTION_LIMIT = 100;

    public static void loadAllChats(String subscriberId, ContentResolver contentResolver, OnResultListener listener) {
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
                        values[i] = new ContentValues(9);
                        values[i].put(ChatPlaceTable.OBJECT_ID_COLUMN, chatPlace.getObjectId());
                        values[i].put(ChatPlaceTable.CREATED_AT_COLUMN, chatPlace.getCreatedAt().getTime());
                        values[i].put(ChatPlaceTable.UPDATED_AT_COLUMN, chatPlace.getUpdatedAt().getTime());
                        values[i].put(ChatPlaceTable.NAME_COLUMN, chatPlace.getName());
                        values[i].put(ChatPlaceTable.DESCRIPTION_COLUMN, chatPlace.getDescription());
                        values[i].put(ChatPlaceTable.CHAT_TYPE_COLUMN, chatPlace.getChatType());
                        if (chatPlace.getChatType() == Constants.ChatType.GEO) {
                            ParseGeoPoint location = chatPlace.getLocation();
                            if (location != null) {
                                values[i].put(ChatPlaceTable.LATITUDE_COLUMN, location.getLatitude());
                                values[i].put(ChatPlaceTable.LONGITUDE_COLUMN, location.getLongitude());
                            }
                            values[i].put(ChatPlaceTable.RADIUS_COLUMN, chatPlace.getRadius());
                        }
                    }

                    contentResolver.bulkInsert(QuezzleProviderContract.CHAT_PLACES_URI, values);
                }

                offset += chats.size();
                hasMoreData = !chats.isEmpty();
            }

            updateSubscription(subscriberId, contentResolver);

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

    private static void updateSubscription(String subscriberId, ContentResolver contentResolver) {
        ParseQuery<Subscriber> query = ParseQuery.getQuery(Subscriber.class);
        query.whereEqualTo(Subscriber.SUBSCRIBER_ID_FIELD, subscriberId);
        query.setLimit(LOAD_SUBSCRIPTION_LIMIT);
        int offset = 0;

        boolean hasMoreData = true;
        try {
            while (hasMoreData) {
                query.setSkip(offset);
                List<Subscriber> subscribers = query.find();

                ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(subscribers.size());
                for (Subscriber subscriber : subscribers) {
                    ContentValues values = new ContentValues(1);
                    values.put(ChatPlaceTable.IS_SUBSCRIBED_COLUMN, 1);
                    operations.add(ContentProviderOperation
                                    .newUpdate(Uri.withAppendedPath(QuezzleProviderContract.CHAT_PLACES_URI, subscriber.getChatId()))
                                    .withValues(values)
                                    .build());
                }
                try {
                    contentResolver.applyBatch(QuezzleProviderContract.AUTHORITY, operations);
                } catch (RemoteException re) {
                    Log.e(Constants.LOG_TAG, re.getMessage());
                    re.printStackTrace();
                } catch (OperationApplicationException oae) {
                    Log.e(Constants.LOG_TAG, oae.getMessage());
                    oae.printStackTrace();
                }

                offset += subscribers.size();
                hasMoreData = !subscribers.isEmpty();
            }
        } catch (ParseException pe) {
            Log.e(Constants.LOG_TAG, "Error loading subscription: " + pe.getMessage());
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
                            contentValues.put(UserTable.GPLUS_LINK_COLUMN, author.getString(QuezzleUserMetadata.GPLUS_LINK));
                            contentValues.put(UserTable.IS_ADMIN_COLUMN, author.getBoolean(QuezzleUserMetadata.IS_ADMIN) ? 1 : 0);
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
        subscriber.setChatId(chatKey);
        subscriber.setSubscriberId(subscriberId);

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
        query.whereEqualTo(Subscriber.CHAT_ID_FIELD, chatKey);
        query.whereEqualTo(Subscriber.SUBSCRIBER_ID_FIELD, subscriberId);
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
                                  int chatType, double latitude, double longitude, int radius,
                                  ContentResolver contentResolver, OnResultListener listener) {
        ChatPlace chatPlace = new ChatPlace();
        chatPlace.setName(chatName);
        chatPlace.setDescription(chatDescription);
        chatPlace.setChatType(chatType);
        chatPlace.setLocation(new ParseGeoPoint(latitude, longitude));
        chatPlace.setRadius(radius);

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
