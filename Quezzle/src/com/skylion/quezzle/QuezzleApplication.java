package com.skylion.quezzle;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.PushService;
import com.skylion.quezzle.datamodel.ChatPlace;
import com.skylion.quezzle.ui.activity.ChatsListActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 17.03.14
 * Time: 23:47
 * To change this template use File | Settings | File Templates.
 */
public class QuezzleApplication extends Application {
    private static QuezzleApplication application;

    public static QuezzleApplication getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;

        initParse();
    }

    private void initParse() {
        //register classes
        ParseObject.registerSubclass(ChatPlace.class);

        Parse.initialize(this, "RVCqyTO6a3jDJPh0GeKRzbbpdXZWGWtm13m0MN67", "BBD41jgbfdaTUdvtTxutynfB07C2HJKRquCX8MR3");
        PushService.setDefaultPushCallback(this, ChatsListActivity.class);
    }
}
