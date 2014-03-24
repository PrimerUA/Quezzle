package com.skylion.quezzle.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.skylion.quezzle.R;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 24.03.14
 * Time: 23:10
 * To change this template use File | Settings | File Templates.
 */
public class ChatFragment extends Fragment {
    private long chatId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_fragment, container, false);

        //TODO

        return rootView;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
