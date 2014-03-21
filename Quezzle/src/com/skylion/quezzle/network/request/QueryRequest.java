package com.skylion.quezzle.network.request;

import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.skylion.quezzle.network.Urls;
import com.skylion.quezzle.network.response.QueryResponse;

import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: roman
 * Date: 3/21/14
 * Time: 6:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryRequest<T> extends ParseBaseRequest<QueryResponse<T>> {

    public QueryRequest(String className, Response.Listener<QueryResponse<T>> listener, Response.ErrorListener errorListener) {
        super(Method.GET, Urls.QUERIES_URL + className , null, listener, errorListener);
    }

    @Override
    protected Response<QueryResponse<T>> parseNetworkResponse(NetworkResponse response) {
        try {
            //get string response
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.d("KVEST_TAG", "json=" + json);

            return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
