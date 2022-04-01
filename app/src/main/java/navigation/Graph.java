package navigation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A directed graph that supports non-indexed vertexes */
class Graph implements Serializable {
    // Use a hashmap to support non-index values.
    private final Map<Long, List<Edge>> adjacentEdges;
    private int edgeCount;

    public Graph() {
        adjacentEdges = new HashMap<>();
    }

    public int edgeCount() {
        return edgeCount;
    }

    // TODO: If we are adding the same edge twice, because there is an identical path for different transport types,
    //  we simply add an additional role to the edge(s).
    public void addEdge(Edge edge) {
        var edges = adjacentEdges.computeIfAbsent(edge.from(), v -> new ArrayList<>());
        edges.add(edge);
        edgeCount++;
    }

    public Iterable<Edge> adjacent(long vertex) {
        return adjacentEdges.computeIfAbsent(vertex, v -> new ArrayList<>());
    }
}
