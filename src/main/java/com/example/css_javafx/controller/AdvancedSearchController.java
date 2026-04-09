package com.example.css_javafx.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class AdvancedSearchController {

    private static final List<String> COMMON_GENRES = List.of(
            "Action", "Adventure", "Avant Garde", "Award Winning", "Boys Love",
            "Comedy", "Drama", "Fantasy", "Girls Love", "Gourmet",
            "Horror", "Mystery", "Romance", "Sci-Fi", "Slice of Life",
            "Sports", "Supernatural", "Suspense", "Ecchi"
    );

    @FXML private TextField titleField;
    @FXML private TextField studioField;
    @FXML private ComboBox<String> typeBox;
    @FXML private ComboBox<String> seasonBox;
    @FXML private ComboBox<String> yearBox;
    @FXML private TextField minScoreField;
    @FXML private ListView<String> genreList;
    @FXML private Label infoLabel;

    private MainController mainController;

    public void init(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        typeBox.getItems().addAll("Any", "TV", "Movie", "OVA", "ONA", "Special", "Music");
        typeBox.setValue("Any");
        typeBox.getStyleClass().add("combo-box-popup");

        seasonBox.getItems().addAll("Any", "winter", "spring", "summer", "fall");
        seasonBox.setValue("Any");
        seasonBox.getStyleClass().add("combo-box-popup");

        yearBox.getItems().add("Any");
        for (int year = Year.now().getValue() + 1; year >= 1980; year--) {
            yearBox.getItems().add(String.valueOf(year));
        }
        yearBox.setValue("Any");
        yearBox.getStyleClass().add("combo-box-popup");

        genreList.setItems(FXCollections.observableArrayList(COMMON_GENRES));
        genreList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        genreList.getStyleClass().add("genre-list");

        infoLabel.setText("Select multiple genres with Ctrl / Shift");
    }

    @FXML
    private void onApply() {
        String title = safeTrim(titleField.getText());
        String studio = safeTrim(studioField.getText());
        String type = typeBox.getValue();
        String season = seasonBox.getValue();
        Integer year = "Any".equals(yearBox.getValue()) ? null : parseInteger(yearBox.getValue());
        Double minScore = parseDouble(minScoreField.getText());
        List<String> genres = new ArrayList<>(genreList.getSelectionModel().getSelectedItems());

        boolean empty = title.isBlank()
                && studio.isBlank()
                && "Any".equalsIgnoreCase(type)
                && "Any".equalsIgnoreCase(season)
                && year == null
                && minScore == null
                && genres.isEmpty();

        if (empty) {
            infoLabel.setText("Enter at least one search criterion.");
            return;
        }

        if (minScore != null && (minScore < 0 || minScore > 10)) {
            infoLabel.setText("Min score must be between 0 and 10.");
            return;
        }

        if (mainController != null) {
            mainController.searchAnimeAdvanced(
                    title,
                    studio,
                    type,
                    season,
                    year,
                    minScore,
                    genres
            );
        }
    }

    @FXML
    private void onReset() {
        titleField.clear();
        studioField.clear();
        typeBox.setValue("Any");
        seasonBox.setValue("Any");
        yearBox.setValue("Any");
        minScoreField.clear();
        genreList.getSelectionModel().clearSelection();
        infoLabel.setText("Filters reset");
    }

    @FXML
    private void onBack() {
        if (mainController != null) {
            mainController.goBackToCatalogFromChild();
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private Integer parseInteger(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            return Double.parseDouble(value.trim().replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }
}