package com.smartinventory.controller;

import com.smartinventory.SmartInventoryApp;
import com.smartinventory.repository.UserRepository;
import com.smartinventory.service.AlertService;
import com.smartinventory.service.LanguageService;
import com.smartinventory.service.SessionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.prefs.Preferences;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox rememberCheckBox;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label messageLabel;

    private final UserRepository userRepository = new UserRepository();
    private final Preferences preferences = Preferences.userNodeForPackage(LoginController.class);

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        visiblePasswordField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());
        String rememberedUser = preferences.get("rememberedUser", "");
        if (!rememberedUser.isBlank()) {
            usernameField.setText(rememberedUser);
            rememberCheckBox.setSelected(true);
        }
    }

    @FXML
    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText(LanguageService.get("validation.required"));
            return;
        }
        try {
            userRepository.authenticate(username, password).ifPresentOrElse(user -> {
                try {
                    if (rememberCheckBox.isSelected()) {
                        preferences.put("rememberedUser", username);
                    } else {
                        preferences.remove("rememberedUser");
                    }
                    SessionService.login(user);
                    SmartInventoryApp.showDashboard();
                } catch (Exception ex) {
                    AlertService.error(LanguageService.get("error.open.dashboard"));
                }
            }, () -> messageLabel.setText(LanguageService.get("login.invalid")));
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("error.database"));
        }
    }
}
