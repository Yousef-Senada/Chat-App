package com.untitled;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationManager {

    private static NavigationManager instance;
    private Stage primaryStage;
    private String cssStylesheet;
    private Object currentController;

    private NavigationManager() {
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
    }

    public void setCssStylesheet(String css) {
        this.cssStylesheet = css;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Object getCurrentController() {
        return currentController;
    }

    public void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            currentController = loader.getController();
            
            Scene scene = new Scene(root);
            
            if (cssStylesheet != null && !cssStylesheet.isEmpty()) {
                scene.getStylesheets().add(cssStylesheet);
            }
            
            try {
                String stylesPath = getClass().getResource("styles/styles.css").toExternalForm();
                if (!scene.getStylesheets().contains(stylesPath)) {
                    scene.getStylesheets().add(stylesPath);
                }
            } catch (Exception e) {
            }
            
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public void navigateTo(String fxmlFile, String title) {
        navigateTo(fxmlFile);
        if (primaryStage != null && title != null) {
            primaryStage.setTitle(title);
        }
    }

    public void navigateTo(String fxmlFile, String title, double width, double height) {
        navigateTo(fxmlFile, title);
        if (primaryStage != null) {
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.centerOnScreen();
        }
    }

    public void refresh() {
        if (primaryStage != null && primaryStage.getScene() != null) {
            Scene currentScene = primaryStage.getScene();
            currentScene.getStylesheets().clear();
            if (cssStylesheet != null) {
                currentScene.getStylesheets().add(cssStylesheet);
            }
        }
    }
}
