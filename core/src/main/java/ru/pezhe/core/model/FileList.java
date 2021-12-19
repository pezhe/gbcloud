package ru.pezhe.core.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileList extends AbstractMessage {

    private List<FileInfo> files;
    private String cloudPath;

    public FileList(List<FileInfo> files, String cloudPath) throws IOException {
        this.files = files;
        this.cloudPath = cloudPath;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_LIST;
    }
}
