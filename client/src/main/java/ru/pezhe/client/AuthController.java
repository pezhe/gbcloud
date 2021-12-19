package ru.pezhe.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ru.pezhe.core.model.AbstractMessage;
import ru.pezhe.core.model.CommandType;
import ru.pezhe.core.model.Request;
import ru.pezhe.core.model.Response;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    @FXML
    public Text text;

    @FXML
    public TextField host;

    @FXML
    public TextField port;

    @FXML
    public TextField login;

    @FXML
    public TextField password;

    @FXML
    public Button connectButton;

    @FXML
    public Button loginButton;

    @FXML
    public Button registerButton;

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        text.setText("Enter server address");
        host.setText("localhost");
        port.setText("8189");
        login.setDisable(true);
        password.setDisable(true);
        loginButton.setDisable(true);
        registerButton.setDisable(true);
    }

    public void connectBtnAction(ActionEvent actionEvent) {
        try {
            Socket socket = new Socket(host.getText(), Integer.parseInt(port.getText()));
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
        } catch (Exception e) {
            text.setFill(Color.RED);
            text.setText("Failed to connect to the server due to invalid address or server unavailability");
            return;
        }
        text.setFill(Color.BLACK);
        text.setText("Connected to server. Enter your login & password");
        host.setDisable(true);
        port.setDisable(true);
        connectButton.setDisable(true);
        login.setDisable(false);
        password.setDisable(false);
        loginButton.setDisable(false);
        registerButton.setDisable(false);
    }

    public void loginBtnAction(ActionEvent actionEvent) {
        try {
            os.writeObject(new Request(CommandType.AUTH_REQUEST, login.getText(), password.getText()));
            AbstractMessage msg = (AbstractMessage) is.readObject();
            if (msg.getType() == CommandType.RESPONSE) {
                Response response = (Response) msg;
                if (response.isOk()) {
                    launchMainWindow(actionEvent);
                } else {
                    text.setFill(Color.RED);
                    text.setText("Unable to login. Reason: " + response.getMessage());
                }
            } else {
                text.setFill(Color.RED);
                text.setText("Authorization failed. Unable to resolve server answer");
            }
        } catch (Exception e) {
            text.setFill(Color.RED);
            text.setText("Authorization failed due to application error");
        }

    }

    public void registerBtnAction(ActionEvent actionEvent) {
        try {
            os.writeObject(new Request(CommandType.REG_REQUEST, login.getText(), password.getText()));
            AbstractMessage msg = (AbstractMessage) is.readObject();
            if (msg.getType() == CommandType.RESPONSE) {
                Response response = (Response) msg;
                if (response.isOk()) {
                    launchMainWindow(actionEvent);
                } else {
                    text.setFill(Color.RED);
                    text.setText("Unable to register new user. Reason: " + response.getMessage());
                }
            } else {
                text.setFill(Color.RED);
                text.setText("Registration failed. Unable to resolve server answer");
            }
        } catch (Exception e) {
            text.setFill(Color.RED);
            text.setText("Registration failed due to application error");
        }
    }

    public void launchMainWindow(ActionEvent actionEvent) throws IOException {
        StreamHolder.getInstance().setInputStream(is);
        StreamHolder.getInstance().setOutputStream(os);
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 750, 600));
        stage.setTitle("GB Cloud");
        stage.show();
    }

    public void quitBtnAction(ActionEvent actionEvent) {
        Platform.exit();
    }
}
