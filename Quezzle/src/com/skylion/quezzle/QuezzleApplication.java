package com.skylion.quezzle;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.PushService;
import com.skylion.quezzle.datamodel.ChatMessage;
import com.skylion.quezzle.datamodel.ChatPlace;
import com.skylion.quezzle.ui.activity.ChatsListActivity;
import com.skylion.quezzle.utility.Constants;

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

        //init UIL
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(config);

        initParse();
    }

    private void initParse() {
        //register classes
        ParseObject.registerSubclass(ChatPlace.class);
        ParseObject.registerSubclass(ChatMessage.class);

        Parse.initialize(this, Constants.PARSE_APP_ID, Constants.PARSE_CLIENT_KEY);
        PushService.setDefaultPushCallback(this, ChatsListActivity.class);
    }
}
