package com.example.moutaz.movieapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import butterknife.BindView;

public class SearchActivity extends ActionBarActivity implements VolleyResponseListener {

    //    @BindView(R.id.etSearchField) EditText etMovieName;
//    @BindView(R.id.ivSearch) ImageView ivSearch;
//    @BindView(R.id.listView2) ListView lvResults;
//    @BindView(R.id.pbLoading) ProgressBar mProgressBar;

    private ProgressBar mProgressBar;
    private ListView lvResults;
    private ImageView ivSearch;
    private EditText etMovieName;

    private ArrayList<GridItem> listViewArrayList;
    private ListViewSearchCustomAdapter listViewAdapter;
    private Boolean noConnection;
    private AlertDialog alertDialog;
    private AlertDialog alertDialog2;

    private StringRequest myStringRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initialize();

    }

    private void initialize() {
        etMovieName = (EditText) findViewById(R.id.etSearchField);
        ivSearch = (ImageView) findViewById(R.id.ivSearch);
        lvResults = (ListView) findViewById(R.id.listView2);
        mProgressBar = (ProgressBar) findViewById(R.id.pbLoading);

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etMovieName.getText().toString();
                if (name.length() > 0) {
                    searchForResults();
                } else {
                    showDialogBox();
                }
            }
        });

        //setting adapter
        listViewArrayList = new ArrayList<>();
        listViewAdapter = new ListViewSearchCustomAdapter(this, R.layout.list_item_layout, listViewArrayList);
        lvResults.setAdapter(listViewAdapter);

        //add list view on item click listener
        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                GridItem gridItem = (GridItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), MovieActivity.class);

                intent.putExtra("TITLE", gridItem.getOrigTitle());
                intent.putExtra("OVERVIEW", gridItem.getOverview());
                intent.putExtra("RATE", gridItem.getVoteAvg());
                intent.putExtra("YEAR", gridItem.getRelDate());
                intent.putExtra("IMAGE", gridItem.getImage());
                intent.putExtra("ID", Integer.toString(gridItem.getId()));

                startActivity(intent);
            }
        });
    }

    private void showDialogBox() {
        //showing dialog Box
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Please enter movie name");

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog2.dismiss();
            }
        });

        alertDialog2 = alertDialogBuilder.create();
        alertDialog2.show();
    }

    private void searchForResults() {
        //get async task
        noConnection = false;
        volleySearch();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showDialogMsg() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("No Internet Connection, Please connect to internet firstly");

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                finish();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void volleySearch() {
        String link;
        link = ApiUrlPath.apiBaseUrl + ApiUrlPath.searchPath + ApiUrlPath.searchQueryParams + etMovieName.getText().toString() + "&" + getString(R.string.API_KEY);
        link = link.replaceAll(" ", "%20");

//        Log.e("volleySearch", "request done");
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, link,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            putDataInArrays(response);
//                            listViewAdapter.setData(listViewArrayList);
//                            mProgressBar.setVisibility(View.GONE);
//                            Log.e("volleySearch", "response done");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                showDialogBox();
//            }
//        });
//
//        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

        myStringRequest = VolleyUtils.makeStringRequest(this, link, this, ApiResponse.class, ApiUrlPath.searchPath);
    }

    private Void putDataInArrays(ApiResponse response) {

//        final Gson gson = new Gson();

        listViewArrayList.clear();

//        ApiResponse response = gson.fromJson(JsonStr, ApiResponse.class);

        listViewArrayList.addAll(response.results);

        return null;
    }

    @Override
    public void onError(String message, String uniquePath) {
        switch (uniquePath) {
            case ApiUrlPath.searchPath:
                showDialogBox();
                break;
        }
    }

    @Override
    public void onSuccess(Object response, String uniquePath) {
        mProgressBar.setVisibility(View.GONE);

        switch (uniquePath) {
            case ApiUrlPath.searchPath:

                putDataInArrays((ApiResponse) response);
                listViewAdapter.setData(listViewArrayList);

                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(myStringRequest != null & (!myStringRequest.isCanceled()))
            myStringRequest.cancel();
    }
}
