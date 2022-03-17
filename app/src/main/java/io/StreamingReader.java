package io;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class StreamingReader<T> extends Reader<Iterable<T>> {

    public StreamingReader(ObjectInputStream in) {
        super(in);
    }

    @Override
    public Iterable<T> read() {
        return () ->
                new Iterator<>() {
                    private T obj;

                    @Override
                    public boolean hasNext() {
                        return obj != null || tryRead();
                    }

                    @Override
                    public T next() {
                        if (!hasNext()) throw new NoSuchElementException("next called on empty iterator");

                        try {
                            return obj;
                        } finally {
                            obj = null;
                        }
                    }

                    @SuppressWarnings("unchecked")
                    private boolean tryRead() {
                        try {
                            obj = (T) stream.readUnshared();
                        } catch (EOFException e) {
                            return false;
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                            throw new RuntimeException("error reading object from stream");
                        }

                        return true;
                    }
                };
    }
}
