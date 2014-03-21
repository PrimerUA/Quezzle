package com.skylion.quezzle;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseUser;
import com.parse.PushService;
import com.skylion.quezzle.network.VolleyHelper;
import com.skylion.quezzle.network.request.ParseBaseRequest;
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
    private VolleyHelper volleyHelper;

    public static QuezzleApplication getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;
        volleyHelper = new VolleyHelper(this);

        Parse.initialize(this, "RVCqyTO6a3jDJPh0GeKRzbbpdXZWGWtm13m0MN67", "BBD41jgbfdaTUdvtTxutynfB07C2HJKRquCX8MR3");
        ParseBaseRequest.setKeys("RVCqyTO6a3jDJPh0GeKRzbbpdXZWGWtm13m0MN67", "KU29aODJKiB1zjApeoeSiHTnwl0mFFcnIDRK7KJ7");
        PushService.setDefaultPushCallback(this, ChatsListActivity.class);
        ParseUser.enableAutomaticUser();
    }

    public VolleyHelper getVolleyHelper() {
        return volleyHelper;
    }
}
