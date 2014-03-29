package com.skylion.quezzle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import java.util.List;


public class ChatsListActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView chatsList;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        chatsList = (ListView) findViewById(R.id.chatsList);
        createButton = (Button) findViewById(R.id.createButton);

        chatsList = (ListView) findViewById(R.id.chatsList);
        chatsList.setOnItemClickListener(this);

        createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(this);

        loadChatList();
    }

    private void loadChatList() {
        final ParseGeoPoint userLocation = (ParseGeoPoint) ParseUser.getCurrentUser().get("location");
        ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(this, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatPlaces");
                query.whereNear("location", userLocation);
                query.setLimit(10);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {

                    }
                });
                return query;
            }
        });
        adapter.setTextKey("name"); //запихнуть в кастомный адаптер и вызвать в done
        adapter.setImageKey("photo");
        chatsList.setAdapter(adapter);
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String chatName = ((ParseObject) chatsList.getAdapter().getItem(i)).get("name").toString();
        startActivity(new Intent(this, ChatDetailsActivity.class).putExtra("chatName", chatName));
    }
}
