package io;

import java.io.IOException;
import java.io.OutputStream;
import osm.OSMObserver;

public interface Writer extends OSMObserver {
    void writeTo(OutputStream out) throws IOException;
}
