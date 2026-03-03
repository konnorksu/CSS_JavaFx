package com.example.css_javafx.controller;

import com.example.css_javafx.AnimeService;
import com.example.css_javafx.model.Anime;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

public class DetailsController {

    @FXML private Button backBtn;
    @FXML private Button openBtn;
    @FXML private Button openTrailerBtn;

    @FXML private ImageView image;
    @FXML private Label title;
    @FXML private Label metaLine;
    @FXML private Label scoreText;
    @FXML private Label votesText;

    @FXML private FlowPane genresPane;
    @FXML private FlowPane studiosPane;

    @FXML private Label desc;

    private Anime anime;
    private HostServices hostServices;
    private Runnable onBack = () -> {};
    private String trailerUrl;

    @FXML
    private void initialize() {
        backBtn.setOnAction(e -> onBack.run());

        openBtn.setOnAction(e -> {
            if (hostServices != null && anime != null) {
                String url = anime.getUrl();
                if (url != null && !url.isBlank()) {
                    hostServices.showDocument(url);
                }
            }
        });
    }

    public void setAnime(Anime anime, String trailerUrl, HostServices hostServices, Runnable onBack) {
        this.anime = anime;
        this.trailerUrl = trailerUrl;
        this.hostServices = hostServices;
        this.onBack = (onBack == null) ? () -> {} : onBack;

        // UI: basic text
        title.setText(safe(anime.getTitle()));
        desc.setText(safe(anime.getDescription()));

        // UI: image
        String imgUrl = anime.getImageUrl();
        if (imgUrl != null && !imgUrl.isBlank()) {
            image.setImage(new Image(imgUrl, 240, 340, true, true, true));
        } else {
            image.setImage(null);
        }

        // UI: meta
        String meta = String.format("%s • %s eps • %s • %s • %s",
                nz(anime.getType(), "—"),
                anime.getEpisodes() == null ? "—" : String.valueOf(anime.getEpisodes()),
                nz(anime.getDuration(), "—"),
                anime.getYear() == null ? "—" : String.valueOf(anime.getYear()),
                nz(anime.getRating(), "—")
        );
        metaLine.setText(meta);

        // UI: score/votes
        scoreText.setText(anime.getScore() == null ? "—" : String.format("%.2f", anime.getScore()));
        votesText.setText(anime.getScoredBy() == null ? "—" : String.valueOf(anime.getScoredBy()));

        // UI: chips
        fillChips(genresPane, anime.getGenres());
        fillChips(studiosPane, anime.getStudios());

        // Buttons
        openBtn.setDisable(anime.getUrl() == null || anime.getUrl().isBlank());
        openTrailerBtn.setDisable(trailerUrl == null || trailerUrl.isBlank());

        // If trailer missing -> try load from /anime/{id}
        if (this.trailerUrl == null || this.trailerUrl.isBlank()) {
            loadTrailerAsync(anime.getMalId());
        }

        System.out.println("DETAILS open: " + anime.getTitle() + " malId=" + anime.getMalId());
        System.out.println("DETAILS initial trailerUrl=" + this.trailerUrl);
    }

    private void loadTrailerAsync(int malId) {
        System.out.println("Loading trailer by malId=" + malId);

        new Thread(() -> {
            try {
                String watchUrl = AnimeService.loadTrailerWatchUrlByMalId(malId);
                System.out.println("DETAILS loaded watchUrl=" + watchUrl);

                if (watchUrl == null || watchUrl.isBlank()) return;

                Platform.runLater(() -> {
                    this.trailerUrl = watchUrl;
                    openTrailerBtn.setDisable(false);
                });
            } catch (Exception e) {
                System.out.println("Trailer load failed: " + e.getMessage());
            }
        }, "details-trailer-loader").start();
    }

    @FXML
    private void onOpenTrailer() {
        if (hostServices == null || trailerUrl == null || trailerUrl.isBlank()) {
            System.out.println("No trailer URL");
            return;
        }
        System.out.println("OPEN TRAILER URL = " + trailerUrl);
        hostServices.showDocument(trailerUrl);
    }

    private void fillChips(FlowPane pane, java.util.List<String> items) {
        pane.getChildren().clear();
        if (items == null || items.isEmpty()) {
            pane.getChildren().add(makeChip("—"));
            return;
        }
        for (String s : items) {
            if (s == null || s.isBlank()) continue;
            pane.getChildren().add(makeChip(s));
        }
    }

    private Label makeChip(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("chip");
        return l;
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String nz(String s, String fallback) { return (s == null || s.isBlank()) ? fallback : s; }
}