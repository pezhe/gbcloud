package ru.pezhe.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

public final class StreamHolder {

    private final static StreamHolder INSTANCE = new StreamHolder();
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    private StreamHolder() {}

    public static StreamHolder getInstance() {
        return INSTANCE;
    }

    public void setInputStream(ObjectDecoderInputStream is) {
        this.is = is;
    }

    public void setOutputStream(ObjectEncoderOutputStream os) {
        this.os = os;
    }

    public ObjectDecoderInputStream getInputStream() {
        return is;
    }

    public ObjectEncoderOutputStream getOutputStream() {
        return os;
    }

}
