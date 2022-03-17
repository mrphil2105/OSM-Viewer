package io;

import navigation.Dijkstra;

public record ReadResult(PolygonsReader polygons, ObjectReader<Dijkstra> dijkstra)
        implements AutoCloseable {
    @Override
    public void close() throws Exception {
        polygons.close();
        dijkstra.close();
    }
}
