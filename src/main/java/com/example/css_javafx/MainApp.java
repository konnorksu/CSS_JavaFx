package com.example.css_javafx;

import com.example.css_javafx.controller.MainController;
import com.example.css_javafx.theme.AppTheme;
import com.example.css_javafx.theme.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private static Scene mainScene;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/css_javafx/main.fxml"));
        Parent root = loader.load();

        mainScene = new Scene(root, 1280, 800);

        MainController controller = loader.getController();
        controller.setHostServices(getHostServices());
        controller.setScene(mainScene);
        controller.setStage(stage);   // важно

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Anime Catalog");
        stage.setScene(mainScene);
        stage.setMaximized(true);
        stage.show();
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}