package navigation;

import static osm.elements.OSMTag.Key.*;

import java.io.Serializable;
import java.util.*;

import javafx.util.Pair;
import osm.OSMObserver;
import osm.elements.*;

public class Dijkstra implements OSMObserver, Serializable {
    private final Graph graph;
    private final Map<Long, Float> distTo;
    private final Map<Long, Edge> edgeTo;
    private final Set<Long> settled;
    private final PriorityQueue<Node> queue;

    private EdgeRole mode;

    public Dijkstra() {
        graph = new Graph();
        distTo = new HashMap<>();
        edgeTo = new HashMap<>();
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

        int maxSpeed = tags.stream()
            .filter(t -> t.key() == MAXSPEED &&
                !t.value().equals("signals") &&
                !t.value().equals("none"))
            .map(t -> Integer.parseInt(t.value()))
            .findFirst()
            .orElse(0);

        var nodes = way.nodes();
        var firstNode = nodes[0];

        for (int i = 1; i < nodes.length; i++) {
            var secondNode = nodes[i];

            // TODO: Handle direction, e.g. one-way and both ways (add two edges).
            var firstVertex = coordinatesToLong((float)firstNode.lon(), (float)firstNode.lat());
            var secondVertex = coordinatesToLong((float)secondNode.lon(), (float)secondNode.lat());
            var distance = calculateDistance(firstNode, secondNode);
            // TODO: Parse roles from way.
            var edge = new Edge(firstVertex, secondVertex, distance, maxSpeed, null);
            graph.addEdge(edge);

            firstNode = secondNode;
        }
    }

    private static long coordinatesToLong(float lon, float lat) {
        var lonBits = Float.floatToIntBits(lon);
        var latBits = Float.floatToIntBits(lat);

        return (((long)lonBits) << 32) | (latBits & 0xFFFFFFFFL);
    }

    private static Pair<Float, Float> longToCoordinates(long value) {
        var lonBits = (int)(value >>> 32);
        var latBits = (int)(value & 0xFFFFFFFFL);

        var lon = Float.intBitsToFloat(lonBits);
        var lat = Float.intBitsToFloat(latBits);

        return new Pair<>(lon, lat);
    }

    public Map<Long, Edge> shortestPath(long sourceVertex, EdgeRole mode) {
        this.mode = mode;

        distTo.clear();
        edgeTo.clear();
        settled.clear();
        queue.clear();

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

        return new HashMap<>(edgeTo);
    }

    private void relax(long vertex) {
        for (var edge : graph.adjacent(vertex)) {
            var to = edge.to();

            if (!settled.contains(to)) {
                var newDistance = distTo.get(vertex) + calculateWeight(edge);

                // TODO: Might have to use 'computeIfAbsent' here with Float.POSITIVE_INFINITY.
                if (newDistance < distTo.computeIfAbsent(to, v -> Float.POSITIVE_INFINITY)) {
                    distTo.put(to, newDistance);
                    edgeTo.put(to, edge);
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

    private static float calculateDistance(SlimOSMNode firstNode, SlimOSMNode secondNode) {
        var x1 = firstNode.lon();
        var y1 = firstNode.lat();
        var x2 = secondNode.lon();
        var y2 = secondNode.lat();

        return (float)Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private record Node(long vertex, float weight) implements Comparable<Node> {
        @Override
        public int compareTo(Node other) {
            return Float.compare(weight, other.weight);
        }
    }
}
