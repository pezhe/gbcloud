package ru.pezhe.core.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo implements Serializable {

    public enum FileType {
        FILE, DIRECTORY
    }

    private final String filename;
    private final FileType type;
    private long size;

    public String getFilename() {
        return filename;
    }

    public FileType getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public FileInfo(Path path) {
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path");
        }
    }

    public FileInfo() {
        this.filename = "..";
        this.size = -1L;
        this.type = FileType.DIRECTORY;
    }

    public boolean isDirectory() {
        return type == FileType.DIRECTORY;
    }

}