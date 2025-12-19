package com.untitled;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Maincontroller {

  @FXML
  private VBox container;

  @FXML
  private Label titleText;

  @FXML
  private Button clickMeBtn;

  @FXML
  public void initialize() {
    titleText.setText("JavaFX Project Ready!");
    clickMeBtn.setOnAction(e -> titleText.setText("Button clicked!"));
  }
}
