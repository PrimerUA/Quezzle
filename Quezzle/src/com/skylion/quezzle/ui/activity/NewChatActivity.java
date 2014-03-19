package com.skylion.quezzle.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.skylion.quezzle.R;


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
        ParseGeoPoint point = new ParseGeoPoint(40.0, -30.0); //get coords!
        ParseObject chatPlace = new ParseObject("ChatPlaces");
        chatPlace.put("name", nameEdit.getText().toString());
        chatPlace.put("description", descEdit.getText().toString());
        //chatPlace.put("location", point);
        chatPlace.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                finish();
            }
        });
    }
}
