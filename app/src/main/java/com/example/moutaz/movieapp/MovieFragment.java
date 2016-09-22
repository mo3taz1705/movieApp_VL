package com.example.moutaz.movieapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


public class MovieFragment extends Fragment {

    private String ID;
//    @BindView(R.id.ivPoster) NetworkImageView poster;
//    @BindView(R.id.tvTitle) TextView title;
//    @BindView(R.id.tvYear) TextView year;
//    @BindView(R.id.tvRate) TextView rate;
//    @BindView(R.id.tvOverview) TextView overview;
//    @BindView(R.id.listView) ListView listView;
//    @BindView(R.id.bFav) Button favBtn;
    private NetworkImageView poster;
    private Button favBtn;
    private ListView listView;
    private TextView title, year, rate, overview;

    private int trailersSize;
    private int reviewsSize;
    private ArrayList<String> listData;
    private ListViewCustomAdapter listViewAdapter;

    private String imageLink;
    private boolean trailersDone;
    private SharedPreferences sharedpreferences;
    private boolean noConnection;
    private String className;
    private Activity currActivity;

    private void initialize(View view) {
        poster = (NetworkImageView) view.findViewById(R.id.ivPoster);
        title = (TextView) view.findViewById(R.id.tvTitle);
        year = (TextView) view.findViewById(R.id.tvYear);
        rate = (TextView) view.findViewById(R.id.tvRate);
        overview = (TextView) view.findViewById(R.id.tvOverview);
        overview.setMovementMethod(new ScrollingMovementMethod());
        listView = (ListView) view.findViewById(R.id.listView);
        favBtn = (Button) view.findViewById(R.id.bFav);


        trailersDone = false;
        trailersSize = reviewsSize = 0;
        listData = new ArrayList<>();
        listViewAdapter = new ListViewCustomAdapter(getActivity(), R.layout.reviews_layout, listData, trailersSize, reviewsSize);
        listView.setAdapter(listViewAdapter);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        currActivity = activity;
        className = activity.getClass().getSimpleName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initialize(rootView);
        Log.d("ID","HELLO");
        noConnection = false;

        Bundle arguments = getArguments();
        if (arguments == null) {
            title.setText("null");
            year.setText("null");
            rate.setText("null");
            overview.setText("null");
        }else {
            ID = arguments.getString("ID");
            Log.e("movie frag arguments", ID);
            if (sharedpreferences.contains(ID)) {
                favBtn.setText("remove from Favorites");
            }
            title.setText(arguments.getString("TITLE"));
            year.setText(arguments.getString("YEAR"));
            rate.setText(arguments.getString("RATE"));
            overview.setText(arguments.getString("OVERVIEW"));

            imageLink = "http://image.tmdb.org/t/p/w185/" + arguments.getString("IMAGE");
            volleyLoadImage(imageLink, poster);
        }

        volleyGetTrailers();
        // Item Click Listener for the listview
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View container, int position, long id) {
                if (position < trailersSize) {
                    String link = ((TextView) container.findViewById(R.id.tvLink)).getText().toString();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/v/" + link)));
                }
            }
        };

        // Setting the item click listener for the listview
        listView.setOnItemClickListener(itemClickListener);

        favBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                long size;
                SharedPreferences.Editor editor = sharedpreferences.edit();

                if(sharedpreferences.contains(ID)){ //remove from fav


                    size = sharedpreferences.getLong("size", 0);
                    String tempSize = sharedpreferences.getString(ID, "");
                    editor.remove(ID);
                    editor.remove(tempSize + "ID");
                    editor.remove(tempSize + "title");
                    editor.remove(tempSize + "year");
                    editor.remove(tempSize + "rate");
                    editor.remove(tempSize + "overview");
                    editor.remove(tempSize + "imageLink");
                    editor.putLong("size", size);
                    editor.commit();
                    favBtn.setText("add to Favorites");
                }else {
                    size = sharedpreferences.getLong("size", 0);
                    size++;
                    editor.putLong("size", size);
                    editor.putString(ID, Long.toString(size));
                    editor.putString(Long.toString(size) + "ID", ID);
                    editor.putString(Long.toString(size) + "title", title.getText().toString());
                    editor.putString(Long.toString(size) + "year", year.getText().toString());
                    editor.putString(Long.toString(size) + "rate", rate.getText().toString());
                    editor.putString(Long.toString(size) + "overview", overview.getText().toString());
                    editor.putString(Long.toString(size) + "imageLink", imageLink);
                    editor.commit();
                    favBtn.setText("remove from Favorites");
                }

                if(className.equals("MainActivity")){
                    ((MainActivity)currActivity).updateView();
                }
            }

        });


        return rootView;
    }

    public void volleyLoadImage(String link, NetworkImageView mNetworkImageView){
        ImageLoader mImageLoader;
        mImageLoader = new ImageLoader(Volley.newRequestQueue(getActivity()), new LruBitmapCache());
        mNetworkImageView.setImageUrl(link, mImageLoader);
    }

    private void volleyGetTrailers() {
        Log.e("ID",ID+"" );
        String link = "http://api.themoviedb.org/3/movie/" + ID + "/videos?" + getString(R.string.API_KEY);

        Log.e("volleyGetTrailers", link);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("volleyGetTrailers", "response done");
                            putDataInArrays(response, "trailer");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        trailersDone = true;
                        noConnection = false;
                        volleyGetReviews();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        Log.e("volleyGetTrailers", "request done");
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void volleyGetReviews() {

        String link = "http://api.themoviedb.org/3/movie/"+ ID +"/reviews?" + getString(R.string.API_KEY);
        Log.e("volleyGetReviews", "strat");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, link,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("volleyGetReviews", "response done");
                            putDataInArrays(response, "reviews");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                                listViewAdapter.setListData(listData, trailersSize, reviewsSize);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        VolleySingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
        Log.e("volleyGetReviews", "request done");
    }

    private void putDataInArrays(String JsonStr, String param)throws JSONException {

        final Gson gson = new Gson();

        JSONObject MJson = new JSONObject(JsonStr);
        JSONArray movieArray = MJson.getJSONArray("results");

        if(param.equals("trailer")){
            trailersSize = movieArray.length();
            listData.clear();
            VideosApiResponse response = gson.fromJson(JsonStr, VideosApiResponse.class);
            List<VideoResults> resultsList = response.results;
            for (int i = 0; i < resultsList.size(); i++) {
                listData.add(resultsList.get(i).name);
                listData.add(resultsList.get(i).key);
            }
        }else {
            reviewsSize = movieArray.length();
            ReviewsApiResponse response = gson.fromJson(JsonStr, ReviewsApiResponse.class);
            List<ReviewResults> resultsList = response.results;
            for(int i = 0; i < resultsList.size(); i++){
                listData.add(resultsList.get(i).author);
                listData.add(resultsList.get(i).content);
            }
        }

        return;
    }

    private void showDialogMsg() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currActivity);
        alertDialogBuilder.setMessage("No Internet Connection, Please connect to internet firstly");

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                currActivity.finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
