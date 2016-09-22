package com.example.moutaz.movieapp;

import java.util.List;

/**
 * Created by Moutaz on 7/19/2016.
 */
public class ReviewsApiResponse{
    private int id;
    private int page;
    public List<ReviewResults> results;
    private long total_pages;
    private long total_results;
}