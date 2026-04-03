package com.example.css_javafx.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimeTest {

    @Test
    void constructor_shouldStoreAllFieldsCorrectly() {
        Anime anime = new Anime(
                101,
                "Naruto",
                "Story about ninja",
                "image.jpg",
                "https://example.com/naruto",
                8.5,
                1000,
                "TV",
                220,
                "Finished Airing",
                "24 min",
                "Spring",
                2002,
                "PG-13",
                List.of("Action", "Adventure"),
                List.of("Studio Pierrot"),
                "https://youtube.com/watch?v=123",
                "https://youtube.com/embed/123"
        );

        assertEquals(101, anime.getMalId());
        assertEquals("Naruto", anime.getTitle());
        assertEquals("Story about ninja", anime.getDescription());
        assertEquals("image.jpg", anime.getImageUrl());
        assertEquals("https://example.com/naruto", anime.getUrl());
        assertEquals(8.5, anime.getScore());
        assertEquals(1000, anime.getScoredBy());
        assertEquals("TV", anime.getType());
        assertEquals(220, anime.getEpisodes());
        assertEquals("Finished Airing", anime.getStatus());
        assertEquals("24 min", anime.getDuration());
        assertEquals("Spring", anime.getSeason());
        assertEquals(2002, anime.getYear());
        assertEquals("PG-13", anime.getRating());
        assertEquals(List.of("Action", "Adventure"), anime.getGenres());
        assertEquals(List.of("Studio Pierrot"), anime.getStudios());
        assertEquals("https://youtube.com/watch?v=123", anime.getTrailerUrl());
        assertEquals("https://youtube.com/embed/123", anime.getTrailerEmbedUrl());
    }

    @Test
    void setTrailerUrl_shouldUpdateTrailerUrl() {
        Anime anime = new Anime(
                1,
                "Test",
                "Desc",
                "img",
                "url",
                7.0,
                10,
                "TV",
                12,
                "Finished",
                "24 min",
                "Winter",
                2020,
                "PG",
                List.of("Action"),
                List.of("Studio"),
                "old-url",
                "old-embed"
        );

        anime.setTrailerUrl("new-url");

        assertEquals("new-url", anime.getTrailerUrl());
    }

    @Test
    void setTrailerEmbedUrl_shouldUpdateTrailerEmbedUrl() {
        Anime anime = new Anime(
                1,
                "Test",
                "Desc",
                "img",
                "url",
                7.0,
                10,
                "TV",
                12,
                "Finished",
                "24 min",
                "Winter",
                2020,
                "PG",
                List.of("Action"),
                List.of("Studio"),
                "old-url",
                "old-embed"
        );

        anime.setTrailerEmbedUrl("new-embed");

        assertEquals("new-embed", anime.getTrailerEmbedUrl());
    }
}