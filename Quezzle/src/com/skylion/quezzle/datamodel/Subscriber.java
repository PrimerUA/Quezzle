package com.skylion.quezzle.datamodel;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 28.04.14
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
@ParseClassName("Subscriber")
public class Subscriber extends ParseObject {
    public static final String SUBSCRIBER_ID_FIELD = "subscriber_id";
    public static final String CHAT_ID_FIELD = "chat_id";

    public Subscriber() {
        super();
    }

    public String getSubscriberId() {
        return getString(SUBSCRIBER_ID_FIELD);
    }

    public void setSubscriberId(String subscriberId) {
        put(SUBSCRIBER_ID_FIELD, subscriberId);
    }

    public String getChatId() {
        return getString(CHAT_ID_FIELD);
    }

    public void setChatId(String chatId) {
        put(CHAT_ID_FIELD, chatId);
    }
}
