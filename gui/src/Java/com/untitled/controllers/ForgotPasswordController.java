package com.untitled.controllers;

import com.untitled.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Button resetBtn;
    @FXML private Label backToLoginLink;

    @FXML
    public void initialize() {
        System.out.println("Forgot Password View Initialized");
    }

    @FXML
    private void onResetClick() {
        String email = emailField != null ? emailField.getText() : "";
        System.out.println("Reset link requested for: " + email);
        
        NavigationManager.getInstance().navigateTo("views/login.fxml", "Login");
    }

    @FXML
    private void onBackToLoginClick() {
        NavigationManager.getInstance().navigateTo("views/login.fxml", "Login");
    }
}
