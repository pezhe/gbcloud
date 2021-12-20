package ru.pezhe.core.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public class FileList extends AbstractMessage {

    private final List<FileInfo> files;
    private final String cloudPath;

    public FileList(Path currentPath, Path rootPath) throws IOException {
        files = new ArrayList<>();
        if (!currentPath.equals(rootPath)) {
            files.add(new FileInfo());
        }
        files.addAll(Files.list(currentPath).map(FileInfo::new).collect(Collectors.toList()));

        cloudPath = rootPath.relativize(currentPath).toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_LIST;
    }
}
