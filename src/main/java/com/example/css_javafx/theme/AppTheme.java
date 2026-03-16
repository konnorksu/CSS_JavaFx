package com.example.css_javafx.theme;

public enum AppTheme {
    DARK("Dark", "/com/example/css_javafx/themes/theme-dark.css"),
    LIGHT("Light", "/com/example/css_javafx/themes/theme-light.css"),
    SAKURA("Sakura", "/com/example/css_javafx/themes/theme-sakura.css"),
    FOREST("Forest", "/com/example/css_javafx/themes/theme-forest.css"),
    COW("Cow", "/com/example/css_javafx/themes/theme-cow.css"),
    OCEAN("Ocean", "/com/example/css_javafx/themes/theme-ocean.css"),
    WIN95("Windows 95", "/com/example/css_javafx/themes/theme-win95.css"),
    CUSTOM("Custom", "");

    private final String displayName;
    private final String cssPath;

    AppTheme(String displayName, String cssPath) {
        this.displayName = displayName;
        this.cssPath = cssPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssPath() {
        return cssPath;
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