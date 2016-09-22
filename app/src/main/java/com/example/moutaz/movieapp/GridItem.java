package com.example.moutaz.movieapp;

import java.util.List;
import com.google.gson.annotations.SerializedName;
/**
 * Created by moutaz on 12/16/2015.
 */
public class GridItem {
    @SerializedName("poster_path")
    private String image;
    private Boolean adult;
    private String overview;
    @SerializedName("release_date")
    private String relDate;
    private List<Integer> genre_ids;
    private int id;
    @SerializedName("original_title")
    private String origTitle;
    private String original_language;
    private String title;
    private String backdrop_path;
    private double popularity;
    private long vote_count;
    private Boolean video;
    @SerializedName("vote_average")
    private String voteAvg;

    public GridItem(){}

    public GridItem(String i){
        super();
        this.image = i ;
    }

    public void setImage(String i){
        this.image = i;
    }

    public String getImage(){
        return this.image;
    }

    public String getRelDate() {
        return relDate;
    }

    public void setRelDate(String relDate) {
        this.relDate = relDate;
        if(relDate.length() > 3)
            this.relDate = relDate.substring(0, 4);
    }

    public String getVoteAvg() {
        return voteAvg;
    }

    public void setVoteAvg(String voteAvg) {
        if(voteAvg.contains("/10"))
            this.voteAvg = voteAvg;
        else
            this.voteAvg = voteAvg + "/10";
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOrigTitle() {
        return origTitle;
    }

    public void setOrigTitle(String origTitle) {
        this.origTitle = origTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
