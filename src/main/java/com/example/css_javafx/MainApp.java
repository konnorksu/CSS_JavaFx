package com.example.css_javafx;

import com.example.css_javafx.controller.MainController;
import com.example.css_javafx.theme.AppTheme;
import com.example.css_javafx.theme.ThemeManager;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private static Scene mainScene;
    private static Stage primaryStage;
    private static HostServices appHostServices;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        appHostServices = getHostServices();

        AppTheme savedTheme = ThemeManager.loadSavedTheme();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(savedTheme.getMainFxmlPath())
        );
        Parent root = loader.load();

        mainScene = new Scene(root, 1280, 800);

        MainController controller = loader.getController();
        controller.setHostServices(appHostServices);
        controller.setStage(stage);

        ThemeManager.saveTheme(savedTheme);
        controller.setScene(mainScene);

        if (savedTheme.isFxmlTheme()) {
            ThemeManager.applyBaseOnly(mainScene);
        } else {
            ThemeManager.applyTheme(mainScene, savedTheme);
        }

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Anime Catalog");
        stage.setScene(mainScene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void switchTheme(AppTheme theme) {
        if (theme == null || mainScene == null || primaryStage == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource(theme.getMainFxmlPath())
            );
            Parent root = loader.load();

            ThemeManager.saveTheme(theme);
            mainScene.setRoot(root);

            MainController controller = loader.getController();
            controller.setHostServices(appHostServices);
            controller.setStage(primaryStage);
            controller.setScene(mainScene);

            if (theme.isFxmlTheme()) {
                ThemeManager.applyBaseOnly(mainScene);
            } else {
                ThemeManager.applyTheme(mainScene, theme);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}