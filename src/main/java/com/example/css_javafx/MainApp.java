package com.example.css_javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("main.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 760);
        scene.getStylesheets().add(MainApp.class.getResource("/style.css").toExternalForm());

        stage.setTitle("Anime Catalog FX");
        stage.setScene(scene);

        // HostServices отдаём контроллеру, чтобы он мог открывать ссылки
        Object c = loader.getController();
        if (c instanceof com.example.css_javafx.controller.MainController mc) {
            mc.setHostServices(getHostServices());
        }

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}