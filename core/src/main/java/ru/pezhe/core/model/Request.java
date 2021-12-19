package ru.pezhe.core.model;

import lombok.Getter;

@Getter
public class Request extends AbstractMessage {

    private final String[] params;
    private final CommandType type;

    public Request(CommandType type, String... args) {
        this.params = args;
        this.type = type;
    }

}
