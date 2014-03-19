package com.skylion.quezzle.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.parse.*;
import com.skylion.quezzle.R;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 19.03.14
 * Time: 21:31
 * To change this template use File | Settings | File Templates.
 */
public class ChatDetailsActivity extends Activity implements View.OnClickListener {
    private ListView chatsList;
    private EditText messageEdit;
    private ImageButton postButton;

    private String chatName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);

        chatsList = (ListView) findViewById(R.id.chattingList);
        messageEdit = (EditText) findViewById(R.id.messageEdit);

        postButton = (ImageButton) findViewById(R.id.postButton);
        postButton.setOnClickListener(this);

        chatName = getIntent().getStringExtra("chatName");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatMessage");
        query.whereEqualTo("chatName", chatName);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> messageList, ParseException e) {
                if (e == null) {
                    loadChatList();
                    Log.d("score", "Retrieved " + messageList.size() + " messages");
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    private void loadChatList() {
        ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(this, "ChatMessage");
        adapter.setTextKey("message");
        adapter.setImageKey("photo");
        chatsList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        ParseObject chatMessage = new ParseObject("ChatMessage");
        chatMessage.put("message", messageEdit.getText().toString());
        chatMessage.put("chatName", chatName);
        chatMessage.put("userName", "Kvest");
        chatMessage.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                loadChatList();
            }
        });
        messageEdit.getText().clear();
    }
}
