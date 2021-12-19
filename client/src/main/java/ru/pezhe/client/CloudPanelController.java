package ru.pezhe.client;

import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.pezhe.core.model.CommandType;
import ru.pezhe.core.model.FileInfo;
import ru.pezhe.core.model.Request;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CloudPanelController implements Initializable {

    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    TextField pathField;

    private ObjectEncoderOutputStream os;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        os = StreamHolder.getInstance().getOutputStream();

        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().toString()));
        fileTypeColumn.setVisible(false);

        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Name");
        filenameColumn.setSortable(false);
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType() == FileInfo.FileType.DIRECTORY ? "[" + param.getValue().getFilename() + "]" : param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setSortable(false);
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "DIR";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        filesTable.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn);
        filesTable.getSortOrder().add(fileTypeColumn);

        filesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileInfo selection = filesTable.getSelectionModel().getSelectedItem();
                if (selection.getType() == FileInfo.FileType.DIRECTORY) {
                    requestUpdate(pathField.getText() + selection.getFilename());
                }
            }
        });

        requestUpdate(".");

    }

    public void updateList(Path path) {
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear();
            if (path.normalize().getNameCount() != 0) {
                filesTable.getItems().add(new FileInfo());
            }
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to update file list", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void requestUpdate(String folder) {
        try {
            os.writeObject(new Request(CommandType.LIST_REQUEST, folder));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to update file list", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void updateList(List<FileInfo> list) {
        filesTable.getItems().clear();
        filesTable.getItems().addAll(list);
        filesTable.sort();
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public String getSelectedFilename() {
        if (!filesTable.isFocused()) {
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }

    public void setCurrentPath (String path) {
        pathField.setText(path);
    }

    public void btnRootAction(ActionEvent actionEvent) {
        Path root = Paths.get(pathField.getText()).getRoot();
        if (root != null) {
            updateList(root);
        }
    }

}
