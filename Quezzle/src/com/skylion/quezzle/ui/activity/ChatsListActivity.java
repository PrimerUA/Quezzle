package com.skylion.quezzle.ui.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.parse.ParseUser;
import com.skylion.quezzle.QuezzleApplication;
import com.skylion.quezzle.R;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datamodel.ChatPlace;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.network.parse.response.QueryResponse;
import com.skylion.quezzle.network.request.ChatPlacesRequest;
import com.skylion.quezzle.ui.adapter.ChatListAdapter;
import com.skylion.quezzle.ui.auth.UserLoginActivity;

public class ChatsListActivity extends Activity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	private static final int LOAD_CHATS_ID = 0;
	private static final String[] PROJECTION = new String[] { ChatPlaceTable._ID, ChatPlaceTable.NAME_COLUMN,
			ChatPlaceTable.DESCRIPTION_COLUMN };

	private ListView chatsList;
	private ChatListAdapter adapter;
	private Button createButton;

	private ArrayList<String> chatNameList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chats);
		chatNameList = new ArrayList<String>();
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));
		if (currentUser != null) {
			// do stuff with the user
		} else {
			startActivity(new Intent(this, UserLoginActivity.class));
		}

		Log.d("KVEST_TAG", "!=" + getIntent().getAction());
		if (getIntent().getExtras() != null) {
			String data = getIntent().getExtras().getString("com.parse.Data");
			for (String key : getIntent().getExtras().keySet()) {

				Log.d("KVEST_TAG", key + "=" + getIntent().getExtras().getString(key));
			}
		}
		// Log.d("KVEST_TAG", "e=" + (getIntent().getExtras() != null ?
		// getIntent().getExtras().toString() : "null"));

		adapter = new ChatListAdapter(this, ChatListAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		chatsList = (ListView) findViewById(R.id.chatsList);
		chatsList.setAdapter(adapter);
		chatsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// show chat
				ChatActivity.start(ChatsListActivity.this, id, chatNameList.get(position));
			}
		});

		createButton = (Button) findViewById(R.id.createButton);
		createButton.setOnClickListener(this);

		getLoaderManager().initLoader(LOAD_CHATS_ID, null, this);
		loadChatList();
	}

	private void loadChatList() {
		// clear db
		getContentResolver().delete(QuezzleProviderContract.CHAT_PLACES_URI, null, null);

		// query new chats
		ChatPlacesRequest request = new ChatPlacesRequest(new Response.Listener<QueryResponse<ChatPlace>>() {
			@Override
			public void onResponse(QueryResponse<ChatPlace> response) {
				if (response.results != null) {
					ContentValues[] values = new ContentValues[response.results.size()];
					for (int i = 0; i < values.length; ++i) {
						ChatPlace chatPlace = response.results.get(i);
						values[i] = new ContentValues(5);
						values[i].put(ChatPlaceTable.OBJECT_ID_COLUMN, chatPlace.objectId);
						values[i].put(ChatPlaceTable.CREATED_AT_COLUMN, chatPlace.getCreatedAt());
						values[i].put(ChatPlaceTable.UPDATED_AT_COLUMN, chatPlace.getUpdatedAt());
						values[i].put(ChatPlaceTable.NAME_COLUMN, chatPlace.name);
						values[i].put(ChatPlaceTable.DESCRIPTION_COLUMN, chatPlace.description);

						chatNameList.add(chatPlace.name);
					}

					getContentResolver().bulkInsert(QuezzleProviderContract.CHAT_PLACES_URI, values);
				} else {
					Toast.makeText(ChatsListActivity.this, "Error loading chats", Toast.LENGTH_SHORT).show();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(ChatsListActivity.this, "Error loading chats: " + error.getMessage(), Toast.LENGTH_SHORT)
						.show();
			}
		});
		request.setTag(this);
		QuezzleApplication.getApplication().getVolleyHelper().addRequest(request);
		// final ParseGeoPoint userLocation = (ParseGeoPoint)
		// ParseUser.getCurrentUser().get("location");
		// ParseQueryAdapter<ParseObject> adapter = new
		// ParseQueryAdapter<ParseObject>(this, new
		// ParseQueryAdapter.QueryFactory<ParseObject>() {
		// public ParseQuery<ParseObject> create() {
		// ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatPlace");
		// query.whereNear("location", userLocation);
		// query.setLimit(10);
		// query.findInBackground(new FindCallback<ParseObject>() {
		// @Override
		// public void done(List<ParseObject> parseObjects, ParseException e) {
		//
		// }
		// });
		// return query;
		// }
		// });
		// adapter.setTextKey("name"); //запихнуть в
		// кастомный адаптер и вызвать в done
		// adapter.setImageKey("photo");
		// chatsList.setAdapter(adapter);
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
			loadChatList();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		startActivity(new Intent(this, NewChatActivity.class));
	}

	// @Override
	// public void onItemClick(AdapterView<?> adapterView, View view, int i,
	// long l) {
	// String chatName = ((ParseObject)
	// chatsList.getAdapter().getItem(i)).get("name").toString();
	// startActivity(new Intent(this,
	// ChatDetailsActivity.class).putExtra("chatName", chatName));
	// }

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
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}
