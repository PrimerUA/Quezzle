package com.skylion.quezzle.service;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.skylion.quezzle.R;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.datastorage.table.FullMessageTable;
import com.skylion.quezzle.datastorage.table.MessageTable;
import com.skylion.quezzle.network.NetworkHelper;
import com.skylion.quezzle.notification.CreateChatNotification;
import com.skylion.quezzle.notification.ReloadChatListNotification;
import com.skylion.quezzle.notification.SendMessageNotification;
import com.skylion.quezzle.ui.activity.ChatActivity;

import java.util.Date;

/**
 * Created with IntelliJ IDEA. User: roman Date: 3/28/14 Time: 5:21 PM To change
 * this template use File | Settings | File Templates.
 */
public class NetworkService extends IntentService {
	public static final String NEW_MESSAGE_ACTION = "com.skylion.quezzle.service.NetworkService.NEW_MESSAGE_ACTION";
    public static final String CHAT_KEY_EXTRA = "com.skylion.quezzle.service.NetworkService.CHAT_KEY";

	private static final String ACTION_EXTRA = "com.skylion.quezzle.service.NetworkService.ACTION";
	private static final String CHAT_NAME_EXTRA = "com.skylion.quezzle.service.NetworkService.CHAT_NAME";
	private static final String CHAT_DESCRIPTION_EXTRA = "com.skylion.quezzle.service.NetworkService.CHAT_DESCRIPTION";
	private static final String AUTHOR_EXTRA = "com.skylion.quezzle.service.NetworkService.AUTHOR";
	private static final String MESSAGE_EXTRA = "com.skylion.quezzle.service.NetworkService.MESSAGE";
	private static final String WITH_NOTIFICATION_EXTRA = "com.skylion.quezzle.service.NetworkService.WITH_NOTIFICATION";

	private static final int NEW_MESSAGE_NOTIFICATION_ID = 1;

	private static final int ACTION_SEND_MESSAGE = 0;
	private static final int ACTION_RELOAD_CHAT = 1;
	private static final int ACTION_REFRESH_CHAT = 2;
    private static final int ACTION_CREATE_CHAT = 3;
    private static final int ACTION_RELOAD_CHAT_LIST = 4;

    public static void reloadChatList(Context context) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(ACTION_EXTRA, ACTION_RELOAD_CHAT_LIST);

