package com.skylion.quezzle.network.parse.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;

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
    private static final String APPLICATION_ID_HEADER = "X-Parse-Application-Id";
    private static final String REST_API_KEY_HEADER = "X-Parse-REST-API-Key";
    private static final Map<String, String> headers = new HashMap<String, String>(2);

    public static void setKeys(String applicationId, String restApiKey) {
        headers.clear();
        headers.put(APPLICATION_ID_HEADER, applicationId);
        headers.put(REST_API_KEY_HEADER, restApiKey);
    }

    public ParseBaseRequest(String url, String requestBody, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(url, requestBody, listener, errorListener);
    }

    public ParseBaseRequest(int method, String url, String requestBody, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }
}
