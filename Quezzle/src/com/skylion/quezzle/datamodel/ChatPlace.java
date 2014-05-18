package com.skylion.quezzle.datamodel;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 20.03.14
 * Time: 23:12
 * To change this template use File | Settings | File Templates.
 */

@ParseClassName("ChatPlace")
public class ChatPlace extends ParseObject {
    public static final String NAME_FIELD = "name";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String CHAT_TYPE_FIELD = "chat_type";
    public static final String LOCATION_FIELD = "location";
    public static final String RADIUS_FIELD = "radius";

    public ChatPlace() {
        super();
    }

    public String getName() {
        return getString(NAME_FIELD);
    }

    public void setName(String name) {
        put(NAME_FIELD, name);
    }

    public String getDescription() {
        return getString(DESCRIPTION_FIELD);
    }

    public void setDescription(String description) {
        put(DESCRIPTION_FIELD, description);
    }

    public void setChatType(int chatType) {
        put(CHAT_TYPE_FIELD, chatType);
    }

    public int getChatType() {
        return getInt(CHAT_TYPE_FIELD);
    }

    public void setLocation(ParseGeoPoint location) {
        put(LOCATION_FIELD, location);
    }

    public ParseGeoPoint getLocation() {
        return (ParseGeoPoint)get(LOCATION_FIELD);
    }

    public void setRadius(int radius) {
        put(RADIUS_FIELD, radius);
    }

    public int getRadius() {
        return getInt(RADIUS_FIELD);
    }
}
