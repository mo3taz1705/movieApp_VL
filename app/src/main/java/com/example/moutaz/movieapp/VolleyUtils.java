package com.example.moutaz.movieapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;


/**
 * Created by Moutaz on 7/27/2016.
 */
public class VolleyUtils {

    public static StringRequest makeStringRequest(Context context, String url, final VolleyResponseListener listener, final Class<?> responseType, final String uniquePath) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // parse object according to the desired responseType
                        Gson gson = new Gson();
                        Object myResponse = gson.fromJson(response, responseType);

                        // send the response to the calling class
                        listener.onSuccess(myResponse, uniquePath);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error.toString(), uniquePath);
            }
        });

        // access the request queue of the singleton class
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);

        // return reference on the stringRequest
        return stringRequest;
    }
}
