package com.skylion.quezzle.ui.activity;

import android.app.ProgressDialog;
import android.content.*;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.skylion.quezzle.R;
import com.skylion.quezzle.notification.CreateChatNotification;
import com.skylion.quezzle.service.NetworkService;

/**
 * Created with IntelliJ IDEA. User: Kvest Date: 19.03.14 Time: 21:28 To change
 * this template use File | Settings | File Templates.
 */
public class NewChatActivity extends QuezzleBaseActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, NewChatActivity.class);
        context.startActivity(intent);
    }

	private EditText nameEdit;
	private EditText descEdit;
	private Button createButton;
	private ProgressDialog progressDialog;

	private CreateChatNotificationReceiver receiver = new CreateChatNotificationReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_chat);

		getActionBar().setTitle(R.string.title_activity_new_chat);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));

		nameEdit = (EditText) findViewById(R.id.nameEdit);
		descEdit = (EditText) findViewById(R.id.descriptionEdit);

		createButton = (Button) findViewById(R.id.createLocalChatButton);
		createButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(CreateChatNotification.ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

	private void sendMessage() {
		progressDialog = ProgressDialog.show(this, getString(R.string.connecting), getString(R.string.loading_new_chat));

		if (TextUtils.isEmpty(nameEdit.getText())) {
			Toast.makeText(NewChatActivity.this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
			return;
		}

		NetworkService.createChat(this, nameEdit.getText().toString(), descEdit.getText().toString());
	}

	private class CreateChatNotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			progressDialog.dismiss();
			if (CreateChatNotification.isSuccessful(intent)) {
				Toast.makeText(NewChatActivity.this, getString(R.string.new_chat_created), Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Toast.makeText(NewChatActivity.this,
						getString(R.string.error_creating_chat, CreateChatNotification.getErrorMessage(intent)), Toast.LENGTH_LONG).show();
			}
		}
	}
}
