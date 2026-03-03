package com.example.css_javafx;

import com.example.css_javafx.dto.AnimeDto;
import com.example.css_javafx.dto.AnimeResponse;
import com.example.css_javafx.model.Anime;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AnimeService {

    private static final String API = "https://api.jikan.moe/v4/seasons/now?page=";
    private static final String DETAILS_API = "https://api.jikan.moe/v4/anime/";

    public static List<Anime> loadAnime() throws Exception {
        return loadNewAnimeWithTrailers(24);
    }

    public static List<Anime> loadNewAnimeWithTrailers(int limit) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        Gson gson = new Gson();
        List<Anime> result = new ArrayList<>();
        int page = 1;

        while (result.size() < limit && page <= 10) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API + page))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "JavaFX-App")
                    .GET()
                    .build();

            String json = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            AnimeResponse response = gson.fromJson(json, AnimeResponse.class);

            if (response == null || response.data == null || response.data.isEmpty()) break;

            for (var dto : response.data) {

                String trailerUrl = extractTrailerWatchUrl(dto.trailer);
                String trailerEmbedUrl = dto.trailer != null ? dto.trailer.embed_url : null;

                String img = (dto.images != null && dto.images.jpg != null && dto.images.jpg.image_url != null)
                        ? dto.images.jpg.image_url : "";

                String synopsis = dto.synopsis != null ? dto.synopsis : "";

                List<String> genres = (dto.genres == null) ? List.of()
                        : dto.genres.stream().map(g -> g.name).filter(s -> s != null && !s.isBlank()).toList();

                List<String> studios = (dto.studios == null) ? List.of()
                        : dto.studios.stream().map(s -> s.name).filter(s -> s != null && !s.isBlank()).toList();

                result.add(new Anime(
                        dto.mal_id,
                        dto.title,
                        synopsis,
                        img,
                        dto.url,

                        dto.score,
                        dto.scored_by,

                        dto.type,
                        dto.episodes,
                        dto.status,
                        dto.duration,
                        dto.season,
                        dto.year,
                        dto.rating,

                        genres,
                        studios,

                        trailerUrl,
                        trailerEmbedUrl
                ));

                if (result.size() >= limit) break;
            }

            page++;
        }

        return result;
    }

    // Для Details: вернуть только watch-url трейлера
    public static String loadTrailerWatchUrlByMalId(int malId) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        Gson gson = new Gson();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DETAILS_API + malId))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "JavaFX-App")
                .GET()
                .build();

        String json = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        AnimeDetailsResponse details = gson.fromJson(json, AnimeDetailsResponse.class);

        if (details == null || details.data == null) return null;

        return extractTrailerWatchUrl(details.data.trailer);
    }

    private static String extractTrailerWatchUrl(AnimeDto.Trailer trailer) {
        if (trailer == null) return null;

        if (trailer.youtube_id != null && !trailer.youtube_id.isBlank()) {
            return "https://www.youtube.com/watch?v=" + trailer.youtube_id;
        }
        if (trailer.embed_url != null && !trailer.embed_url.isBlank()) {
            return convertEmbedToWatch(trailer.embed_url);
        }
        if (trailer.url != null && !trailer.url.isBlank()) {
            return trailer.url;
        }
        return null;
    }

    private static String convertEmbedToWatch(String embedUrl) {
        if (embedUrl == null || embedUrl.isBlank()) return null;

        int index = embedUrl.indexOf("/embed/");
        if (index == -1) return embedUrl;

        String videoId = embedUrl.substring(index + 7);
        int qIndex = videoId.indexOf("?");
        if (qIndex != -1) videoId = videoId.substring(0, qIndex);

        if (videoId.isBlank()) return null;

        return "https://www.youtube.com/watch?v=" + videoId;
    }

    private static class AnimeDetailsResponse {
        public AnimeDto data;
    }
}