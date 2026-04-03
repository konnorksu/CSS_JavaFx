package com.example.css_javafx.theme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppThemeTest {

    @Test
    void fromName_shouldReturnDark_whenNameIsNull() {
        AppTheme result = AppTheme.fromName(null);

        assertEquals(AppTheme.DARK, result);
    }

    @Test
    void fromName_shouldReturnDark_whenNameIsBlank() {
        AppTheme result = AppTheme.fromName("   ");

        assertEquals(AppTheme.DARK, result);
    }

    @Test
    void fromName_shouldReturnMatchingTheme_whenNameIsValid() {
        AppTheme result = AppTheme.fromName("LIGHT");

        assertEquals(AppTheme.LIGHT, result);
    }

    @Test
    void fromName_shouldIgnoreCase_whenNameMatches() {
        AppTheme result = AppTheme.fromName("sAkUrA");

        assertEquals(AppTheme.SAKURA, result);
    }

    @Test
    void fromName_shouldReturnDark_whenNameIsUnknown() {
        AppTheme result = AppTheme.fromName("UNKNOWN_THEME");

        assertEquals(AppTheme.DARK, result);
    }

    @Test
    void toString_shouldReturnDisplayName() {
        String result = AppTheme.WIN95.toString();

        assertEquals("Windows 95", result);
    }
}