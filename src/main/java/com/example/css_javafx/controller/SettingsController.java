package com.example.css_javafx.controller;

import com.example.css_javafx.theme.AppTheme;
import com.example.css_javafx.theme.ThemeManager;
import com.example.css_javafx.theme.UserThemeConfig;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Locale;

public class SettingsController {

    @FXML private HBox windowBar;
    @FXML private ColorPicker borderColorPicker;
    @FXML private ColorPicker hoverColorPicker;

    @FXML private TextField backgroundImageField;
    @FXML private ComboBox<String> fontComboBox;
    @FXML private Spinner<Integer> fontSizeSpinner;
    private String selectedBackgroundImageUri = "";

    @FXML private Button dialogMinBtn;
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

    private double dragOffsetX;
    private double dragOffsetY;

    @FXML
    private void initialize() {
        windowBar.setOnMousePressed(this::handleWindowPressed);
        windowBar.setOnMouseDragged(this::handleWindowDragged);

        if (fontComboBox != null) {
            fontComboBox.getItems().setAll(Font.getFamilies());
        }

        if (fontSizeSpinner != null) {
            fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 40, 14));
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

        this.originalTheme = themeComboBox != null && themeComboBox.getValue() != null
                ? themeComboBox.getValue()
                : ThemeManager.loadSavedTheme();

        UserThemeConfig currentConfig = ThemeManager.loadCustomTheme();
        fillForm(currentConfig);

        if (dialogStage.getScene() != null) {
            ThemeManager.applyTheme(dialogStage.getScene(), originalTheme);
        }

        dialogStage.setOnCloseRequest(event -> {
            if (!saved) {
                restoreOriginalTheme();
            }
        });
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
        backgroundImageField.setText(selectedBackgroundImageUri);

        if (config.getFontFamily() != null && !config.getFontFamily().isBlank()) {
            fontComboBox.setValue(config.getFontFamily());
        } else {
            fontComboBox.setValue("System");
        }

        fontSizeSpinner.getValueFactory().setValue(config.getFontSize());
    }

    @FXML
    private void onPreview() {
        UserThemeConfig previewConfig = buildUserThemeConfig();

        System.out.println("Preview BG = " + previewConfig.getBackgroundColor());
        System.out.println("Preview Sidebar = " + previewConfig.getSidebarColor());
        System.out.println("Preview Accent = " + previewConfig.getPrimaryColor());

        ThemeManager.previewCustomTheme(mainScene, previewConfig);

        if (dialogStage.getScene() != null) {
            ThemeManager.previewCustomTheme(dialogStage.getScene(), previewConfig);
        }

        dialogStatus.setText("Preview applied");
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
        if (dialogStage.getScene() != null) {
            ThemeManager.applyTheme(dialogStage.getScene(), AppTheme.CUSTOM);
        }

        if (themeComboBox != null) {
            themeComboBox.setValue(AppTheme.CUSTOM);
        }

        if (statusText != null) {
            statusText.setText("Theme: Custom");
        }

        dialogStatus.setText("Custom theme saved");
        saved = true;
        dialogStage.close();
    }

    @FXML
    private void onReset() {
        UserThemeConfig defaults = UserThemeConfig.defaultConfig();
        fillForm(defaults);
        dialogStatus.setText("Default values restored");
    }

    @FXML
    private void onCancel() {
        restoreOriginalTheme();
        dialogStage.close();
    }

    @FXML
    private void onMinimize() {
        dialogStage.setIconified(true);
    }

    @FXML
    private void onClose() {
        if (!saved) {
            restoreOriginalTheme();
        }
        dialogStage.close();
    }

    private void restoreOriginalTheme() {
        ThemeManager.applyTheme(mainScene, originalTheme);
        if (dialogStage.getScene() != null) {
            ThemeManager.applyTheme(dialogStage.getScene(), originalTheme);
        }

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

    @FXML
    private void onChooseBackgroundImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose background image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        File file = chooser.showOpenDialog(dialogStage);
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

    private void setColor(ColorPicker picker, String hex) {
        if (picker == null || hex == null || !hex.matches("#[0-9a-fA-F]{6}")) return;
        picker.setValue(Color.web(hex));
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

    private void handleWindowPressed(MouseEvent event) {
        dragOffsetX = event.getSceneX();
        dragOffsetY = event.getSceneY();
    }

    private void handleWindowDragged(MouseEvent event) {
        dialogStage.setX(event.getScreenX() - dragOffsetX);
        dialogStage.setY(event.getScreenY() - dragOffsetY);
    }
}