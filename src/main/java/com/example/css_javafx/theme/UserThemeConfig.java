package com.example.css_javafx.theme;

public class UserThemeConfig {

    private final String backgroundColor;
    private final String windowBarColor;
    private final String topbarColor;
    private final String sidebarColor;
    private final String surfaceColor;
    private final String primaryColor;
    private final String textColor;
    private final String mutedTextColor;
    private final String borderColor;
    private final String hoverColor;

    private final String backgroundImageUri;
    private final String fontFamily;
    private final int fontSize;

    public UserThemeConfig(String backgroundColor,
                           String windowBarColor,
                           String topbarColor,
                           String sidebarColor,
                           String surfaceColor,
                           String primaryColor,
                           String textColor,
                           String mutedTextColor,
                           String borderColor,
                           String hoverColor,
                           String backgroundImageUri,
                           String fontFamily,
                           int fontSize) {
        this.backgroundColor = backgroundColor;
        this.windowBarColor = windowBarColor;
        this.topbarColor = topbarColor;
        this.sidebarColor = sidebarColor;
        this.surfaceColor = surfaceColor;
        this.primaryColor = primaryColor;
        this.textColor = textColor;
        this.mutedTextColor = mutedTextColor;
        this.borderColor = borderColor;
        this.hoverColor = hoverColor;
        this.backgroundImageUri = backgroundImageUri;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getWindowBarColor() {
        return windowBarColor;
    }

    public String getTopbarColor() {
        return topbarColor;
    }

    public String getSidebarColor() {
        return sidebarColor;
    }

    public String getSurfaceColor() {
        return surfaceColor;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public String getMutedTextColor() {
        return mutedTextColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public String getHoverColor() {
        return hoverColor;
    }

    public String getBackgroundImageUri() {
        return backgroundImageUri;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public int getFontSize() {
        return fontSize;
    }

    public static UserThemeConfig defaultConfig() {
        return new UserThemeConfig(
                "#161A2B",
                "#1B2035",
                "#202742",
                "#1A2139",
                "#232C4A",
                "#6C5CE7",
                "#F7F9FF",
                "#AAB4D2",
                "#44507A",
                "#7C6CFF",
                "",
                "System",
                14
        );
    }
}