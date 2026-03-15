package com.example.css_javafx.theme;

import javafx.scene.Scene;

import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String BASE_CSS = "/com/example/css_javafx/themes/base.css";
    private static final String PREF_KEY = "selected_theme";

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(ThemeManager.class);

    private ThemeManager() {
    }

    public static void applyTheme(Scene scene, AppTheme theme) {
        if (scene == null || theme == null) return;

        scene.getStylesheets().clear();

        String basePath = "/com/example/css_javafx/themes/base.css";
        if (theme == AppTheme.WIN95) {
            basePath = "/com/example/css_javafx/themes/base-win95.css";
        }

        String base = ThemeManager.class.getResource(basePath).toExternalForm();
        String themeCss = ThemeManager.class.getResource(theme.getCssPath()).toExternalForm();

        scene.getStylesheets().add(base);
        scene.getStylesheets().add(themeCss);
    }

    public static void saveTheme(AppTheme theme) {
        if (theme == null) return;
        PREFS.put(PREF_KEY, theme.name());
    }

    public static AppTheme loadSavedTheme() {
        String saved = PREFS.get(PREF_KEY, AppTheme.DARK.name());
        return AppTheme.fromName(saved);
    }
}