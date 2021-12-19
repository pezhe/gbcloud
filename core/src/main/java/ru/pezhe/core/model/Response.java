package ru.pezhe.core.model;

import lombok.Getter;

@Getter
public class Response extends AbstractMessage {

    private final boolean ok;
    private final String message;

    public Response (boolean status, String message) {
        this.ok = status;
        this.message = message;
    }

    @Override
    public CommandType getType() {
        return CommandType.RESPONSE;
    }

}
