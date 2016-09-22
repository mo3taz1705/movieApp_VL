package com.example.moutaz.movieapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;


public class GridFragment extends Fragment implements VolleyResponseListener{

    private AlertDialog alertDialog;
    private int mPosition = 0;
    private static final String SELECTED_KEY = "selected_position";

//    @BindView(R.id.gridView) GridView gridView;
//    @BindView(R.id.progressBar) ProgressBar mProgressBar;
//    @BindView(R.id.tvNoFav) TextView noFavTV;
    private GridView gridView;
    private ProgressBar mProgressBar;
    private TextView noFavTV;

    private String lastStatus;
    private GridViewCustomAdapter gridViewAdapter;
    private ArrayList<GridItem> gridItemArrayList;
    private SharedPreferences sharedpreferences;
    private boolean noConnection;

    public interface Callback {
        public void onItemSelected(GridItem gridItem, boolean clicked);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        gridView = (GridView) rootView.findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        noFavTV = (TextView) rootView.findViewById(R.id.tvNoFav);

        gridItemArrayList = new ArrayList<>();

        getFromPreferences();

        gridViewAdapter = new GridViewCustomAdapter(getActivity(), R.layout.grid_item_layout, gridItemArrayList);
        gridView.setAdapter(gridViewAdapter);

        noConnection = false;
        if(lastStatus.equals("Fav")){
            updateView();
        }else {
            if(isOnline(getActivity())) {
                noConnection = false;
                volleyGetData();
                mProgressBar.setVisibility(View.VISIBLE);
            }else {
                showDialogMsg();
            }
        }


        //listener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                GridItem gridItem = (GridItem) parent.getItemAtPosition(position);
                mPosition = position;
                ((Callback) getActivity()).onItemSelected(gridItem, true);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        if(lastStatus.equals(getString(R.string.popular_tag))){
            menu.findItem(R.id.popularity).setChecked(true);
        }else if (lastStatus.equals(getString(R.string.rating_tag))){
            menu.findItem(R.id.rated).setChecked(true);
        }else{
            menu.findItem(R.id.favorites).setChecked(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveToPreferences();
    }

    private void saveToPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("state", lastStatus);
        if (mPosition < gridItemArrayList.size()) {
            editor.putInt(SELECTED_KEY, mPosition);
        }
        editor.commit();
    }

    private void getFromPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lastStatus = preferences.getString("state", "Popularity");
        mPosition = preferences.getInt(SELECTED_KEY, 0);
    }

    public void updateView() {

        if(lastStatus.equals("Fav")) {
            long size = sharedpreferences.getLong("size", 0);
            if (size == 0) {
                noFavTV.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
            }
            gridItemArrayList.clear();
            for (long i = 1; i <= size; i++) {
                if (sharedpreferences.contains(Long.toString(i) + "ID")) {
                    GridItem gridItem = new GridItem();
                    gridItem.setId(Integer.parseInt(sharedpreferences.getString(Long.toString(i) + "ID", "")));
                    gridItem.setOrigTitle(sharedpreferences.getString(Long.toString(i) + "title", ""));
                    gridItem.setRelDate(sharedpreferences.getString(Long.toString(i) + "year", ""));
                    gridItem.setVoteAvg(sharedpreferences.getString(Long.toString(i) + "rate", ""));
                    gridItem.setOverview(sharedpreferences.getString(Long.toString(i) + "overview", ""));
                    gridItem.setImage(sharedpreferences.getString(Long.toString(i) + "imageLink", ""));
                    gridItemArrayList.add(gridItem);
                }
            }
            if (gridItemArrayList.size() > 0) {
                gridViewAdapter.setGridData(gridItemArrayList);
                if(mPosition >= gridItemArrayList.size()) {
                    mPosition = 0;
                }
                gridView.smoothScrollToPosition(mPosition);
                ((Callback) getActivity()).onItemSelected(gridItemArrayList.get(mPosition), false);
            } else {
                noFavTV.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putLong("size", 0);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.popularity:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                if (!(lastStatus.equals(getString(R.string.popular_tag)))) {
                    mPosition = 0;
                    lastStatus = getString(R.string.popular_tag);
                    noConnection = false;
                    if(isOnline(getActivity())) {
                        noConnection = false;
                        volleyGetData();
                        mProgressBar.setVisibility(View.VISIBLE);
                        noFavTV.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);
                    }else{
                        showDialogMsg();
                    }
                }
                return true;
            case R.id.rated:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                if(!(lastStatus.equals(getString(R.string.rating_tag)))) {
                    mPosition = 0;
                    lastStatus = getString(R.string.rating_tag);
                    noConnection = false;
                    if(isOnline(getActivity())) {
                        noConnection = false;
                        volleyGetData();
                        mProgressBar.setVisibility(View.VISIBLE);
                        noFavTV.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);
                    }else{
                        showDialogMsg();
                    }
                }
                return true;
            case R.id.favorites:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                if(!(lastStatus.equals("Fav"))) {
                    mPosition = 0;
                    lastStatus = "Fav";
                    updateView();
                }
                return true;

            case R.id.search:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                //open search activity
                Intent intent = new Intent(getActivity().getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showDialogMsg() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("No Internet Connection, Please connect to internet firstly");

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                getActivity().finish();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public void volleyGetData() {
        String link;
        if(lastStatus.equals(getString(R.string.popular_tag))) {
            link = getString(R.string.popular_movies) + "&" + getString(R.string.API_KEY);
            VolleyUtils.makeStringRequest(getActivity(), link, this, ApiResponse.class, ApiUrlPath.popularMoviesPath);
        }else {
            link = getString(R.string.highest_rated_movies) + "&" + getString(R.string.API_KEY);
            VolleyUtils.makeStringRequest(getActivity(), link, this, ApiResponse.class, ApiUrlPath.highestRatedPath);
        }
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, link,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            putDataInArrays(response);
//                            gridViewAdapter.setGridData(gridItemArrayList);
//                            mProgressBar.setVisibility(View.GONE);
//                            if(mPosition >= gridItemArrayList.size()) {
//                                mPosition = 0;
//                            }
//                            gridView.smoothScrollToPosition(mPosition);
//                            ((Callback) getActivity()).onItemSelected(gridItemArrayList.get(mPosition), false);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                showDialogMsg();
//            }
//        });
//        VolleySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);



    }

    private Void putDataInArrays(ApiResponse response)throws JSONException {

//        final Gson gson = new Gson();

        gridItemArrayList.clear();

//        ApiResponse response = gson.fromJson(JsonStr, ApiResponse.class);

        gridItemArrayList.addAll(response.results);

        return null;
    }

    @Override
    public void onError(String message, String uniquePath) {
        if (uniquePath.equals(ApiUrlPath.popularMoviesPath) || uniquePath.equals(ApiUrlPath.highestRatedPath)) {
            showDialogMsg();
        }
    }

    @Override
    public void onSuccess(Object response, String uniquePath) {
        if (uniquePath.equals(ApiUrlPath.popularMoviesPath) || uniquePath.equals(ApiUrlPath.highestRatedPath)) {
            try {
                putDataInArrays((ApiResponse) response);
                gridViewAdapter.setGridData(gridItemArrayList);
                mProgressBar.setVisibility(View.GONE);
                if(mPosition >= gridItemArrayList.size()) {
                    mPosition = 0;
                }
                gridView.smoothScrollToPosition(mPosition);
                ((Callback) getActivity()).onItemSelected(gridItemArrayList.get(mPosition), false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