        context.startService(intent);
    }

    public static void createChat(Context context, String chatName, String chatDescription) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(ACTION_EXTRA, ACTION_CREATE_CHAT);
        intent.putExtra(CHAT_NAME_EXTRA, chatName);
        intent.putExtra(CHAT_DESCRIPTION_EXTRA, chatDescription);

        context.startService(intent);
    }

    public static void sendMessage(Context context, String chatKey, String message, String authorId) {
		Intent intent = new Intent(context, NetworkService.class);
		intent.putExtra(ACTION_EXTRA, ACTION_SEND_MESSAGE);
		intent.putExtra(CHAT_KEY_EXTRA, chatKey);
		intent.putExtra(MESSAGE_EXTRA, message);
		intent.putExtra(AUTHOR_EXTRA, authorId);

		context.startService(intent);
	}

	public static void reloadChat(Context context, String chatKey) {
		Intent intent = new Intent(context, NetworkService.class);
		intent.putExtra(ACTION_EXTRA, ACTION_RELOAD_CHAT);
		intent.putExtra(CHAT_KEY_EXTRA, chatKey);

		context.startService(intent);
	}

	public static void refreshChat(Context context, String chatKey, boolean withNotification) {
		Intent intent = new Intent(context, NetworkService.class);
		intent.putExtra(ACTION_EXTRA, ACTION_REFRESH_CHAT);
		intent.putExtra(CHAT_KEY_EXTRA, chatKey);
		intent.putExtra(WITH_NOTIFICATION_EXTRA, withNotification);

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
            case ACTION_CREATE_CHAT :
                doCreateChat(intent);
                break;
            case ACTION_RELOAD_CHAT_LIST :
                doReloadChatList(intent);
                break;
		}
	}

	private void doSendMessage(Intent intent) {
        NetworkHelper.sendMessage(intent.getStringExtra(MESSAGE_EXTRA), intent.getStringExtra(CHAT_KEY_EXTRA),
                                  intent.getStringExtra(AUTHOR_EXTRA), new NetworkHelper.OnResultListener() {
            @Override
            public void onSuccess() {
                //notify UI about success creating chat
                sendLocalBroadcast(SendMessageNotification.createSuccessResult());
            }

            @Override
            public void onError(String message) {
                sendLocalBroadcast(SendMessageNotification.createErrorsResult(message));
            }
        });
	}

    private void doCreateChat(Intent intent) {
        NetworkHelper.createChat(intent.getStringExtra(CHAT_NAME_EXTRA), intent.getStringExtra(CHAT_DESCRIPTION_EXTRA),
                                 getContentResolver(), new NetworkHelper.OnResultListener() {
            @Override
            public void onSuccess() {
                //notify UI about success creating chat
                sendLocalBroadcast(CreateChatNotification.createSuccessResult());
            }

            @Override
            public void onError(String message) {
                //notify UI about error while creating chat
                sendLocalBroadcast(CreateChatNotification.createErrorsResult(message));
            }
        });
    }

	private void doRefreshChat(Intent intent) {
		// load needed data from db
		final String chatKey = intent.getStringExtra(CHAT_KEY_EXTRA);
		Date lastMessageDate = getChatLastMessageDate(chatKey);
		Uri messagesUri = QuezzleProviderContract.getMessagesUri(chatKey);

		// load new data
		int createdCount = NetworkHelper.loadAllChatMessages(chatKey, messagesUri, lastMessageDate,
                                                             getContentResolver(), null);

		// show notification if needed
		if (createdCount > 0 && intent.getBooleanExtra(WITH_NOTIFICATION_EXTRA, false)) {
			Intent broadcastIntent = new Intent(NEW_MESSAGE_ACTION);
			broadcastIntent.putExtra(CHAT_KEY_EXTRA, chatKey);
			sendOrderedBroadcast(broadcastIntent, null, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (getResultCode() != Activity.RESULT_OK) {
                        showNewMessageNotification(chatKey);
                    }
                }
            }, null, 0, null, null);
		}
	}

	private void showNewMessageNotification(String chatKey) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setAutoCancel(true)
				.setSmallIcon(R.drawable.ic_launcher).setContentTitle(getString(R.string.new_message))
				.setContentText(getString(R.string.chat_has_new_message, getChatName(chatKey)))
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setVibrate(new long[]{1000, 500, 500});
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = ChatActivity.getIntent(this, chatKey);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(NEW_MESSAGE_NOTIFICATION_ID, builder.build());
	}

    private void doReloadChatList(Intent intent) {
        // clear local cache
        getContentResolver().delete(QuezzleProviderContract.CHAT_PLACES_URI, null, null);

        // query all chats
        NetworkHelper.loadAllChats(getContentResolver(), new NetworkHelper.OnResultListener() {
            @Override
            public void onSuccess() {
                //notify UI about success reloading chats
                sendLocalBroadcast(ReloadChatListNotification.createSuccessResult());
            }

            @Override
            public void onError(String message) {
                //notify UI about error while reloading chats
                sendLocalBroadcast(ReloadChatListNotification.createErrorsResult(message));
            }
        });
    }

	private void doReloadChat(Intent intent) {
        String chatKey = intent.getStringExtra(CHAT_KEY_EXTRA);
        Uri messagesUri = QuezzleProviderContract.getMessagesUri(chatKey);

		// remove old data
		getContentResolver().delete(messagesUri, null, null);

		// load new data
        NetworkHelper.loadAllChatMessages(chatKey, messagesUri, null, getContentResolver(), null);
	}

	private Date getChatLastMessageDate(String chatKey) {
		Cursor cursor = getContentResolver().query(QuezzleProviderContract.getMessagesUri(chatKey),
				new String[] { FullMessageTable.UPDATED_AT_COLUMN }, null, null, FullMessageTable.UPDATED_AT_COLUMN + " DESC");
		try {
			if (cursor.moveToFirst()) {
				return new Date(cursor.getLong(cursor.getColumnIndex(FullMessageTable.UPDATED_AT_COLUMN)));
			} else {
				return null;
			}
		} finally {
			cursor.close();
		}
	}

	private String getChatName(String chatKey) {
		Uri uri = Uri.withAppendedPath(QuezzleProviderContract.CHAT_PLACES_URI, chatKey);
		Cursor cursor = getContentResolver().query(uri, new String[] { ChatPlaceTable.NAME_COLUMN }, null, null, null);
		try {
			if (cursor.moveToFirst()) {
				return cursor.getString(cursor.getColumnIndex(ChatPlaceTable.NAME_COLUMN));
			} else {
				return "?";
			}
		} finally {
			cursor.close();
		}
	}

    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(NetworkService.this).sendBroadcast(intent);
    }
}
