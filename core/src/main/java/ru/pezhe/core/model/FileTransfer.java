package ru.pezhe.core.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileTransfer extends AbstractMessage {

    private String fileName;
    private byte[] bytes;

    public FileTransfer(Path path) throws IOException {
        fileName = path.getFileName().toString();
        bytes = Files.readAllBytes(path);
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_TRANSFER;
    }
}
