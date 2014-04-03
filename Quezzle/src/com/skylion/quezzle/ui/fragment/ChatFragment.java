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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.skylion.quezzle.R;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.datastorage.table.MessageTable;
import com.skylion.quezzle.service.NetworkService;
import com.skylion.quezzle.ui.adapter.MessageListAdapter;

/**
 * Created with IntelliJ IDEA. User: Kvest Date: 24.03.14 Time: 23:10 To change
 * this template use File | Settings | File Templates.
 */
public class ChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final String CHAT_MESSAGES_ORDER = MessageTable.UPDATED_AT_COLUMN + " DESC";
	private static final int LOAD_CHAT_KEY_ID = 0;
	private static final int LOAD_MESSAGES_ID = 1;
	private static final String CHAT_ID_ARGUMENT = "com.skylion.quezzle.ui.fragment.ChatFragment.CHAT_ID";

    private boolean wasReloaded = false;

	public static ChatFragment newInstance(long chatId) {
		Bundle arguments = new Bundle();
		arguments.putLong(CHAT_ID_ARGUMENT, chatId);

		ChatFragment result = new ChatFragment();
		result.setArguments(arguments);
		return result;
	}

	private String chatKey;
	private ImageButton send;
	private EditText message;
	private ListView messageList;
	private MessageListAdapter messageListAdapter;

	private NewMessageEventReceiver receiver = new NewMessageEventReceiver();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);

		getActivity().getActionBar().setHomeButtonEnabled(true);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));

		View rootView = inflater.inflate(R.layout.chat_fragment, container, false);

		send = (ImageButton) rootView.findViewById(R.id.send);
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
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(receiver);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(LOAD_CHAT_KEY_ID, null, this);
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
			NetworkService.reloadChat(getActivity(), getChatId());
			return true;
		default:
			getActivity().finish();
			return super.onOptionsItemSelected(item);
		}
	}

	private void sendMessage() {
		if (!TextUtils.isEmpty(message.getText().toString().trim())) {
			message.setText("");
			ParseUser user = ParseUser.getCurrentUser();
			user.fetchInBackground(new GetCallback<ParseUser>() {

				@Override
				public void done(ParseUser parseUser, ParseException arg1) {
					NetworkService.sendMessage(getActivity(), chatKey, message.getText().toString(), parseUser.getUsername());
				}
			});
		} else {
			Toast.makeText(getActivity(), getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
		}
	}

	private long getChatId() {
		Bundle arguments = getArguments();
		return (arguments != null && arguments.containsKey(CHAT_ID_ARGUMENT)) ? arguments.getLong(CHAT_ID_ARGUMENT) : -1;
	}

	private void setChatData(Cursor cursor) {
		if (cursor.moveToFirst()) {
			chatKey = cursor.getString(cursor.getColumnIndex(ChatPlaceTable.OBJECT_ID_COLUMN));
			send.setEnabled(true);
			message.setEnabled(true);

            String chatName = cursor.getString(cursor.getColumnIndex(ChatPlaceTable.NAME_COLUMN));
            getActivity().getActionBar().setTitle(chatName);
		}
	}

	private void resetChatKey() {
		send.setEnabled(false);
		message.setEnabled(false);
		chatKey = null;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOAD_CHAT_KEY_ID:
			Uri uri = Uri.withAppendedPath(QuezzleProviderContract.CHAT_PLACES_URI, Long.toString(getChatId()));
			return new CursorLoader(getActivity(), uri, new String[] { ChatPlaceTable.OBJECT_ID_COLUMN, ChatPlaceTable.NAME_COLUMN },
                                    null, null, null);
		case LOAD_MESSAGES_ID:
			return new CursorLoader(getActivity(), QuezzleProviderContract.getMessagesUri(getChatId()), new String[] { MessageTable._ID,
					MessageTable.UPDATED_AT_COLUMN, MessageTable.MESSAGE_COLUMN, MessageTable.AUTHOR_COLUMN }, null, null,
					CHAT_MESSAGES_ORDER);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOAD_CHAT_KEY_ID:
            setChatData(cursor);
			break;
		case LOAD_MESSAGES_ID:
			if (cursor.getCount() == 0 && !wasReloaded) {
                wasReloaded = true;

				// try to load chat messages
				NetworkService.reloadChat(getActivity(), getChatId());
			} else {
                wasReloaded = true;
            }
			messageListAdapter.swapCursor(cursor);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case LOAD_CHAT_KEY_ID:
			resetChatKey();
			break;
		case LOAD_MESSAGES_ID:
			messageListAdapter.swapCursor(null);
			break;
		}
	}

	private class NewMessageEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getChatId() == intent.getLongExtra(NetworkService.CHAT_ID_EXTRA, -1)) {
				setResultCode(Activity.RESULT_OK);
				abortBroadcast();
			}
		}
	}
}
