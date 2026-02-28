package com.example.css_javafx.controller;

import com.example.css_javafx.model.Anime;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DetailsController {

    @FXML private Button backBtn;
    @FXML private ImageView image;
    @FXML private Label title;
    @FXML private TextArea desc;
    @FXML private Button openBtn;

    private Anime anime;
    private HostServices hostServices;
    private Runnable onBack = () -> {};
    private String trailerUrl;

    @FXML
    private void initialize() {
        backBtn.setOnAction(e -> onBack.run());

        openBtn.setOnAction(e -> {
            if (hostServices != null && anime != null && anime.getUrl() != null) {
                hostServices.showDocument(anime.getUrl());
            }
        });
    }

    public void setAnime(Anime anime, String trailerUrl,
                         HostServices hostServices,
                         Runnable onBack) {

        this.anime = anime;
        this.trailerUrl = trailerUrl;
        this.hostServices = hostServices;
        this.onBack = (onBack == null) ? () -> {} : onBack;

        title.setText(anime.getTitle());
        desc.setText(anime.getDescription());

        if (anime.getImageUrl() != null && !anime.getImageUrl().isBlank()) {
            image.setImage(new Image(anime.getImageUrl(), 260, 360, true, true, true));
        } else {
            image.setImage(null);
        }
    }

    @FXML
    private void onOpenTrailer() {
        if (hostServices == null || trailerUrl == null || trailerUrl.isBlank()) {
            System.out.println("No trailer URL");
            return;
        }

        hostServices.showDocument(trailerUrl);
    }
}