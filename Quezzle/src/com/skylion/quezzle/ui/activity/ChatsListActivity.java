package com.skylion.quezzle.ui.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;
import com.parse.ParseUser;
import com.skylion.quezzle.R;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.notification.ReloadChatListNotification;
import com.skylion.quezzle.service.NetworkService;
import com.skylion.quezzle.ui.adapter.ChatListAdapter;
import com.skylion.quezzle.utility.Constants;

public class ChatsListActivity extends Activity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	private static final int LOAD_CHATS_ID = 0;
	private static final String[] PROJECTION = new String[] { ChatPlaceTable._ID, ChatPlaceTable.NAME_COLUMN,
			ChatPlaceTable.DESCRIPTION_COLUMN };

    private boolean firstLoad = true;
	private ListView chatsList;
	private ChatListAdapter adapter;
	private Button createButton;

    private ReloadChatListNotificationReceiver receiver = new ReloadChatListNotificationReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chats);
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));
		if (currentUser != null) {
			// do stuff with the user
		} else {
			startActivity(new Intent(this, UserLoginActivity.class));
		}

		Log.d(Constants.LOG_TAG, "!=" + getIntent().getAction());
		if (getIntent().getExtras() != null) {
			String data = getIntent().getExtras().getString("com.parse.Data");
			for (String key : getIntent().getExtras().keySet()) {
				Log.d(Constants.LOG_TAG, key + "=" + getIntent().getExtras().getString(key));
			}
		}

		adapter = new ChatListAdapter(this, ChatListAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		chatsList = (ListView) findViewById(R.id.chatsList);
		chatsList.setAdapter(adapter);
		chatsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// show chat
				ChatActivity.start(ChatsListActivity.this, id);
			}
		});

		createButton = (Button) findViewById(R.id.createButton);
		createButton.setOnClickListener(this);

		getLoaderManager().initLoader(LOAD_CHATS_ID, null, this);
	}

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(ReloadChatListNotification.ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
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
		if (id == R.id.action_refresh) {
			reloadChatList();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		startActivity(new Intent(this, NewChatActivity.class));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOAD_CHATS_ID:
			return new CursorLoader(this, QuezzleProviderContract.CHAT_PLACES_URI, PROJECTION, null, null, null);
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
                Toast.makeText(ChatsListActivity.this, getString(R.string.error_reloading_chat_list,
                        ReloadChatListNotification.getErrorMessage(intent)),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
