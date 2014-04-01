package com.skylion.quezzle.ui.activity;

import android.app.Activity;
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
public class ChatActivity extends Activity {
    private static final String CHAT_ID_EXTRA = "com.skylion.quezzle.ui.activity.ChatActivity.CHAT_ID";
    private static final String CHAT_NAME_EXTRA = "com.skylion.quezzle.ui.activity.ChatActivity.CHAT_NAME";

    public static void start(Context context, long chatId, String chatName) {
        context.startActivity(getIntent(context, chatId, chatName));
    }

    public static Intent getIntent(Context context, long chatId, String chatName) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(CHAT_ID_EXTRA, chatId);
        intent.putExtra(CHAT_NAME_EXTRA, chatName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        //get chat id from extra
        long chatId = getIntent().getLongExtra(CHAT_ID_EXTRA, -1);
        String chatName = getIntent().getStringExtra(CHAT_NAME_EXTRA);

        //set fragment
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            try {
            	transaction.add(R.id.fragment_container, ChatFragment.newInstance(chatId, chatName));
            } finally {
                transaction.commit();
            }
        }
    }
}
