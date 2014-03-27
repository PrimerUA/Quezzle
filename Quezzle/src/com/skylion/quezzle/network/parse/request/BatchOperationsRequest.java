package com.skylion.quezzle.network.parse.request;

import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.skylion.quezzle.network.Urls;
import com.skylion.quezzle.network.parse.datamodel.Operation;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 27.03.14
 * Time: 22:50
 * To change this template use File | Settings | File Templates.
 */
public class BatchOperationsRequest extends ParseBaseRequest<Object> {
    protected static final Gson gson = new Gson();

    private RequestBody requestBody;

    public BatchOperationsRequest(Response.Listener<Object> listener, Response.ErrorListener errorListener) {
        super(Method.POST, Urls.BATCH_OPERATIONS_URL , null, listener, errorListener);

        requestBody = new RequestBody();
    }

    public BatchOperationsRequest addOperation(Operation operation) {
        requestBody.requests.add(operation);
        return this;
    }

    protected Response<Object> parseNetworkResponse(NetworkResponse response) {
        try {
            //get string response
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Log.d("KVEST_TAG", "json=" + json);

            return Response.error(new ParseError());
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public byte[] getBody() {
        String json = gson.toJson(requestBody);
        try {
            return json.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", json, PROTOCOL_CHARSET);
            return null;
        }
    }

    private static class RequestBody {
        private List<Operation> requests;

        public RequestBody() {
            requests = new ArrayList<Operation>();
        }
    }
}
