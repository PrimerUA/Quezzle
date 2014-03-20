package com.skylion.quezzle.network.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 20.03.14
 * Time: 23:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class ParseBaseRequest<T> extends JsonRequest<T> {
    public ParseBaseRequest(String url, String requestBody, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(url, requestBody, listener, errorListener);
    }

    public ParseBaseRequest(int method, String url, String requestBody, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<String, String>(2);
        headers.put("X-Parse-Application-Id", "RVCqyTO6a3jDJPh0GeKRzbbpdXZWGWtm13m0MN67");
        headers.put("X-Parse-REST-API-Key", "KU29aODJKiB1zjApeoeSiHTnwl0mFFcnIDRK7KJ7");
        return headers;
    }
}
