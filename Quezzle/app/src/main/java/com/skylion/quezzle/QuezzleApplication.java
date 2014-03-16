package com.skylion.quezzle;

import android.app.Application;

import com.parse.Parse;
import com.parse.PushService;

/**
 * Created by Michael Skylion on 16.03.14.
 */
public class QuezzleApplication extends Application{

    public void onCreate() {
        Parse.initialize(this, "RVCqyTO6a3jDJPh0GeKRzbbpdXZWGWtm13m0MN67", "BBD41jgbfdaTUdvtTxutynfB07C2HJKRquCX8MR3");
        PushService.setDefaultPushCallback(this, ChatsListActivity.class);
    }
}
