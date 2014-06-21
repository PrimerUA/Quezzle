package com.skylion.quezzle.ui.activity;

import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.Toast;
import com.parse.ParseUser;
import com.skylion.quezzle.R;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datastorage.SettingsSPStorage;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.notification.ReloadChatListNotification;
import com.skylion.quezzle.service.NetworkService;
import com.skylion.quezzle.ui.adapter.ChatListAdapter;
import com.skylion.quezzle.utility.Constants;

public class ChatsListActivity extends QuezzleBaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private static final int LOAD_CHATS_ID = 0;
	private static final String CHARTS_ORDER = ChatPlaceTable.CREATED_AT_COLUMN + " DESC";

	private boolean firstLoad = true;
	private ListView chatsList;
	private ChatListAdapter adapter;
	private SwipeRefreshLayout refresh;
	private ImageView reloadHint;
	private TextView textHint1;
	private TextView textHint2;
	private TextView textHint3;

	private ReloadChatListNotificationReceiver receiver = new ReloadChatListNotificationReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chats);

		ParseUser currentUser = ParseUser.getCurrentUser();
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.name_top));
		if (currentUser != null) {
			// do stuff with the user
		} else {
			UserLoginActivity.start(this);
		}

		Log.d(Constants.LOG_TAG, "!=" + getIntent().getAction());
		if (getIntent().getExtras() != null) {
			String data = getIntent().getExtras().getString("com.parse.Data");
			for (String key : getIntent().getExtras().keySet()) {
				Log.d(Constants.LOG_TAG, key + "=" + getIntent().getExtras().getString(key));
			}
		}

		adapter = new ChatListAdapter(this);
		chatsList = (ListView) findViewById(R.id.chatsList);
		chatsList.setAdapter(adapter);
		chatsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// get chat key from adapter
				String chatKey = adapter.getChatKey(view, position, id);

				// show chat
				ChatActivity.start(ChatsListActivity.this, chatKey);
			}
		});
		refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
		refresh.setColorScheme(R.color.main_bg, android.R.color.black, R.color.main_bg, android.R.color.white);
		refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				reloadChatList();
			}
		});
		reloadHint = (ImageView) findViewById(R.id.reload_hint);
		textHint1 = (TextView) findViewById(R.id.r1);
		textHint2 = (TextView) findViewById(R.id.r2);
		textHint3 = (TextView) findViewById(R.id.r3);
		textHint1.setVisibility(SettingsSPStorage.isReloadHintShown(this) ? View.INVISIBLE : View.VISIBLE);
		textHint2.setVisibility(SettingsSPStorage.isReloadHintShown(this) ? View.INVISIBLE : View.VISIBLE);
		textHint3.setVisibility(SettingsSPStorage.isReloadHintShown(this) ? View.INVISIBLE : View.VISIBLE);
		reloadHint.setVisibility(SettingsSPStorage.isReloadHintShown(this) ? View.INVISIBLE : View.VISIBLE);
		reloadHint.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View hintView) {
				// hide hint
				reloadHint.setVisibility(View.INVISIBLE);
				textHint1.setVisibility(View.INVISIBLE);
				textHint2.setVisibility(View.INVISIBLE);
				textHint3.setVisibility(View.INVISIBLE);

				// save that hint was shown
				SettingsSPStorage.setReloadHintShown(ChatsListActivity.this, true);
			}
		});
		getLoaderManager().initLoader(LOAD_CHATS_ID, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(ReloadChatListNotification.ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

	private void reloadChatList() {
		NetworkService.reloadChatList(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_profile) {
			UserProfileActivity.start(ChatsListActivity.this);
		} else if (id == R.id.action_create) {
			NewChatActivity.start(this);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOAD_CHATS_ID:
			return new CursorLoader(this, QuezzleProviderContract.CHAT_PLACES_URI, ChatListAdapter.PROJECTION, null, null, CHARTS_ORDER);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (firstLoad && cursor.getCount() == 0) {
			reloadChatList();
		}
		firstLoad = false;

		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	private class ReloadChatListNotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!ReloadChatListNotification.isSuccessful(intent)) {
				Toast.makeText(ChatsListActivity.this,
						getString(R.string.error_reloading_chat_list, ReloadChatListNotification.getErrorMessage(intent)),
						Toast.LENGTH_LONG).show();
			}

			// hide progress
			refresh.setRefreshing(false);
		}
	}
}
