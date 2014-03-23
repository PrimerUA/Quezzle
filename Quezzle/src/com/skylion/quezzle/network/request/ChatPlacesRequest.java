package com.skylion.quezzle.network.request;

import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;
import com.skylion.quezzle.datamodel.ChatPlace;
import com.skylion.quezzle.network.parse.request.QueryRequest;
import com.skylion.quezzle.network.parse.response.QueryResponse;

import java.lang.reflect.Type;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 23.03.14
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
public class ChatPlacesRequest extends QueryRequest<ChatPlace> {
    public ChatPlacesRequest(Response.Listener<QueryResponse<ChatPlace>> listener, Response.ErrorListener errorListener) {
        super(ChatPlace.class.getSimpleName(), new TypeToken<QueryResponse<ChatPlace>>(){}.getType(), listener, errorListener);
    }
}
