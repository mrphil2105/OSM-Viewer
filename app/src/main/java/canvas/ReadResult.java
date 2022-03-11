package canvas;

import drawing.Polygons;
import navigation.Dijkstra;

public record ReadResult(Polygons polygons, Dijkstra dijkstra) {}
