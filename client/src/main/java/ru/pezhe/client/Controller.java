package ru.pezhe.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    TextArea messageField;

    @FXML
    VBox localPanel, cloudPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageField.setText("Client started...\n");
    }

    public void copyBtnAction(ActionEvent actionEvent) {
        LocalPanelController leftPC = (LocalPanelController) localPanel.getProperties().get("ctrl");
        LocalPanelController rightPC = (LocalPanelController) cloudPanel.getProperties().get("ctrl");

        if (leftPC.getSelectedFilename() == null && rightPC.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file is selected", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        LocalPanelController srcPC = null, dstPC = null;
        if (leftPC.getSelectedFilename() != null) {
            srcPC = leftPC;
            dstPC = rightPC;
        }
        if (rightPC.getSelectedFilename() != null) {
            srcPC = rightPC;
            dstPC = leftPC;
        }

        Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFilename());
        Path dstPath = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());

        try {
            Files.copy(srcPath, dstPath);
            dstPC.updateList(Paths.get(dstPC.getCurrentPath()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to copy file", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void setDefaultPathBtnAction(ActionEvent actionEvent) {
    }

    public void uploadFileBtnAction(ActionEvent actionEvent) {
    }

    public void downloadBtnAction(ActionEvent actionEvent) {
    }

    public void createDirBtnAction(ActionEvent actionEvent) {
    }

    public void deleteBtnAction(ActionEvent actionEvent) {
    }
}