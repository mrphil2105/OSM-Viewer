package navigation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A directed graph that supports non-indexed vertexes and splitting edges */
class Graph implements Serializable {
    private final int vertexCount;
    // Use a hashmap to support OSMNode ids instead of indexes.
    private final Map<Integer, List<Edge>> adjacentEdges;
    private int edgeCount;

    public Graph(int vertexCount) {
        this.vertexCount = vertexCount;
        adjacentEdges = new HashMap<>(vertexCount);
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edgeCount;
    }

    // TODO: If we are adding the same edge twice, because there is an identical path for different transport types,
    //  we simply add an additional role to the edge(s).
    // TODO: However, this might be troublesome, because if we add an edge that is equivalent to one that has been
    //  split, we need to update the new edges after that split. Not sure on this one. Perhaps we cannot split edges.
    //  Maybe we simply need to add additional edges and not have a role system?
    //  Perhaps an edge needs to remember its own splitting? So that the original edge still exists? But we still
    //  return its children in case of a split, and the children cannot have children, as that does not seem necessary.
    public void addEdge(Edge edge) {
        var edges = adjacentEdges.computeIfAbsent(edge.from(), v -> new ArrayList<>());
        edges.add(edge);
        edgeCount++;
    }

    // TODO: Implement 'splitEdge' for when adding a road that ends on an added road (it intersects).
    // TODO: Also check if we are adding an edge that creates an intersection, and if so we split with 'splitEdge'.
    private void splitEdge() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Iterable<Edge> adjacent(int vertex) {
        return adjacentEdges.get(vertex);
    }
}
