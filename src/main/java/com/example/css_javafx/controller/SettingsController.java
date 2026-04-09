package com.example.css_javafx.controller;

import com.example.css_javafx.theme.AppTheme;
import com.example.css_javafx.theme.ThemeManager;
import com.example.css_javafx.theme.UserThemeConfig;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Locale;

public class SettingsController {

    @FXML private ColorPicker borderColorPicker;
    @FXML private ColorPicker hoverColorPicker;

    @FXML private TextField backgroundImageField;
    @FXML private ComboBox<String> fontComboBox;
    @FXML private Spinner<Integer> fontSizeSpinner;

    @FXML private Button dialogCloseBtn;

    @FXML private ColorPicker bgColorPicker;
    @FXML private ColorPicker windowBarColorPicker;
    @FXML private ColorPicker topbarColorPicker;
    @FXML private ColorPicker sidebarColorPicker;
    @FXML private ColorPicker surfaceColorPicker;
    @FXML private ColorPicker primaryColorPicker;
    @FXML private ColorPicker textColorPicker;
    @FXML private ColorPicker mutedTextColorPicker;

    @FXML private Label dialogStatus;

    private Stage dialogStage;
    private Scene mainScene;
    private ComboBox<AppTheme> themeComboBox;
    private Label statusText;

    private AppTheme originalTheme;
    private boolean saved = false;
    private Runnable onBack;

    private String selectedBackgroundImageUri = "";

