package com.example.css_javafx.theme;

public enum AppTheme {
    DARK("Dark", "/com/example/css_javafx/themes/theme-dark.css"),
    LIGHT("Light", "/com/example/css_javafx/themes/theme-light.css"),
    SAKURA("Sakura", "/com/example/css_javafx/themes/theme-sakura.css"),
    FOREST("Forest", "/com/example/css_javafx/themes/theme-forest.css"),
    COW("Cow", "/com/example/css_javafx/themes/theme-cow.css"),
    OCEAN("Ocean", "/com/example/css_javafx/themes/theme-ocean.css"),
    WIN95("Windows 95", "/com/example/css_javafx/themes/theme-win95.css"),
    FXML_DARK("Dark (FXML)", "", "/com/example/css_javafx/main-dark-fxml.fxml", true),
    CUSTOM("Custom", "");

    private final String displayName;
    private final String cssPath;
    private final String mainFxmlPath;
    private final boolean fxmlTheme;

    AppTheme(String displayName, String cssPath) {
        this(displayName, cssPath, "/com/example/css_javafx/main.fxml", false);
    }

    AppTheme(String displayName, String cssPath, String mainFxmlPath, boolean fxmlTheme) {
        this.displayName = displayName;
        this.cssPath = cssPath;
        this.mainFxmlPath = mainFxmlPath;
        this.fxmlTheme = fxmlTheme;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssPath() {
        return cssPath;
    }

    public String getMainFxmlPath() {
        return mainFxmlPath;
    }

    public boolean isFxmlTheme() {
        return fxmlTheme;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static AppTheme fromName(String name) {
        if (name == null || name.isBlank()) return DARK;

        for (AppTheme theme : values()) {
            if (theme.name().equalsIgnoreCase(name)) {
                return theme;
            }
        }
        return DARK;
    }
}