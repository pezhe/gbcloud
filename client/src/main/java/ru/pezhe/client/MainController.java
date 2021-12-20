package ru.pezhe.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import ru.pezhe.core.model.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    public TextArea console;

    @FXML
    VBox localPanel, cloudPanel;

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private LocalPanelController localPanelController;
    private CloudPanelController cloudPanelController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        console.setText("Session started...");
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
                    case FILE_LIST: {
                        FileList fileList = (FileList) msg;
                        cloudPanelController.setCurrentPath(fileList.getCloudPath());
                        cloudPanelController.updateList(fileList.getFiles());
                        break;
                    }
                    case FILE_TRANSFER: {
                        System.out.println("received file");
                        FileTransfer fileMsg = (FileTransfer) msg;
                        Path result = Paths.get(localPanelController.getCurrentPath()).resolve(fileMsg.getFileName());
                        if (Files.exists(result)) {
                            console.appendText("\nFailed to download file " +
                                    fileMsg.getFileName() + " because it already exists in destination directory");
                        } else {
                            Files.write(result, fileMsg.getBytes());
                            localPanelController.updateList();
                            console.appendText("\nFile " +
                                    fileMsg.getFileName() + " successfully downloaded");
                        }
                        break;
                    }
                    case RESPONSE: {
                        Response response = (Response) msg;
                        console.appendText("\n" + response.getMessage());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uploadFileBtnAction(ActionEvent actionEvent) {
        FileInfo fileInfo = localPanelController.getSelectedItem();
        if (fileInfo == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Select file for uploading", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        if (cloudPanelController.contains(fileInfo.getFilename())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "File already exists", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        Path file = Paths.get(localPanelController.getCurrentPath()).resolve(fileInfo.getFilename());
        if (!Files.isDirectory(file)) {
            try {
                os.writeObject(new FileTransfer(file));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to upload file", ButtonType.OK);
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You can't upload directory", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public void downloadBtnAction(ActionEvent actionEvent) {
        FileInfo fileInfo = cloudPanelController.getSelectedItem();
        if (fileInfo == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Select file for downloading", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        if (localPanelController.contains(fileInfo.getFilename())) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "File already exists", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        if (!fileInfo.isDirectory()) {
            try {
                os.writeObject(new Request(CommandType.FILE_REQUEST, fileInfo.getFilename()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to request file", ButtonType.OK);
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You can't download directory", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    public void createDirBtnAction(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter directory name:");
        dialog.setTitle("Make directory");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if (!cloudPanelController.contains(result.get())) {
                try {
                    os.writeObject(new Request(CommandType.MKDIR_REQUEST, result.get()));
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to send mkdir request", ButtonType.OK);
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Directory already exists", ButtonType.OK);
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        }
    }

    public void deleteBtnAction(ActionEvent actionEvent) {
        FileInfo fileInfo = cloudPanelController.getSelectedItem();
        if (fileInfo == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Select file/directory to delete", ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete file/directory");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete " + fileInfo.getFilename());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            try {
                os.writeObject(new Request(CommandType.DEL_REQUEST, fileInfo.getFilename()));
            } catch (IOException e) {
                Alert alert1 = new Alert(Alert.AlertType.ERROR, "Failed to send delete request", ButtonType.OK);
                alert1.setHeaderText(null);
                alert1.showAndWait();
            }
        }
    }

    public void setDefaultPathBtnAction(ActionEvent actionEvent) {
        console.appendText("\nFeature is not available yet");
    }

}