package com.skylion.quezzle.datamodel;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 27.03.14
 * Time: 22:40
 * To change this template use File | Settings | File Templates.
 */
@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String MESSAGE_FIELD = "message";
    public static final String AUTHOR_FIELD = "author";
    public static final String CHAT_ID_FIELD = "chatId";

    public Message() {
        super();
    }

    public String getMessage() {
        return getString(MESSAGE_FIELD);
    }

    public void setMessage(String message) {
        put(MESSAGE_FIELD, message);
    }

    public ParseUser getAuthor() {
        return getParseUser(AUTHOR_FIELD);
    }

    public void setAuthor(ParseUser author) {
        put(AUTHOR_FIELD, author);
    }

    public String getChatId() {
        return getString(CHAT_ID_FIELD);
    }

    public void setChatId(String chatId) {
        put(CHAT_ID_FIELD, chatId);
    }
}
