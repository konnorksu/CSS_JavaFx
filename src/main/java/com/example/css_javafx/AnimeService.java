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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AnimeService {

    private static final String SEASON_NOW_API = "https://api.jikan.moe/v4/seasons/now?page=";
    private static final String SEASON_YEAR_API = "https://api.jikan.moe/v4/seasons/%d/%s?page=%d";
    private static final String SEARCH_API = "https://api.jikan.moe/v4/anime";
    private static final String DETAILS_API = "https://api.jikan.moe/v4/anime/";
    private static final String GENRES_API = "https://api.jikan.moe/v4/genres/anime?filter=genres";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Gson GSON = new Gson();

    private static Map<String, Integer> cachedGenreMap;

    public static List<Anime> loadAnime() throws Exception {
        return loadNewAnime(24);
    }

    public static List<Anime> loadNewAnime(int limit) throws Exception {
        List<Anime> result = new ArrayList<>();
        int page = 1;

        while (result.size() < limit) {
            String json = fetchJson(SEASON_NOW_API + page);
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
            Thread.sleep(500);
        }

        return result;
    }

    public static List<Anime> searchAnime(String query, int limit) throws Exception {
        if (query == null || query.isBlank()) {
            return loadNewAnime(limit);
        }

        String url = SEARCH_API
                + "?q=" + encode(query.trim())
                + "&limit=" + limit
                + "&sfw=true"
                + "&order_by=score"
                + "&sort=desc";

        String json = fetchJson(url);
        AnimeResponse response = GSON.fromJson(json, AnimeResponse.class);

        if (response == null || response.data == null || response.data.isEmpty()) {
            return List.of();
        }

        List<AnimeDto> filtered = response.data.stream()
                .filter(dto -> !isHentai(dto))
                .toList();

        List<AnimeDto> exactMatches = filtered.stream()
                .filter(dto -> isExactTitleMatch(dto, query))
                .toList();

        if (!exactMatches.isEmpty()) {
            return exactMatches.stream()
                    .map(AnimeService::mapDtoToAnime)
                    .toList();
        }

        return filtered.stream()
                .map(AnimeService::mapDtoToAnime)
                .toList();
    }

    public static List<Anime> loadAnimePage(int page, int limit) throws Exception {
        String json = fetchJson(SEASON_NOW_API + page);
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

        String url = SEARCH_API
                + "?q=" + encode(query.trim())
                + "&page=" + page
                + "&limit=" + limit
                + "&sfw=true"
                + "&order_by=score"
                + "&sort=desc";

        String json = fetchJson(url);
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

    public static List<Anime> searchAnimeAdvanced(String title,
                                                  String studio,
                                                  String type,
                                                  String season,
                                                  Integer year,
                                                  Double minScore,
                                                  List<String> genres) throws Exception {
        AdvancedSearchOptions options = new AdvancedSearchOptions(
                title,
                studio,
                type,
                season,
                year,
                minScore,
                genres
        );

        List<Integer> genreIds = resolveGenreIds(options.selectedGenres);
        LinkedHashMap<Integer, Anime> unique = new LinkedHashMap<>();

        if (options.year != null) {
            if (options.hasSeason()) {
                collectSeasonCandidates(unique, options.year, options.season, 3);
            } else if (options.title.isBlank() && options.studio.isBlank() && genreIds.isEmpty()) {
                collectSeasonCandidates(unique, options.year, "winter", 2);
                collectSeasonCandidates(unique, options.year, "spring", 2);
                collectSeasonCandidates(unique, options.year, "summer", 2);
                collectSeasonCandidates(unique, options.year, "fall", 2);
            }
        }

        if (!options.title.isBlank()) {
            collectSearchCandidates(
                    unique,
                    buildAnimeSearchUrl(options.title, 1, 25, options, genreIds.size() == 1 ? List.of(genreIds.get(0)) : List.of()),
                    4
            );
        }

        if (!options.studio.isBlank() && !normalize(options.studio).equals(normalize(options.title))) {
            collectSearchCandidates(
                    unique,
                    buildAnimeSearchUrl(options.studio, 1, 25, options, genreIds.size() == 1 ? List.of(genreIds.get(0)) : List.of()),
                    3
            );
        }

        for (Integer genreId : genreIds) {
            collectSearchCandidates(
                    unique,
                    buildAnimeSearchUrl("", 1, 25, options, List.of(genreId)),
                    3
            );
        }

        if (unique.size() < 35) {
            collectSearchCandidates(
                    unique,
                    buildAnimeSearchUrl("", 1, 25, options, genreIds.size() == 1 ? List.of(genreIds.get(0)) : List.of()),
                    2
            );
        }

        List<Anime> filtered = unique.values().stream()
                .filter(anime -> matchesNonGenreFilters(anime, options))
                .toList();

        if (options.selectedGenres.isEmpty()) {
            return filtered.stream()
                    .sorted(
                            Comparator.comparingInt((Anime anime) -> advancedScore(anime, options)).reversed()
                                    .thenComparing(anime -> anime.getScore() == null ? 0.0 : anime.getScore(), Comparator.reverseOrder())
                                    .thenComparing(anime -> anime.getTitle() == null ? "" : anime.getTitle(), String.CASE_INSENSITIVE_ORDER)
                    )
                    .toList();
        }

        List<Anime> andMatches = filtered.stream()
                .filter(anime -> matchedGenreCount(anime, options.selectedGenres) == options.selectedGenres.size())
                .sorted(
                        Comparator.comparingInt((Anime anime) -> advancedScore(anime, options)).reversed()
                                .thenComparing(anime -> anime.getScore() == null ? 0.0 : anime.getScore(), Comparator.reverseOrder())
                                .thenComparing(anime -> anime.getTitle() == null ? "" : anime.getTitle(), String.CASE_INSENSITIVE_ORDER)
                )
                .toList();

        List<Anime> orMatches = filtered.stream()
                .filter(anime -> {
                    int matched = matchedGenreCount(anime, options.selectedGenres);
                    return matched > 0 && matched < options.selectedGenres.size();
                })
                .sorted(
                        Comparator.comparingInt((Anime anime) -> matchedGenreCount(anime, options.selectedGenres)).reversed()
                                .thenComparing(Comparator.comparingInt((Anime anime) -> advancedScore(anime, options)).reversed())
                                .thenComparing(anime -> anime.getScore() == null ? 0.0 : anime.getScore(), Comparator.reverseOrder())
                                .thenComparing(anime -> anime.getTitle() == null ? "" : anime.getTitle(), String.CASE_INSENSITIVE_ORDER)
                )
                .toList();

        List<Anime> result = new ArrayList<>(andMatches.size() + orMatches.size());
        result.addAll(andMatches);
        result.addAll(orMatches);
        return result;
    }

    public static String loadTrailerWatchUrlByMalId(int malId) throws Exception {
        String json = fetchJson(DETAILS_API + malId);
        AnimeDetailsResponse details = GSON.fromJson(json, AnimeDetailsResponse.class);

        if (details == null || details.data == null) return null;

        return extractTrailerWatchUrl(details.data.trailer);
    }

    private static void collectSearchCandidates(LinkedHashMap<Integer, Anime> unique,
                                                String firstPageUrl,
                                                int pages) throws Exception {
        if (firstPageUrl == null || firstPageUrl.isBlank()) return;

        for (int page = 1; page <= pages; page++) {
            String url = replaceOrAppendPage(firstPageUrl, page);
            String json = fetchJson(url);
            AnimeResponse response = GSON.fromJson(json, AnimeResponse.class);

            if (response == null || response.data == null || response.data.isEmpty()) {
                break;
            }

            for (AnimeDto dto : response.data) {
                if (isHentai(dto)) continue;
                unique.putIfAbsent(dto.mal_id, mapDtoToAnime(dto));
            }

            if (unique.size() >= 180) {
                return;
            }

            Thread.sleep(500);
        }
    }

    private static void collectSeasonCandidates(LinkedHashMap<Integer, Anime> unique,
                                                int year,
                                                String season,
                                                int pages) throws Exception {
        if (season == null || season.isBlank()) return;

        String safeSeason = season.trim().toLowerCase(Locale.ROOT);

        for (int page = 1; page <= pages; page++) {
            String url = String.format(Locale.US, SEASON_YEAR_API, year, safeSeason, page);
            String json = fetchJson(url);
            AnimeResponse response = GSON.fromJson(json, AnimeResponse.class);

            if (response == null || response.data == null || response.data.isEmpty()) {
                break;
            }

            for (AnimeDto dto : response.data) {
                if (isHentai(dto)) continue;
                unique.putIfAbsent(dto.mal_id, mapDtoToAnime(dto));
            }

            if (unique.size() >= 180) {
                return;
            }

            Thread.sleep(500);
        }
    }

    private static String buildAnimeSearchUrl(String query,
                                              int page,
                                              int limit,
                                              AdvancedSearchOptions options,
                                              List<Integer> genreIds) {
        StringBuilder url = new StringBuilder(SEARCH_API)
                .append("?page=").append(page)
                .append("&limit=").append(limit)
                .append("&sfw=true")
                .append("&order_by=score")
                .append("&sort=desc");

        if (query != null && !query.isBlank()) {
            appendParam(url, "q", query.trim());
        }

        if (options.type != null && !options.type.isBlank()) {
            appendParam(url, "type", options.type.toLowerCase(Locale.ROOT));
        }

        if (options.minScore != null) {
            appendParam(url, "min_score", formatDecimal(options.minScore));
        }

        if (!genreIds.isEmpty()) {
            appendParam(
                    url,
                    "genres",
                    genreIds.stream().map(String::valueOf).collect(Collectors.joining(","))
            );
        }

        return url.toString();
    }

    private static void appendParam(StringBuilder url, String key, String value) {
        if (value == null || value.isBlank()) return;
        url.append("&")
                .append(key)
                .append("=")
                .append(encode(value));
    }

    private static String replaceOrAppendPage(String url, int page) {
        if (url.contains("page=")) {
            return url.replaceAll("page=\\d+", "page=" + page);
        }
        return url + "&page=" + page;
    }

    private static List<Integer> resolveGenreIds(List<String> genreNames) throws Exception {
        if (genreNames == null || genreNames.isEmpty()) {
            return List.of();
        }

        Map<String, Integer> genreMap = loadGenreMap();
        List<Integer> ids = new ArrayList<>();

        for (String name : genreNames) {
            Integer id = genreMap.get(normalize(name));
            if (id != null && !ids.contains(id)) {
                ids.add(id);
            }
        }

        return ids;
    }

    private static synchronized Map<String, Integer> loadGenreMap() throws Exception {
        if (cachedGenreMap != null && !cachedGenreMap.isEmpty()) {
            return cachedGenreMap;
        }

        Map<String, Integer> result = new LinkedHashMap<>();

        try {
            String json = fetchJson(GENRES_API);
            GenreResponse response = GSON.fromJson(json, GenreResponse.class);

            if (response != null && response.data != null) {
                for (AnimeDto.NameItem item : response.data) {
                    if (item != null && item.name != null && !item.name.isBlank()) {
                        result.put(normalize(item.name), item.mal_id);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        cachedGenreMap = result;
        return cachedGenreMap;
    }

    private static boolean matchesNonGenreFilters(Anime anime, AdvancedSearchOptions options) {
        if (anime == null) return false;

        if (!options.title.isBlank() && !looseContains(anime.getTitle(), options.title)) {
            return false;
        }

        if (!options.studio.isBlank()) {
            boolean studioMatch = safeStrings(anime.getStudios()).stream()
                    .anyMatch(studio -> looseContains(studio, options.studio));
            if (!studioMatch) {
                return false;
            }
        }

        if (!options.type.isBlank()) {
            if (anime.getType() == null || !anime.getType().equalsIgnoreCase(options.type)) {
                return false;
            }
        }

        if (options.year != null) {
            if (anime.getYear() == null || !anime.getYear().equals(options.year)) {
                return false;
            }
        }

        if (options.hasSeason()) {
            if (anime.getSeason() == null || !anime.getSeason().equalsIgnoreCase(options.season)) {
                return false;
            }
        }

        if (options.minScore != null) {
            if (anime.getScore() == null || anime.getScore() < options.minScore) {
                return false;
            }
        }

        return true;
    }

    private static int matchedGenreCount(Anime anime, List<String> selectedGenres) {
        if (anime == null || selectedGenres == null || selectedGenres.isEmpty()) {
            return 0;
        }

        Set<String> animeGenres = safeStrings(anime.getGenres()).stream()
                .map(AnimeService::normalize)
                .collect(Collectors.toSet());

        int matched = 0;
        for (String genre : selectedGenres) {
            if (animeGenres.contains(normalize(genre))) {
                matched++;
            }
        }

        return matched;
    }

    private static int advancedScore(Anime anime, AdvancedSearchOptions options) {
        int score = 0;

        if (!options.title.isBlank()) {
            String animeTitle = normalize(anime.getTitle());
            String queryTitle = normalize(options.title);

            if (animeTitle.equals(queryTitle)) score += 120;
            else if (animeTitle.startsWith(queryTitle)) score += 80;
            else if (animeTitle.contains(queryTitle)) score += 45;
            else if (allTokensPresent(animeTitle, queryTitle)) score += 25;
        }

        if (!options.studio.isBlank()) {
            boolean studioMatch = safeStrings(anime.getStudios()).stream()
                    .map(AnimeService::normalize)
                    .anyMatch(studio -> studio.contains(normalize(options.studio)));

            if (studioMatch) {
                score += 25;
            }
        }

        if (!options.selectedGenres.isEmpty()) {
            score += matchedGenreCount(anime, options.selectedGenres) * 30;
        }

        if (anime.getScore() != null) {
            score += Math.round(anime.getScore().floatValue());
        }

        return score;
    }

    private static boolean isHentai(AnimeDto dto) {
        if (dto.rating != null && dto.rating.contains("Hentai")) {
            return true;
        }

        if (dto.genres != null) {
            for (AnimeDto.NameItem g : dto.genres) {
                if ("Hentai".equalsIgnoreCase(g.name)) {
                    return true;
                }
            }
        }

        if (dto.explicit_genres != null) {
            for (AnimeDto.NameItem g : dto.explicit_genres) {
                if ("Hentai".equalsIgnoreCase(g.name)) {
                    return true;
                }
            }
        }

        return false;
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
                ? dto.images.jpg.image_url
                : "";

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

    private static boolean isExactTitleMatch(AnimeDto dto, String query) {
        if (dto == null || query == null) return false;

        String q = normalize(query);

        if (normalize(dto.title).equals(q)) return true;
        if (dto.title_english != null && normalize(dto.title_english).equals(q)) return true;
        if (dto.title_japanese != null && normalize(dto.title_japanese).equals(q)) return true;

        return false;
    }

    private static boolean looseContains(String source, String query) {
        String left = normalize(source);
        String right = normalize(query);

        if (right.isBlank()) return true;
        if (left.contains(right)) return true;

        return allTokensPresent(left, right);
    }

    private static boolean allTokensPresent(String source, String query) {
        if (query == null || query.isBlank()) return true;

        String[] tokens = normalize(query).split("\\s+");
        for (String token : tokens) {
            if (token.isBlank()) continue;
            if (!source.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private static List<String> safeStrings(List<String> list) {
        return list == null ? List.of() : list;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim()
                .toLowerCase(Locale.ROOT)
                .replace("’", "'")
                .replace("`", "'")
                .replaceAll("\\s+", " ");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String formatDecimal(Double value) {
        if (value == null) return "";
        if (Math.floor(value) == value) {
            return String.valueOf(value.intValue());
        }
        return String.format(Locale.US, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
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
            Thread.sleep(1500);
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        }

        return response;
    }

    private static class AnimeDetailsResponse {
        public AnimeDto data;
    }

    private static class GenreResponse {
        public List<AnimeDto.NameItem> data;
    }

    private static class AdvancedSearchOptions {
        private final String title;
        private final String studio;
        private final String type;
        private final String season;
        private final Integer year;
        private final Double minScore;
        private final List<String> selectedGenres;

        private AdvancedSearchOptions(String title,
                                      String studio,
                                      String type,
                                      String season,
                                      Integer year,
                                      Double minScore,
                                      List<String> selectedGenres) {
            this.title = title == null ? "" : title.trim();
            this.studio = studio == null ? "" : studio.trim();
            this.type = normalizeType(type);
            this.season = normalizeSeason(season);
            this.year = year;
            this.minScore = minScore;
            this.selectedGenres = selectedGenres == null ? List.of() : selectedGenres.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .toList();
        }

        private boolean hasSeason() {
            return season != null && !season.isBlank();
        }

        private static String normalizeType(String value) {
            if (value == null || value.isBlank() || "Any".equalsIgnoreCase(value)) {
                return "";
            }
            return value.trim();
        }

        private static String normalizeSeason(String value) {
            if (value == null || value.isBlank() || "Any".equalsIgnoreCase(value)) {
                return "";
            }
            return value.trim().toLowerCase(Locale.ROOT);
        }
    }
}