package com.example.css_javafx.theme;

import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.prefs.Preferences;

public class ThemeManager {

    private static final String BASE_CSS = "/com/example/css_javafx/themes/base.css";
    private static final String BASE_WIN95_CSS = "/com/example/css_javafx/themes/base-win95.css";
    private static final String PREF_KEY = "selected_theme";

    private static final String PREF_BG = "custom_bg";
    private static final String PREF_WINDOW = "custom_window";
    private static final String PREF_TOPBAR = "custom_topbar";
    private static final String PREF_SIDEBAR = "custom_sidebar";
    private static final String PREF_SURFACE = "custom_surface";
    private static final String PREF_PRIMARY = "custom_primary";
    private static final String PREF_TEXT = "custom_text";
    private static final String PREF_MUTED = "custom_muted";
    private static final String PREF_BORDER = "custom_border";
    private static final String PREF_HOVER = "custom_hover";
    private static final String PREF_BG_IMAGE = "custom_bg_image";
    private static final String PREF_FONT = "custom_font";
    private static final String PREF_FONT_SIZE = "custom_font_size";

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(ThemeManager.class);

    private ThemeManager() {
    }

    public static void applyTheme(Scene scene, AppTheme theme) {
        if (scene == null || theme == null) {
            return;
        }

        scene.getStylesheets().clear();

        String basePath = theme == AppTheme.WIN95 ? BASE_WIN95_CSS : BASE_CSS;
        String base = ThemeManager.class.getResource(basePath).toExternalForm();
        scene.getStylesheets().add(base);

        if (theme == AppTheme.CUSTOM) {
            scene.getStylesheets().add(getCustomThemeCssUri());
            return;
        }

        if (theme.getCssPath() != null && !theme.getCssPath().isBlank()) {
            String themeCss = ThemeManager.class.getResource(theme.getCssPath()).toExternalForm();
            scene.getStylesheets().add(themeCss);
        }
    }

    public static void previewCustomTheme(Scene scene, UserThemeConfig config) {
        if (scene == null || config == null) {
            return;
        }

        Path previewCss = writePreviewThemeCss(config);

        scene.getStylesheets().clear();
        scene.getStylesheets().add(ThemeManager.class.getResource(BASE_CSS).toExternalForm());
        scene.getStylesheets().add(toVersionedUri(previewCss));

        if (scene.getRoot() != null) {
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        }
    }

    private static Path writePreviewThemeCss(UserThemeConfig config) {
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".css_javafx");
            Files.createDirectories(dir);

