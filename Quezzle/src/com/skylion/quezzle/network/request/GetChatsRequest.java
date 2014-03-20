package com.skylion.quezzle.network.request;

import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.skylion.quezzle.network.response.GetChatsResponse;

import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 20.03.14
 * Time: 23:22
 * To change this template use File | Settings | File Templates.
 */
public class GetChatsRequest extends ParseBaseRequest<GetChatsResponse> {
    public GetChatsRequest(Response.Listener<GetChatsResponse> listener, Response.ErrorListener errorListener) {
        super(Method.GET, "https://api.parse.com/1/classes/ChatPlaces", null, listener, errorListener);
    };

    @Override
    protected Response<GetChatsResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            //get string response
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.d("KVEST_TAG", "json=" + json);

            return Response.success(new GetChatsResponse(), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