    @FXML
    private void initialize() {
        if (fontComboBox != null) {
            fontComboBox.getItems().setAll(Font.getFamilies());
        }

        if (fontSizeSpinner != null) {
            fontSizeSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 40, 14)
            );
            fontSizeSpinner.setEditable(true);
        }
    }

    public void init(Stage dialogStage,
                     Scene mainScene,
                     ComboBox<AppTheme> themeComboBox,
                     Label statusText) {
        this.dialogStage = dialogStage;
        this.mainScene = mainScene;
        this.themeComboBox = themeComboBox;
        this.statusText = statusText;
        this.onBack = () -> {
            if (dialogStage != null) {
                dialogStage.close();
            }
        };

        initInternal();
    }

    public void initEmbedded(Scene mainScene,
                             ComboBox<AppTheme> themeComboBox,
                             Label statusText,
                             Runnable onBack) {
        this.dialogStage = null;
        this.mainScene = mainScene;
        this.themeComboBox = themeComboBox;
        this.statusText = statusText;
        this.onBack = onBack;

        initInternal();
    }

    private void initInternal() {
        this.originalTheme = themeComboBox != null && themeComboBox.getValue() != null
                ? themeComboBox.getValue()
                : ThemeManager.loadSavedTheme();

        UserThemeConfig currentConfig = ThemeManager.loadCustomTheme();
        fillForm(currentConfig);

        if (dialogStatus != null) {
            dialogStatus.setText("Custom theme editor");
        }
    }

    private void fillForm(UserThemeConfig config) {
        setColor(bgColorPicker, config.getBackgroundColor());
        setColor(windowBarColorPicker, config.getWindowBarColor());
        setColor(topbarColorPicker, config.getTopbarColor());
        setColor(sidebarColorPicker, config.getSidebarColor());
        setColor(surfaceColorPicker, config.getSurfaceColor());
        setColor(primaryColorPicker, config.getPrimaryColor());
        setColor(textColorPicker, config.getTextColor());
        setColor(mutedTextColorPicker, config.getMutedTextColor());
        setColor(borderColorPicker, config.getBorderColor());
        setColor(hoverColorPicker, config.getHoverColor());

        selectedBackgroundImageUri = config.getBackgroundImageUri() == null ? "" : config.getBackgroundImageUri();
        if (backgroundImageField != null) {
            backgroundImageField.setText(selectedBackgroundImageUri);
        }

        if (fontComboBox != null) {
            if (config.getFontFamily() != null && !config.getFontFamily().isBlank()) {
                fontComboBox.setValue(config.getFontFamily());
            } else {
                fontComboBox.setValue("System");
            }
        }

        if (fontSizeSpinner != null && fontSizeSpinner.getValueFactory() != null) {
            fontSizeSpinner.getValueFactory().setValue(config.getFontSize());
        }
    }

    @FXML
    private void onPreview() {
        UserThemeConfig previewConfig = buildUserThemeConfig();
        ThemeManager.previewCustomTheme(mainScene, previewConfig);

        if (dialogStatus != null) {
            dialogStatus.setText("Preview applied");
        }
        if (statusText != null) {
            statusText.setText("Preview custom theme");
        }
    }

    @FXML
    private void onSave() {
        UserThemeConfig config = buildUserThemeConfig();

        ThemeManager.saveCustomTheme(config);
        ThemeManager.saveTheme(AppTheme.CUSTOM);
        ThemeManager.applyTheme(mainScene, AppTheme.CUSTOM);

        if (themeComboBox != null) {
            themeComboBox.setValue(AppTheme.CUSTOM);
        }

        if (statusText != null) {
            statusText.setText("Theme: Custom");
        }

        if (dialogStatus != null) {
            dialogStatus.setText("Custom theme saved");
        }

        saved = true;

        if (onBack != null) {
            onBack.run();
        }
    }

    @FXML
    private void onReset() {
        UserThemeConfig defaults = UserThemeConfig.defaultConfig();
        fillForm(defaults);

        if (dialogStatus != null) {
            dialogStatus.setText("Default values restored");
        }
    }

    @FXML
    private void onCancel() {
        restoreOriginalTheme();
        if (onBack != null) {
            onBack.run();
        }
    }

    @FXML
    private void onClose() {
        if (!saved) {
            restoreOriginalTheme();
        }
        if (onBack != null) {
            onBack.run();
        }
    }

    @FXML
    private void onChooseBackgroundImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose background image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        Stage resolvedOwner = dialogStage;
        if (resolvedOwner == null && mainScene != null && mainScene.getWindow() instanceof Stage s) {
            resolvedOwner = s;
        }

        final Stage owner = resolvedOwner;
        final boolean wasMaximized = owner != null && owner.isMaximized();
        final boolean wasFullScreen = owner != null && owner.isFullScreen();

        File file = chooser.showOpenDialog(owner);

        if (owner != null) {
            javafx.application.Platform.runLater(() -> {
                if (wasFullScreen) {
                    owner.setFullScreen(true);
                } else if (wasMaximized) {
                    owner.setMaximized(false);
                    owner.setMaximized(true);
                }
            });
        }

        if (file != null) {
            selectedBackgroundImageUri = file.toURI().toString();
            backgroundImageField.setText(selectedBackgroundImageUri);
            dialogStatus.setText("Background image selected");
        }
    }

    @FXML
    private void onClearBackgroundImage() {
        selectedBackgroundImageUri = "";
        backgroundImageField.setText("");
        dialogStatus.setText("Background image cleared");
    }

    private void restoreOriginalTheme() {
        ThemeManager.applyTheme(mainScene, originalTheme);

        if (themeComboBox != null) {
            themeComboBox.setValue(originalTheme);
        }

        if (statusText != null) {
            statusText.setText("Theme: " + originalTheme.getDisplayName());
        }
    }

    private UserThemeConfig buildUserThemeConfig() {
        return new UserThemeConfig(
                toCssColor(bgColorPicker.getValue()),
                toCssColor(windowBarColorPicker.getValue()),
                toCssColor(topbarColorPicker.getValue()),
                toCssColor(sidebarColorPicker.getValue()),
                toCssColor(surfaceColorPicker.getValue()),
                toCssColor(primaryColorPicker.getValue()),
                toCssColor(textColorPicker.getValue()),
                toCssColor(mutedTextColorPicker.getValue()),
                toCssColor(borderColorPicker.getValue()),
                toCssColor(hoverColorPicker.getValue()),
                selectedBackgroundImageUri,
                fontComboBox.getValue() == null ? "System" : fontComboBox.getValue(),
                fontSizeSpinner.getValue() == null ? 14 : fontSizeSpinner.getValue()
        );
    }

    private void setColor(ColorPicker picker, String cssColor) {
        if (picker == null || cssColor == null || cssColor.isBlank()) return;

        try {
            if (cssColor.startsWith("#")) {
                picker.setValue(Color.web(cssColor));
                return;
            }

            if (cssColor.startsWith("rgba")) {
                String raw = cssColor
                        .replace("rgba(", "")
                        .replace(")", "");
                String[] parts = raw.split(",");

                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                double a = Double.parseDouble(parts[3].trim());

                picker.setValue(Color.rgb(r, g, b, a));
            }
        } catch (Exception ignored) {
        }
    }

    private String toCssColor(Color color) {
        if (color == null) {
            return "rgba(255,255,255,1.0)";
        }

        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        double a = color.getOpacity();

        return String.format(Locale.US, "rgba(%d,%d,%d,%.3f)", r, g, b, a);
    }
}