package navigation;

import static osm.elements.OSMTag.Key.*;

import java.io.Serializable;
import java.util.*;

import osm.OSMObserver;
import osm.elements.OSMTag;
import osm.elements.OSMWay;

public class Dijkstra implements OSMObserver, Serializable {
    private final Map<Long, Float> distTo;
    private final Set<Long> settled;
    private final PriorityQueue<Node> queue;

    private int vertexCount;
    private Graph graph;

    private EdgeRole mode;

    public Dijkstra() {
        distTo = new HashMap<>();
        settled = new HashSet<>();
        queue = new PriorityQueue<>();

        mode = EdgeRole.CAR;
    }

    @Override
    public void onWay(OSMWay way) {
        var tags = way.tags();

        if (tags.stream().noneMatch(t -> t.key() == HIGHWAY)) {
            return;
        }

        String name =
                tags.stream().filter(t -> t.key() == NAME).map(OSMTag::value).findFirst().orElse(null);

        int maxSpeed =
                tags.stream()
                        .filter(
                                t ->
                                        t.key() == MAXSPEED
                                                && !t.value().equals("signals")
                                                && !t.value().equals("none"))
                        .map(t -> Integer.parseInt(t.value()))
                        .findFirst()
                        .orElse(0);

        var road = new Road(name, calculateDistance(way), maxSpeed);
        roads.add(road);
    }

    public Map<Long, Float> shortestPath(long sourceVertex, EdgeRole mode) {
        this.mode = mode;
        distTo.clear();

        queue.add(new Node(sourceVertex, 0));
        distTo.put(sourceVertex, 0f);

        while (!queue.isEmpty()) {
            var vertex = queue.remove().vertex;

            // TODO: Check if a settled set is really necessary.
            //  It probably is when we have cycles or loops in the graph.
            if (settled.contains(vertex)) {
                continue;
            }

            // We settle the vertex BEFORE relaxing (and not after), in case there happens to be a loop.
            settled.add(vertex);
            relax(vertex);
        }

        return new HashMap<>(distTo);
    }

    private void relax(long vertex) {
        for (var edge : graph.adjacent(vertex)) {
            var to = edge.to();

            if (!settled.contains(to)) {
                var newDistance = distTo.get(vertex) + calculateWeight(edge);

                // TODO: Might have to use 'computeIfAbsent' here with Float.POSITIVE_INFINITY.
                if (newDistance < distTo.get(to)) {
                    distTo.put(to, newDistance);
                }

                queue.add(new Node(to, distTo.get(to)));
            }
        }
    }

    private float calculateWeight(Edge edge) {
        float weight = edge.distance();

        if (mode == EdgeRole.CAR) {
            weight /= edge.maxSpeed();
        }

        return weight;
    }

    private static float calculateDistance(OSMWay way) {
        var nodes = way.nodes();
        var firstNode = nodes[0];
        float totalDistance = 0;

        for (int i = 1; i < nodes.length; i++) {
            var secondNode = nodes[i];

            var x1 = firstNode.lat();
            var y1 = firstNode.lon();
            var x2 = secondNode.lat();
            var y2 = secondNode.lon();

            var distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            totalDistance += distance;

            firstNode = secondNode;
        }

        return totalDistance;
    }

    private record Node(long vertex, float weight) implements Comparable<Node> {
        @Override
        public int compareTo(Node other) {
            return Float.compare(weight, other.weight);
        }
    }
}
