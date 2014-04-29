package com.skylion.quezzle.datamodel;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 28.04.14
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
@ParseClassName("Subscriber")
public class Subscriber extends ParseObject {
    public static final String SUBSCRIBER_FIELD = "subscriber";
    public static final String CHAT_FIELD = "chat";

    public Subscriber() {
        super();
    }

    public ParseUser getSubscriber() {
        return getParseUser(SUBSCRIBER_FIELD);
    }

    public void setSubscriber(ParseUser author) {
        put(SUBSCRIBER_FIELD, author);
    }

    public ChatPlace getChat() {
        return (ChatPlace)get(CHAT_FIELD);
    }

    public void setChat(ChatPlace chat) {
        put(CHAT_FIELD, chat);
    }
}
