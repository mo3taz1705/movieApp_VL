package com.example.moutaz.movieapp;

/**
 * Created by Moutaz on 7/27/2016.
 */
public interface VolleyResponseListener {

    void onError (String message, String uniquePath);

    void onSuccess (Object response, String uniquePath);

}
