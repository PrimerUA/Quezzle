package com.skylion.quezzle.network.parse.request;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.skylion.quezzle.datamodel.ChatPlaces;
import com.skylion.quezzle.network.Urls;
import com.skylion.quezzle.network.parse.response.QueryResponse;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * Created with IntelliJ IDEA.
 * User: roman
 * Date: 3/21/14
 * Time: 6:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryRequest<T> extends ParseBaseRequest<QueryResponse<T>> {
    protected static final Gson gson = new Gson();

    public QueryRequest(String className, Response.Listener<QueryResponse<T>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, Urls.QUERIES_URL + className , null, listener, errorListener);
    }

    @Override
    protected Response<QueryResponse<T>> parseNetworkResponse(NetworkResponse response) {
        try {
            //get string response
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            //parse response
            Type responseType = new TypeToken<QueryResponse<T>>(){}.getType();
            QueryResponse<T> result = gson.fromJson(json, responseType);

            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
