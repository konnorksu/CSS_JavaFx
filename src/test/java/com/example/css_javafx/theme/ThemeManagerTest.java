package com.example.css_javafx.theme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThemeManagerTest {

    @BeforeEach
    void clearPreferences() throws Exception {
        Preferences.userNodeForPackage(ThemeManager.class).clear();
    }

    @Test
    void loadSavedTheme_shouldReturnDarkByDefault() {
        AppTheme result = ThemeManager.loadSavedTheme();

        assertEquals(AppTheme.DARK, result);
    }

    @Test
    void saveTheme_shouldPersistSelectedTheme() {
        ThemeManager.saveTheme(AppTheme.OCEAN);

        AppTheme result = ThemeManager.loadSavedTheme();

        assertEquals(AppTheme.OCEAN, result);
    }

    @Test
    void saveCustomTheme_shouldPersistCustomConfig() {
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

        ThemeManager.saveCustomTheme(config);
        UserThemeConfig loaded = ThemeManager.loadCustomTheme();

        assertEquals("#111111", loaded.getBackgroundColor());
        assertEquals("#222222", loaded.getWindowBarColor());
        assertEquals("#333333", loaded.getTopbarColor());
        assertEquals("#444444", loaded.getSidebarColor());
        assertEquals("#555555", loaded.getSurfaceColor());
        assertEquals("#666666", loaded.getPrimaryColor());
        assertEquals("#777777", loaded.getTextColor());
        assertEquals("#888888", loaded.getMutedTextColor());
        assertEquals("#999999", loaded.getBorderColor());
        assertEquals("#AAAAAA", loaded.getHoverColor());
        assertEquals("file:///background.png", loaded.getBackgroundImageUri());
        assertEquals("Arial", loaded.getFontFamily());
        assertEquals(18, loaded.getFontSize());
    }

    @Test
    void resetCustomTheme_shouldRestoreDefaultConfig() {
        UserThemeConfig custom = new UserThemeConfig(
                "#AAAAAA",
                "#BBBBBB",
                "#CCCCCC",
                "#DDDDDD",
                "#EEEEEE",
                "#999999",
                "#123456",
                "#654321",
                "#111111",
                "#222222",
                "file:///custom.jpg",
                "Verdana",
                20
        );

        ThemeManager.saveCustomTheme(custom);

        ThemeManager.resetCustomTheme();
        UserThemeConfig loaded = ThemeManager.loadCustomTheme();
        UserThemeConfig defaults = UserThemeConfig.defaultConfig();

        assertEquals(defaults.getBackgroundColor(), loaded.getBackgroundColor());
        assertEquals(defaults.getWindowBarColor(), loaded.getWindowBarColor());
        assertEquals(defaults.getTopbarColor(), loaded.getTopbarColor());
        assertEquals(defaults.getSidebarColor(), loaded.getSidebarColor());
        assertEquals(defaults.getSurfaceColor(), loaded.getSurfaceColor());
        assertEquals(defaults.getPrimaryColor(), loaded.getPrimaryColor());
        assertEquals(defaults.getTextColor(), loaded.getTextColor());
        assertEquals(defaults.getMutedTextColor(), loaded.getMutedTextColor());
        assertEquals(defaults.getBorderColor(), loaded.getBorderColor());
        assertEquals(defaults.getHoverColor(), loaded.getHoverColor());
        assertEquals(defaults.getBackgroundImageUri(), loaded.getBackgroundImageUri());
        assertEquals(defaults.getFontFamily(), loaded.getFontFamily());
        assertEquals(defaults.getFontSize(), loaded.getFontSize());
    }
}