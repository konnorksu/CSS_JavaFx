package com.example.css_javafx.theme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserThemeConfigTest {

    @Test
    void defaultConfig_shouldReturnNonNullObject() {
        UserThemeConfig config = UserThemeConfig.defaultConfig();

        assertNotNull(config);
    }

    @Test
    void defaultConfig_shouldContainExpectedDefaultValues() {
        UserThemeConfig config = UserThemeConfig.defaultConfig();

        assertEquals("#161A2B", config.getBackgroundColor());
        assertEquals("#1B2035", config.getWindowBarColor());
        assertEquals("#202742", config.getTopbarColor());
        assertEquals("#1A2139", config.getSidebarColor());
        assertEquals("#232C4A", config.getSurfaceColor());
        assertEquals("#6C5CE7", config.getPrimaryColor());
        assertEquals("#F7F9FF", config.getTextColor());
        assertEquals("#AAB4D2", config.getMutedTextColor());
        assertEquals("#44507A", config.getBorderColor());
        assertEquals("#7C6CFF", config.getHoverColor());
        assertEquals("", config.getBackgroundImageUri());
        assertEquals("System", config.getFontFamily());
        assertEquals(14, config.getFontSize());
    }

    @Test
    void getters_shouldReturnValuesPassedToConstructor() {
        UserThemeConfig config = new UserThemeConfig(
                "#111111",
                "#222222",
                "#333333",
                "#444444",
                "#555555",
                "#666666",
                "#777777",
                "#888888",
                "#999999",
                "#AAAAAA",
                "file:///background.png",
                "Arial",
                18
        );

        assertEquals("#111111", config.getBackgroundColor());
        assertEquals("#222222", config.getWindowBarColor());
        assertEquals("#333333", config.getTopbarColor());
        assertEquals("#444444", config.getSidebarColor());
        assertEquals("#555555", config.getSurfaceColor());
        assertEquals("#666666", config.getPrimaryColor());
        assertEquals("#777777", config.getTextColor());
        assertEquals("#888888", config.getMutedTextColor());
        assertEquals("#999999", config.getBorderColor());
        assertEquals("#AAAAAA", config.getHoverColor());
        assertEquals("file:///background.png", config.getBackgroundImageUri());
        assertEquals("Arial", config.getFontFamily());
        assertEquals(18, config.getFontSize());
    }
}