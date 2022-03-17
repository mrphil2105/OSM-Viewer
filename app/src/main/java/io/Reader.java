package io;

import java.io.ObjectInputStream;

public abstract class Reader<T> implements AutoCloseable {
    protected ObjectInputStream stream;

    public Reader(ObjectInputStream in) {
        setStream(in);
    }

    public abstract T read();

    protected void setStream(ObjectInputStream in) {
        stream = in;
    }

    @Override
    public void close() throws Exception {
        stream.close();
    }
}
