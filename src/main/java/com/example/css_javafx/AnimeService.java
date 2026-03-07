package com.example.css_javafx;

import com.example.css_javafx.dto.AnimeDto;
import com.example.css_javafx.dto.AnimeResponse;
import com.example.css_javafx.model.Anime;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AnimeService {

    private static final String SEASON_API = "https://api.jikan.moe/v4/seasons/now?page=";
    private static final String SEARCH_API = "https://api.jikan.moe/v4/anime?q=%s&limit=%d";
    private static final String DETAILS_API = "https://api.jikan.moe/v4/anime/";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Gson GSON = new Gson();

    public static List<Anime> loadAnime() throws Exception {
        return loadNewAnime(24);
    }

    public static List<Anime> loadNewAnime(int limit) throws Exception {
        List<Anime> result = new ArrayList<>();
        int page = 1;

        while (result.size() < limit) {
            String json = fetchJson(SEASON_API + page);
            AnimeResponse response = GSON.fromJson(json, AnimeResponse.class);

            if (response == null || response.data == null || response.data.isEmpty()) {
                break;
            }

            for (AnimeDto dto : response.data) {
                if (isHentai(dto)) continue;

                result.add(mapDtoToAnime(dto));
                if (result.size() >= limit) break;
            }

            page++;

            // небольшая пауза, чтобы не ловить rate limit
            Thread.sleep(350);
        }

        return result;
    }

    public static List<Anime> searchAnime(String query, int limit) throws Exception {
        if (query == null || query.isBlank()) {
            return loadNewAnime(limit);
        }

        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = String.format(SEARCH_API, encodedQuery, limit);

        String json = fetchJson(url);
        AnimeResponse response = GSON.fromJson(json, AnimeResponse.class);

        if (response == null || response.data == null || response.data.isEmpty()) {
            return List.of();
        }

        return response.data.stream()
                .filter(dto -> !isHentai(dto))
                .map(AnimeService::mapDtoToAnime)
                .toList();
    }

    public static String loadTrailerWatchUrlByMalId(int malId) throws Exception {
        String json = fetchJson(DETAILS_API + malId);
        AnimeDetailsResponse details = GSON.fromJson(json, AnimeDetailsResponse.class);

        if (details == null || details.data == null) return null;

        AnimeDto dto = details.data;
        return extractTrailerWatchUrl(dto.trailer);
    }

    public static List<Anime> loadAnimePage(int page, int limit) throws Exception {
        String json = fetchJson(SEASON_API + page);
        AnimeResponse response = GSON.fromJson(json, AnimeResponse.class);

        if (response == null || response.data == null || response.data.isEmpty()) {
            return List.of();
        }

        return response.data.stream()
                .filter(dto -> !isHentai(dto))
                .limit(limit)
                .map(AnimeService::mapDtoToAnime)
                .toList();
    }

    public static List<Anime> searchAnimePage(String query, int page, int limit) throws Exception {
        if (query == null || query.isBlank()) return List.of();

        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = "https://api.jikan.moe/v4/anime?q=" + encodedQuery + "&page=" + page + "&limit=" + limit;

        String json = fetchJson(url);
        AnimeResponse response = GSON.fromJson(json, AnimeResponse.class);

        if (response == null || response.data == null || response.data.isEmpty()) {
            return List.of();
        }

        return response.data.stream()
                .map(AnimeService::mapDtoToAnime)
                .toList();
    }

    private static boolean isHentai(AnimeDto dto) {
        if (dto == null || dto.genres == null) return false;

        return dto.genres.stream()
                .map(g -> g.name)
                .filter(name -> name != null && !name.isBlank())
                .anyMatch(name -> name.equalsIgnoreCase("Hentai"));
    }

    private static Anime mapDtoToAnime(AnimeDto dto) {
        String trailerUrl = null;
        String trailerEmbedUrl = null;

        if (dto.trailer != null) {
            trailerEmbedUrl = dto.trailer.embed_url;

            if (dto.trailer.youtube_id != null && !dto.trailer.youtube_id.isBlank()) {
                trailerUrl = "https://www.youtube.com/watch?v=" + dto.trailer.youtube_id;
            } else if (dto.trailer.embed_url != null && !dto.trailer.embed_url.isBlank()) {
                trailerUrl = convertEmbedToWatch(dto.trailer.embed_url);
            } else if (dto.trailer.url != null && !dto.trailer.url.isBlank()) {
                trailerUrl = dto.trailer.url;
            }
        }

        String img = (dto.images != null && dto.images.jpg != null && dto.images.jpg.image_url != null)
                ? dto.images.jpg.image_url : "";

        String synopsis = dto.synopsis != null ? dto.synopsis : "";

        List<String> genres = (dto.genres == null) ? List.of()
                : dto.genres.stream()
                .map(g -> g.name)
                .filter(s -> s != null && !s.isBlank())
                .toList();

        List<String> studios = (dto.studios == null) ? List.of()
                : dto.studios.stream()
                .map(s -> s.name)
                .filter(s -> s != null && !s.isBlank())
                .toList();

        return new Anime(
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
        );
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
        if (qIndex != -1) {
            videoId = videoId.substring(0, qIndex);
        }

        if (videoId.isBlank()) return null;
        return "https://www.youtube.com/watch?v=" + videoId;
    }

    private static String fetchJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "JavaFX-App")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = sendWithRetry(request);

        int status = response.statusCode();
        String body = response.body();

        if (status >= 200 && status < 300) {
            return body;
        }

        if (status == 404) {
            throw new IOException("Данные не найдены");
        }

        if (status == 429) {
            throw new IOException("Слишком много запросов к API. Подожди немного и попробуй снова.");
        }

        if (status >= 500) {
            throw new IOException("Сервис Jikan временно недоступен (" + status + ")");
        }

        throw new IOException("Ошибка API: HTTP " + status);
    }

    private static HttpResponse<String> sendWithRetry(HttpRequest request) throws Exception {
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            Thread.sleep(1200);
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        }

        return response;
    }

    private static class AnimeDetailsResponse {
        public AnimeDto data;
    }
}