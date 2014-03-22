package com.skylion.quezzle.network.parse.request;

import android.text.TextUtils;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.skylion.quezzle.network.Urls;
import com.skylion.quezzle.network.parse.datamodel.ParseBaseObject;

import java.io.UnsupportedEncodingException;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 22.03.14
 * Time: 20:24
 * To change this template use File | Settings | File Templates.
 */
public class CreateObjectRequest<T extends ParseBaseObject> extends ParseBaseRequest<T> {
    protected static final Gson gson = new Gson();

    private T src;

    public CreateObjectRequest(String className, T object, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.POST, Urls.CREATE_OBJECT_URL + className , gson.toJson(object), listener, errorListener);

        src = object;
    }

    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            //get string response
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            //parse response
            CreatedObjectData responseData = gson.fromJson(json, CreatedObjectData.class);
            if (responseData != null && !TextUtils.isEmpty(responseData.objectId)) {
                //set fields
                if (src != null) {
                    src.objectId = responseData.objectId;
                    src.setCreatedAt(responseData.createdAt);
                }

                return Response.success(src, HttpHeaderParser.parseCacheHeaders(response));
            } else {
                return Response.error(new VolleyError("Error creating object"));
            }
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    private static class CreatedObjectData {
        public String createdAt;
        public String objectId;
    }
}
