package com.skylion.quezzle.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import com.parse.ParseUser;
import com.skylion.quezzle.R;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.datastorage.table.MessageTable;
import com.skylion.quezzle.notification.SendMessageNotification;
import com.skylion.quezzle.service.NetworkService;
import com.skylion.quezzle.ui.adapter.MessageListAdapter;

/**
 * Created with IntelliJ IDEA. User: Kvest Date: 24.03.14 Time: 23:10 To change
 * this template use File | Settings | File Templates.
 */
public class ChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String CHAT_MESSAGES_ORDER = MessageTable.UPDATED_AT_COLUMN + " DESC";
	private static final int LOAD_CHAT_INFO_ID = 0;
	private static final int LOAD_MESSAGES_ID = 1;
	private static final String CHAT_KEY_ARGUMENT = "com.skylion.quezzle.ui.fragment.ChatFragment.CHAT_KEY";

    private boolean firstLoad = true;

	public static ChatFragment newInstance(String chatKey) {
		Bundle arguments = new Bundle();
		arguments.putString(CHAT_KEY_ARGUMENT, chatKey);

		ChatFragment result = new ChatFragment();
		result.setArguments(arguments);
		return result;
	}

    private String chatKey = null;
	private ImageView send;
	private EditText message;
	private ListView messageList;
	private MessageListAdapter messageListAdapter;

	private NewMessageEventReceiver receiver = new NewMessageEventReceiver();
    private SendMessageNotificationReceiver sendMessageNotificationReceiver = new SendMessageNotificationReceiver();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		getActivity().getActionBar().setHomeButtonEnabled(true);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));

		View rootView = inflater.inflate(R.layout.chat_fragment, container, false);

		send = (ImageView) rootView.findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
		message = (EditText) rootView.findViewById(R.id.message);
		messageList = (ListView) rootView.findViewById(R.id.messages_list);
		messageListAdapter = new MessageListAdapter(getActivity(), MessageListAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		messageList.setAdapter(messageListAdapter);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(receiver, new IntentFilter(NetworkService.NEW_MESSAGE_ACTION));
        getActivity().registerReceiver(sendMessageNotificationReceiver, new IntentFilter(SendMessageNotification.ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(sendMessageNotificationReceiver);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(LOAD_CHAT_INFO_ID, null, this);
		getLoaderManager().initLoader(LOAD_MESSAGES_ID, null, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.main, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			NetworkService.reloadChat(getActivity(), getChatKey());
			return true;
        case android.R.id.home :
            getActivity().finish();
            return true;
		}

        return super.onOptionsItemSelected(item);
	}

	private void sendMessage() {
		final String text = message.getText().toString().trim();
		if (!TextUtils.isEmpty(text)) {
			ParseUser user = ParseUser.getCurrentUser();
            if (user != null) {
                NetworkService.sendMessage(getActivity(), getChatKey(), text, user.getUsername());
            } else {
                Toast.makeText(getActivity(), R.string.not_logged_id, Toast.LENGTH_LONG).show();
            }
            message.setText("");
		} else {
			Toast.makeText(getActivity(), getString(R.string.empty_message), Toast.LENGTH_SHORT).show();
		}
	}

	private String getChatKey() {
        if (chatKey == null) {
            Bundle arguments = getArguments();
            chatKey = (arguments != null && arguments.containsKey(CHAT_KEY_ARGUMENT)) ? arguments.getString(CHAT_KEY_ARGUMENT) : "";
        }

		return chatKey;
	}

	private void setChatInfo(Cursor cursor) {
		if (cursor.moveToFirst()) {
            String chatName = cursor.getString(cursor.getColumnIndex(ChatPlaceTable.NAME_COLUMN));
            getActivity().getActionBar().setTitle(chatName);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOAD_CHAT_INFO_ID :
			Uri uri = Uri.withAppendedPath(QuezzleProviderContract.CHAT_PLACES_URI, getChatKey());
			return new CursorLoader(getActivity(), uri, new String[] {ChatPlaceTable.NAME_COLUMN }, null, null, null);
		case LOAD_MESSAGES_ID :
			return new CursorLoader(getActivity(), QuezzleProviderContract.getMessagesUri(getChatKey()),
                                    MessageListAdapter.PROJECTION, null, null,CHAT_MESSAGES_ORDER);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
            case LOAD_CHAT_INFO_ID :
                setChatInfo(cursor);
                break;
            case LOAD_MESSAGES_ID:
                if (firstLoad && cursor.getCount() == 0) {
                    // try to load chat messages
                    NetworkService.reloadChat(getActivity(), getChatKey());
                }
                firstLoad = false;

                messageListAdapter.swapCursor(cursor);
                break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
            case LOAD_MESSAGES_ID:
                messageListAdapter.swapCursor(null);
                break;
		}
	}

	private class NewMessageEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getChatKey().equals(intent.getStringExtra(NetworkService.CHAT_KEY_EXTRA))) {
				setResultCode(Activity.RESULT_OK);
				abortBroadcast();
			}
		}
	}

    private class SendMessageNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!SendMessageNotification.isSuccessful(intent)) {
                Toast.makeText(getActivity(), getString(R.string.error_sending_message,
                                SendMessageNotification.getErrorMessage(intent)), Toast.LENGTH_LONG).show();
            }
        }
    }
}
