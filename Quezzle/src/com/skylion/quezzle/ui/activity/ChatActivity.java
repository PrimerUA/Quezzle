package com.skylion.quezzle.ui.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.skylion.quezzle.R;
import com.skylion.quezzle.ui.fragment.ChatFragment;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 24.03.14
 * Time: 23:51
 * To change this template use File | Settings | File Templates.
 */
public class ChatActivity extends QuezzleBaseActivity {
	private static final String CHAT_KEY_EXTRA = "com.skylion.quezzle.ui.activity.ChatActivity.CHAT_KEY";

    public static void start(Context context, String chatKey) {
        context.startActivity(getIntent(context, chatKey));
    }

    public static Intent getIntent(Context context, String chatKey) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(CHAT_KEY_EXTRA, chatKey);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        //get chat key from extra
        String chatKey = getIntent().getStringExtra(CHAT_KEY_EXTRA);

        //set fragment
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            try {
            	transaction.add(R.id.fragment_container, ChatFragment.newInstance(chatKey));
            } finally {
                transaction.commit();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //get chat key from extra
        String chatKey = intent.getStringExtra(CHAT_KEY_EXTRA);

        //show new fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        try {
            transaction.replace(R.id.fragment_container, ChatFragment.newInstance(chatKey));
        } finally {
            transaction.commit();
        }
    }
}
