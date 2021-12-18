package ru.pezhe.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    
    public Text text;
    public TextField host;
    public TextField port;
    public TextField login;
    public TextField password;
    public Button connectButton;
    public Button loginButton;
    public Button registerButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        text.setText("Enter server address");
        login.setDisable(true);
        password.setDisable(true);
        loginButton.setDisable(true);
        registerButton.setDisable(true);
    }

    public void connectBtnAction(ActionEvent actionEvent) {
    }

    public void loginBtnAction(ActionEvent actionEvent) {
    }

    public void registerBtnAction(ActionEvent actionEvent) {
    }

    public void quitBtnAction(ActionEvent actionEvent) {
        Platform.exit();
    }
}
