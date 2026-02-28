package com.example.css_javafx;

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

    // Текущий сезон (новые)
    private static final String API = "https://api.jikan.moe/v4/seasons/now?page=";

    /**
     * Загружаем новые аниме, но возвращаем только те, где есть трейлер.
     * @return список аниме (например 24 штуки)
     */
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
            String url = API + page;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "JavaFX-App")
                    .GET()
                    .build();

            String json = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            AnimeResponse response = gson.fromJson(json, AnimeResponse.class);

            if (response == null || response.data == null || response.data.isEmpty()) {
                break;
            }

            for (var dto : response.data) {
                String trailerUrl = null;
                String trailerEmbedUrl = null;

                if (dto.trailer != null) {
                    trailerUrl = dto.trailer.url;
                    trailerEmbedUrl = dto.trailer.embed_url;
                }

                // фильтруем: берём только те, где реально есть трейлер
                boolean hasTrailer =
                        (trailerEmbedUrl != null && !trailerEmbedUrl.isBlank()) ||
                                (trailerUrl != null && !trailerUrl.isBlank());

                if (!hasTrailer) continue;

                String img = "";
                if (dto.images != null && dto.images.jpg != null && dto.images.jpg.image_url != null) {
                    img = dto.images.jpg.image_url;
                }

                String synopsis = dto.synopsis != null ? dto.synopsis : "";

                result.add(new Anime(
                        dto.title,
                        synopsis,
                        img,
                        dto.url,
                        trailerUrl,
                        trailerEmbedUrl,
                        null   // trailerVideoUrl (mp4/m3u8) — пока нет, можно заполнить позже
                ));

                if (result.size() >= limit) break;
            }

            page++;
        }

        return result;
    }
}