package com.example.css_javafx.model;

public class Anime {
    private final String title;
    private final String description;
    private final String imageUrl;
    private final String url;

    public Anime(String title, String description, String imageUrl, String url,
                 String trailerUrl, String trailerEmbedUrl,
                 String trailerVideoUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.url = url;
//        this.trailerUrl = trailerUrl;
//        this.trailerEmbedUrl = trailerEmbedUrl;
//        this.trailerVideoUrl = trailerVideoUrl;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getUrl() { return url; }

}