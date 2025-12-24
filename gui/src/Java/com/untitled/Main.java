package com.untitled;

import com.untitled.api.TokenStorage;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    TokenStorage tokenStorage = new TokenStorage();
    tokenStorage.clearToken();
    System.out.println("Token cleared on application startup");

    NavigationManager navManager = NavigationManager.getInstance();
    navManager.setPrimaryStage(stage);

    try {
      String css = getClass().getResource("styles/styles.css").toExternalForm();
      navManager.setCssStylesheet(css);
    } catch (Exception e) {
      System.out.println("Styles not found, continuing without them");
    }

    stage.setTitle("ChatApp");
    stage.setWidth(1200);
    stage.setHeight(800);

    navManager.navigateTo("views/login.fxml", "ChatApp - Login");
  }

  public static void main(String[] args) {
    launch();
  }
}
