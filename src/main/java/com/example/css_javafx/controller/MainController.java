package com.example.css_javafx.controller;

import com.example.css_javafx.AnimeService;
import com.example.css_javafx.model.Anime;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML private BorderPane root;

    @FXML private TextField searchField;
    @FXML private Label statusText;

    @FXML private ScrollPane scroll;
    @FXML private FlowPane cards;
    @FXML private ProgressIndicator loading;

    private HostServices hostServices;

    // нужен чтобы контроллер деталей мог открыть ссылку
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    // сохраняем исходный каталог-центр, чтобы потом вернуться из Details
    private Parent catalogCenterSnapshot;

    @FXML
    private void initialize() {
        // запомним каталог при старте
        catalogCenterSnapshot = (Parent) root.getCenter();
        loadAnime();
    }

    @FXML
    private void onSearch() {
        // как и раньше: пока просто перезагрузка
        loadAnime();
    }

    private void loadAnime() {
        setLoading(true);
        statusText.setText("Load...");

        new Thread(() -> {
            try {
                List<Anime> list = AnimeService.loadAnime();
                Platform.runLater(() -> {
                    showCards(list);
                    statusText.setText("Detected: " + (list == null ? 0 : list.size()));
                    setLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusText.setText("Error");
                    setLoading(false);
                    showError(e.getMessage());
                });
            }
        }, "anime-loader").start();
    }

    private void setLoading(boolean value) {
        loading.setVisible(value);
        scroll.setDisable(value);
        scroll.setOpacity(value ? 0.65 : 1.0);
    }

    private void showCards(List<Anime> list) {
        cards.getChildren().clear();

        if (list == null || list.isEmpty()) {
            cards.getChildren().add(makeEmptyState());
            return;
        }

        for (Anime anime : list) {
            cards.getChildren().add(makeCard(anime));
        }
    }

    private Parent makeCard(Anime anime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/css_javafx/anime_card.fxml"));
            Parent node = loader.load();

            AnimeCardController controller = loader.getController();
            controller.setAnime(anime);
            controller.setOnClick(() -> showDetails(anime));

            return node;
        } catch (IOException e) {
            Label err = new Label("Card load error");
            err.getStyleClass().add("card");
            return err;
        }
    }

    private Parent makeEmptyState() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/css_javafx/empty_state.fxml"));
            return loader.load();
        } catch (IOException e) {
            return new Label("No found");
        }
    }

    private void showDetails(Anime anime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/css_javafx/details.fxml"));
            Parent detailsNode = loader.load();

            DetailsController controller = loader.getController();

            controller.setAnime(anime, null, hostServices, this::goBackToCatalog);

            root.setCenter(detailsNode);
        } catch (IOException e) {
            showError("Unable to open details: " + e.getMessage());
        }
    }

    private void goBackToCatalog() {
        root.setCenter(catalogCenterSnapshot);
    }

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Failed to load data");
        a.setContentText(message);
        a.showAndWait();
    }
}