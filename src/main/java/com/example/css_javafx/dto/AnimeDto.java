package com.example.css_javafx.dto;

import java.util.List;

public class AnimeDto {
    public String title;
    public String synopsis;
    public String url;
    public int mal_id;

    public Double score;
    public Integer scored_by;

    public String type;
    public Integer episodes;
    public String status;
    public String duration;

    public String season;
    public Integer year;
    public String rating;

    public Images images;
    public List<NameItem> genres;
    public List<NameItem> studios;

    public Trailer trailer;

    public static class Images {
        public Jpg jpg;
    }
    public static class Jpg {
        public String image_url;
    }

    public static class NameItem {
        public String name;
    }

    public static class Trailer {
        public String url;
        public String embed_url;
        public String youtube_id;
    }
}