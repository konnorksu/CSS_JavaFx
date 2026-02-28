package com.example.css_javafx.dto;

public class AnimeDto {
    public String title;
    public String synopsis;
    public String url;
    public Images images;
    public AnimeTrailerDto trailer;

    public static class Images {
        public Jpg jpg;
    }
    public static class Jpg {
        public String image_url;
    }
}
