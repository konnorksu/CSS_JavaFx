package com.example.css_javafx.model;

import java.util.List;

public class Anime {
    private final String title;
    private final String description;
    private final String imageUrl;
    private final String url;

    private final Double score;
    private final Integer scoredBy;

    private final String type;
    private final Integer episodes;
    private final String status;
    private final String duration;
    private final String season;
    private final Integer year;
    private final String rating;

    private final List<String> genres;
    private final List<String> studios;

    private String trailerUrl;
    private String trailerEmbedUrl;
    private final int malId;

    public Anime(
            int malId,
            String title,
            String description,
            String imageUrl,
            String url,
            Double score,
            Integer scoredBy,
            String type,
            Integer episodes,
            String status,
            String duration,
            String season,
            Integer year,
            String rating,
            List<String> genres,
            List<String> studios,
            String trailerUrl,
            String trailerEmbedUrl
    ) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.url = url;
        this.score = score;
        this.scoredBy = scoredBy;
        this.type = type;
        this.episodes = episodes;
        this.status = status;
        this.duration = duration;
        this.season = season;
        this.year = year;
        this.rating = rating;
        this.genres = genres;
        this.studios = studios;
        this.trailerUrl = trailerUrl;
        this.trailerEmbedUrl = trailerEmbedUrl;
        this.malId = malId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getUrl() { return url; }
    public int getMalId() {return malId; }

    public Double getScore() { return score; }
    public Integer getScoredBy() { return scoredBy; }

    public String getType() { return type; }
    public Integer getEpisodes() { return episodes; }
    public String getStatus() { return status; }
    public String getDuration() { return duration; }
    public String getSeason() { return season; }
    public Integer getYear() { return year; }
    public String getRating() { return rating; }

    public List<String> getGenres() { return genres; }
    public List<String> getStudios() { return studios; }

    public String getTrailerUrl() { return trailerUrl; }
    public String getTrailerEmbedUrl() { return trailerEmbedUrl; }
    public void setTrailerUrl(String trailerUrl){ this.trailerUrl = trailerUrl; }
    public void setTrailerEmbedUrl(String trailerEmbedUrl) { this.trailerEmbedUrl = trailerEmbedUrl; }

}