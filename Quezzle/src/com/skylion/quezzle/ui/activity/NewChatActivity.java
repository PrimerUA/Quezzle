package com.skylion.quezzle.ui.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.skylion.quezzle.QuezzleApplication;
import com.skylion.quezzle.R;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datamodel.ChatPlace;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.network.parse.request.CreateObjectRequest;


/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 19.03.14
 * Time: 21:28
 * To change this template use File | Settings | File Templates.
 */
public class NewChatActivity extends Activity  implements View.OnClickListener  {
    private EditText nameEdit;
    private EditText descEdit;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        nameEdit = (EditText) findViewById(R.id.nameEdit);
        descEdit = (EditText) findViewById(R.id.descriptionEdit);

        createButton = (Button) findViewById(R.id.createLocalChatButton);
        createButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (TextUtils.isEmpty(nameEdit.getText())) {
            Toast.makeText(NewChatActivity.this, "Chat name can not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatPlace src = new ChatPlace();
        src.name = nameEdit.getText().toString();
        src.description = descEdit.getText().toString();
        CreateObjectRequest<ChatPlace> request = new CreateObjectRequest<ChatPlace>(ChatPlace.class.getSimpleName(), src,
                new Response.Listener<ChatPlace>() {
                    @Override
                    public void onResponse(ChatPlace response) {
                        //save object to db
                        ContentValues values = new ContentValues(5);
                        values.put(ChatPlaceTable.OBJECT_ID_COLUMN, response.objectId);
                        values.put(ChatPlaceTable.CREATED_AT_COLUMN, response.getCreatedAt());
                        values.put(ChatPlaceTable.UPDATED_AT_COLUMN, response.getUpdatedAt());
                        values.put(ChatPlaceTable.NAME_COLUMN, response.name);
                        values.put(ChatPlaceTable.DESCRIPTION_COLUMN, response.description);
                        getContentResolver().insert(QuezzleProviderContract.CHAT_PLACES_URI, values);

                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(NewChatActivity.this, "Error creating new chat", Toast.LENGTH_SHORT).show();
                    }
                });
        request.setTag(this);
        QuezzleApplication.getApplication().getVolleyHelper().addRequest(request);
//        ParseGeoPoint point = new ParseGeoPoint(40.0, -30.0); //get coords!
//        ParseObject chatPlace = new ParseObject("ChatPlace");
//        chatPlace.put("name", nameEdit.getText().toString());
//        chatPlace.put("description", descEdit.getText().toString());
//        //chatPlace.put("location", point);
//        chatPlace.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                finish();
//            }
//        });
    }
}
