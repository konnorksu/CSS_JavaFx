package com.example.css_javafx.controller;

import com.example.css_javafx.model.Anime;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class AnimeCardController {

    @FXML private StackPane cardRoot;
    @FXML private ImageView poster;
    @FXML private Label title;

    private Runnable onClick = () -> {};

    @FXML
    private void initialize() {
        cardRoot.setOnMouseClicked(e -> onClick.run());
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick == null ? () -> {} : onClick;
    }

    public void setAnime(Anime anime) {
        title.setText(anime.getTitle());
        String url = anime.getImageUrl();
        if (url != null && !url.isBlank()) {
            poster.setImage(new Image(url, 210, 310, false, true, true));
        } else {
            poster.setImage(null);
        }
    }
}

