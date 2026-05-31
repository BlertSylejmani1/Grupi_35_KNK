package com.smartinventory;

import com.smartinventory.service.LanguageService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SmartInventoryApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showLogin();
    }

    public static void showLogin() throws IOException {
        showScene("/com/smartinventory/view/login.fxml", "app.title", 520, 420);
    }

    public static void showDashboard() throws IOException {
        showScene("/com/smartinventory/view/dashboard.fxml", "app.title", 1180, 760);
    }

    public static void showDashboardPreservingWindow() throws IOException {
        double width = primaryStage.getWidth() > 0 ? primaryStage.getWidth() : 1180;
        double height = primaryStage.getHeight() > 0 ? primaryStage.getHeight() : 760;
        boolean maximized = primaryStage.isMaximized();
        showScene("/com/smartinventory/view/dashboard.fxml", "app.title", width, height);
        primaryStage.setMaximized(maximized);
    }

    public static void showScene(String fxml, String titleKey, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(SmartInventoryApp.class.getResource(fxml), LanguageService.getBundle());
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(SmartInventoryApp.class.getResource("/com/smartinventory/styles/app.css").toExternalForm());
        primaryStage.setTitle(LanguageService.get(titleKey));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(width * 0.8);
        primaryStage.setMinHeight(height * 0.8);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