            Path cssFile = dir.resolve("user-theme-preview-" + System.currentTimeMillis() + ".css");
            Files.writeString(
                    cssFile,
                    buildCustomCss(config),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return cssFile;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create preview theme", e);
        }
    }

    public static void saveTheme(AppTheme theme) {
        if (theme == null) {
            return;
        }
        PREFS.put(PREF_KEY, theme.name());
    }

    public static AppTheme loadSavedTheme() {
        String saved = PREFS.get(PREF_KEY, AppTheme.DARK.name());
        return AppTheme.fromName(saved);
    }

    public static void saveCustomTheme(UserThemeConfig config) {
        if (config == null) {
            return;
        }

        PREFS.put(PREF_BG, nullToEmpty(config.getBackgroundColor()));
        PREFS.put(PREF_WINDOW, nullToEmpty(config.getWindowBarColor()));
        PREFS.put(PREF_TOPBAR, nullToEmpty(config.getTopbarColor()));
        PREFS.put(PREF_SIDEBAR, nullToEmpty(config.getSidebarColor()));
        PREFS.put(PREF_SURFACE, nullToEmpty(config.getSurfaceColor()));
        PREFS.put(PREF_PRIMARY, nullToEmpty(config.getPrimaryColor()));
        PREFS.put(PREF_TEXT, nullToEmpty(config.getTextColor()));
        PREFS.put(PREF_MUTED, nullToEmpty(config.getMutedTextColor()));
        PREFS.put(PREF_BORDER, nullToEmpty(config.getBorderColor()));
        PREFS.put(PREF_HOVER, nullToEmpty(config.getHoverColor()));
        PREFS.put(PREF_BG_IMAGE, nullToEmpty(config.getBackgroundImageUri()));
        PREFS.put(PREF_FONT, config.getFontFamily() == null || config.getFontFamily().isBlank()
                ? "System"
                : config.getFontFamily());
        PREFS.putInt(PREF_FONT_SIZE, config.getFontSize());

        writeCustomThemeCss(config);
    }

    public static UserThemeConfig loadCustomTheme() {
        UserThemeConfig defaults = UserThemeConfig.defaultConfig();

        return new UserThemeConfig(
                PREFS.get(PREF_BG, defaults.getBackgroundColor()),
                PREFS.get(PREF_WINDOW, defaults.getWindowBarColor()),
                PREFS.get(PREF_TOPBAR, defaults.getTopbarColor()),
                PREFS.get(PREF_SIDEBAR, defaults.getSidebarColor()),
                PREFS.get(PREF_SURFACE, defaults.getSurfaceColor()),
                PREFS.get(PREF_PRIMARY, defaults.getPrimaryColor()),
                PREFS.get(PREF_TEXT, defaults.getTextColor()),
                PREFS.get(PREF_MUTED, defaults.getMutedTextColor()),
                PREFS.get(PREF_BORDER, defaults.getBorderColor()),
                PREFS.get(PREF_HOVER, defaults.getHoverColor()),
                PREFS.get(PREF_BG_IMAGE, defaults.getBackgroundImageUri()),
                PREFS.get(PREF_FONT, defaults.getFontFamily()),
                PREFS.getInt(PREF_FONT_SIZE, defaults.getFontSize())
        );
    }

    public static void resetCustomTheme() {
        saveCustomTheme(UserThemeConfig.defaultConfig());
    }

    private static String getCustomThemeCssUri() {
        return toVersionedUri(writeCustomThemeCss(loadCustomTheme()));
    }

    private static Path writeCustomThemeCss(UserThemeConfig config) {
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".css_javafx");
            Files.createDirectories(dir);

            Path cssFile = dir.resolve("user-theme.css");
            Files.writeString(
                    cssFile,
                    buildCustomCss(config),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            return cssFile;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save custom theme", e);
        }
    }

    private static String buildCustomCss(UserThemeConfig config) {
        String bg = safeColor(config.getBackgroundColor(), "#161A2B");
        String window = safeColor(config.getWindowBarColor(), "#1B2035");
        String topbar = safeColor(config.getTopbarColor(), "#202742");
        String sidebar = safeColor(config.getSidebarColor(), "#1A2139");
        String surface = safeColor(config.getSurfaceColor(), "#232C4A");
        String primary = safeColor(config.getPrimaryColor(), "#6C5CE7");
        String text = safeColor(config.getTextColor(), "#F7F9FF");
        String muted = safeColor(config.getMutedTextColor(), "#AAB4D2");
        String border = safeColor(config.getBorderColor(), "#44507A");
        String hover = safeColor(config.getHoverColor(), "#7C6CFF");

        String fontFamily = safeFont(config.getFontFamily(), "System");
        int fontSize = safeFontSize(config.getFontSize(), 14);

        String bgImageUri = config.getBackgroundImageUri() == null
                ? ""
                : config.getBackgroundImageUri().trim();

        StringBuilder appRootBlock = new StringBuilder();
        appRootBlock.append(".app-root {\n");
        appRootBlock.append("    -fx-background-color: ").append(bg).append(";\n");
        appRootBlock.append("    -fx-font-family: \"").append(fontFamily).append("\";\n");
        appRootBlock.append("    -fx-font-size: ").append(fontSize).append("px;\n");

        if (!bgImageUri.isBlank()) {
            appRootBlock.append("    -fx-background-image: url(\"")
                    .append(escapeCssUrl(bgImageUri))
                    .append("\");\n");
            appRootBlock.append("    -fx-background-size: cover;\n");
            appRootBlock.append("    -fx-background-repeat: no-repeat;\n");
            appRootBlock.append("    -fx-background-position: center center;\n");
        }

        appRootBlock.append("}\n");

        String faint = rgba(text, 0.07);
        String footer = linearGradient(surface);
        String softButton = rgba(text, 0.08);
        String scrollTrack = rgba(surface, 0.85);

        return String.join("\n",
                appRootBlock.toString(),

                ".window-bar {",
                "    -fx-background-color: " + window + ";",
                "    -fx-border-color: " + border + ";",
                "    -fx-border-width: 0 0 1 0;",
                "}",

                ".window-title,",
                ".brand,",
                ".section-title,",
                ".details-title,",
                ".empty-title,",
                ".stat-value,",
                ".card-title {",
                "    -fx-text-fill: " + text + ";",
                "}",

                ".window-btn {",
                "    -fx-background-color: " + softButton + ";",
                "    -fx-text-fill: " + text + ";",
                "    -fx-background-radius: 8;",
                "    -fx-border-radius: 8;",
                "    -fx-border-color: " + border + ";",
                "}",

                ".window-btn:hover {",
                "    -fx-background-color: " + hover + ";",
                "}",

                ".window-btn-close:hover {",
                "    -fx-background-color: #E74C3C;",
                "    -fx-text-fill: white;",
                "}",

                ".topbar {",
                "    -fx-background-color: " + topbar + ";",
                "    -fx-border-color: " + border + ";",
                "}",

                ".sidebar {",
                "    -fx-background-color: " + sidebar + ";",
                "    -fx-border-color: " + border + ";",
                "}",

                ".sidebar-title,",
                ".status,",
                ".empty-desc,",
                ".stat-label,",
                ".card-hint,",
                ".custom-theme-label {",
                "    -fx-text-fill: " + muted + ";",
                "}",

                ".search,",
                ".details-desc,",
                ".stat-card,",
                ".chip,",
                ".custom-theme-panel,",
                ".text-field,",
                ".spinner {",
                "    -fx-background-color: " + surface + ";",
                "    -fx-border-color: " + border + ";",
                "    -fx-text-fill: " + text + ";",
                "}",

                ".search {",
                "    -fx-prompt-text-fill: " + muted + ";",
                "}",

                ".nav-btn {",
                "    -fx-background-color: transparent;",
                "    -fx-text-fill: " + muted + ";",
                "}",

                ".nav-btn:hover {",
                "    -fx-background-color: " + faint + ";",
                "}",

                ".nav-btn-active {",
                "    -fx-background-color: " + hover + ";",
                "    -fx-text-fill: " + text + ";",
                "}",

                ".btn-primary {",
                "    -fx-background-color: " + primary + ";",
                "    -fx-text-fill: white;",
                "}",

                ".btn-ghost {",
                "    -fx-background-color: " + faint + ";",
                "    -fx-text-fill: " + text + ";",
                "    -fx-border-color: " + border + ";",
                "}",

                ".card-footer {",
                "    -fx-background-color: " + footer + ";",
                "}",

                ".loading {",
                "    -fx-progress-color: " + primary + ";",
                "}",

                ".chip {",
                "    -fx-text-fill: " + text + ";",
                "}",

                ".scroll-pane {",
                "    -fx-background-color: transparent;",
                "    -fx-border-color: transparent;",
                "}",

                ".scroll-pane > .viewport {",
                "    -fx-background-color: transparent;",
                "}",

                ".scroll-pane .track,",
                ".scroll-pane .increment-button,",
                ".scroll-pane .decrement-button {",
                "    -fx-background-color: " + scrollTrack + ";",
                "}",

                ".scroll-pane .thumb {",
                "    -fx-background-color: " + primary + ";",
                "}",

                ".scroll-bar:vertical,",
                ".scroll-bar:horizontal {",
                "    -fx-background-color: transparent;",
                "}",

                ".combo-box,",
                ".color-picker,",
                ".text-field,",
                ".spinner {",
                "    -fx-background-color: " + surface + ";",
                "    -fx-border-color: " + border + ";",
                "    -fx-border-radius: 12;",
                "    -fx-background-radius: 12;",
                "}",

                ".combo-box .list-cell,",
                ".color-picker .label,",
                ".text-field,",
                ".spinner .text-field {",
                "    -fx-text-fill: " + text + ";",
                "    -fx-background-color: transparent;",
                "}",

                ".combo-box-popup .list-view {",
                "    -fx-background-color: " + surface + ";",
                "    -fx-border-color: " + border + ";",
                "}",

                ".combo-box-popup .list-cell {",
                "    -fx-background-color: " + surface + ";",
                "    -fx-text-fill: " + text + ";",
                "}",

                ".combo-box-popup .list-cell:hover,",
                ".combo-box-popup .list-cell:selected {",
                "    -fx-background-color: " + hover + ";",
                "    -fx-text-fill: " + text + ";",
                "}",

                ".custom-theme-panel {",
                "    -fx-padding: 12;",
                "    -fx-background-radius: 16;",
                "    -fx-border-radius: 16;",
                "}",

                ""
        );
    }

    private static String safeColor(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        String trimmed = value.trim();

        try {
            Color.web(trimmed);
            return trimmed;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String safeFont(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.replace("\"", "").trim();
    }

    private static int safeFontSize(int value, int fallback) {
        if (value < 8 || value > 40) {
            return fallback;
        }
        return value;
    }

    private static String escapeCssUrl(String value) {
        return value.replace("\"", "%22");
    }

    private static String rgba(String cssColor, double alpha) {
        Color color = parseColor(cssColor, Color.WHITE);
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format(Locale.US, "rgba(%d,%d,%d,%.3f)", r, g, b, alpha);
    }

    private static String linearGradient(String cssColor) {
        Color color = parseColor(cssColor, Color.web("#232C4A"));
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);

        return String.format(Locale.US,
                "linear-gradient(to top, rgba(%d,%d,%d,0.92), rgba(%d,%d,%d,0.18))",
                r, g, b, r, g, b
        );
    }

    private static Color parseColor(String cssColor, Color fallback) {
        try {
            return Color.web(cssColor);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String toVersionedUri(Path path) {
        return path.toUri().toString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}