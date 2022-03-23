package io;

import navigation.Dijkstra;
import navigation.NearestNeighbor;

/** Object to manage multiple readers read from a file */
public record ReadResult(PolygonsReader polygons, ObjectReader<NearestNeighbor> nearestNeighbor, ObjectReader<Dijkstra> dijkstra)
        implements AutoCloseable {
    @Override
    public void close() throws Exception {
        polygons.close();
        nearestNeighbor.close();
        dijkstra.close();
    }
}
