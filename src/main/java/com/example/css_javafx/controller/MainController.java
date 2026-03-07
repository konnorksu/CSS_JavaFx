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

    private int currentPage = 1;
    private boolean isLoadingMore = false;
    private boolean hasMore = true;
    private String currentQuery = "";

    private HostServices hostServices;
    private final java.util.Set<Integer> loadedAnimeIds = new java.util.HashSet<>();

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    private Parent catalogCenterSnapshot;

    @FXML
    private void initialize() {
        // запомним каталог при старте
        catalogCenterSnapshot = (Parent) root.getCenter();
        scroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 0.9) {
                loadMoreIfNeeded();
            }
        });
        loadAnime();
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            loadAnime();
        } else {
            searchAnime(query.trim());
        }
    }

    private void loadAnime() {
        setLoading(true);
        statusText.setText("Loading...");
        currentPage = 1;
        currentQuery = "";
        hasMore = true;

        new Thread(() -> {
            try {
                List<Anime> list = AnimeService.loadAnime();
                Platform.runLater(() -> {
                    showCards(list);
                    statusText.setText("Detected: " + list.size());
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

    private void loadMoreIfNeeded() {
        if (isLoadingMore || !hasMore) return;

        isLoadingMore = true;
        currentPage++;

        new Thread(() -> {
            try {
                List<Anime> nextPage;

                if (currentQuery == null || currentQuery.isBlank()) {
                    nextPage = AnimeService.loadAnimePage(currentPage, 24);
                } else {
                    nextPage = AnimeService.searchAnimePage(currentQuery, currentPage, 24);
                }

                Platform.runLater(() -> {
                    if (nextPage == null || nextPage.isEmpty()) {
                        hasMore = false;
                    } else {
                        appendCards(nextPage);
                    }
                    isLoadingMore = false;
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.out.println("Load more failed: " + e.getMessage());
                    isLoadingMore = false;
                });
            }
        }, "anime-load-more").start();
    }

    private void searchAnime(String query) {
        setLoading(true);
        statusText.setText("Searching...");
        currentPage = 1;
        currentQuery = query;
        hasMore = true;

        new Thread(() -> {
            try {
                List<Anime> list = AnimeService.searchAnime(query, 24);

                Platform.runLater(() -> {
                    showCards(list);
                    statusText.setText("Found: " + list.size());
                    setLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusText.setText("Error");
                    setLoading(false);
                    showError(e.getMessage());
                });
            }
        }, "anime-search-loader").start();
    }

    private void setLoading(boolean value) {
        loading.setVisible(value);
        scroll.setDisable(value);
        scroll.setOpacity(value ? 0.65 : 1.0);
    }

    private void showCards(List<Anime> list) {
        cards.getChildren().clear();
        loadedAnimeIds.clear();

        if (list == null || list.isEmpty()) {
            cards.getChildren().add(makeEmptyState());
            return;
        }

        for (Anime anime : list) {
            if (loadedAnimeIds.add(anime.getMalId())) {
                cards.getChildren().add(makeCard(anime));
            }
        }
    }

    private void appendCards(List<Anime> list) {
        if (list == null || list.isEmpty()) return;

        for (Anime anime : list) {
            if (loadedAnimeIds.add(anime.getMalId())) {
                cards.getChildren().add(makeCard(anime));
            }
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

            String trailerUrl = anime.getTrailerUrl(); // только watch

            controller.setAnime(anime, trailerUrl, hostServices, this::goBackToCatalog);

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