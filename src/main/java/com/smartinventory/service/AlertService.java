package com.smartinventory.service;

import javafx.scene.control.Alert;

public final class AlertService {
    private AlertService() {
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, message);
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, message);
    }

    private static void show(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(LanguageService.get("app.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
