package ru.pezhe.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import ru.pezhe.core.model.AbstractMessage;
import ru.pezhe.core.model.CommandType;
import ru.pezhe.core.model.FileList;
import ru.pezhe.core.model.Request;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    TextArea messageField;

    @FXML
    VBox localPanel, cloudPanel;

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private LocalPanelController localPanelController;
    private CloudPanelController cloudPanelController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageField.setText("Client started...\n");
        localPanelController = (LocalPanelController) localPanel.getProperties().get("ctrl");
        cloudPanelController = (CloudPanelController) cloudPanel.getProperties().get("ctrl");
        is = StreamHolder.getInstance().getInputStream();
        os = StreamHolder.getInstance().getOutputStream();
        Thread t = new Thread(this::read);
        t.setDaemon(true);
        t.start();
    }

    private void read() {
        try {
            while (true) {
                AbstractMessage msg = (AbstractMessage) is.readObject();
                switch (msg.getType()) {
                    case FILE_LIST:
                        FileList fileList = (FileList) msg;
                        cloudPanelController.setCurrentPath(fileList.getCloudPath());
                        cloudPanelController.updateList(fileList.getFiles());
                        break;
                    case FILE_TRANSFER:
                        break;
                    case RESPONSE:
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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