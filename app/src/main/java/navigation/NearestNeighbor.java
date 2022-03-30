package navigation;

import static osm.elements.OSMTag.Key.HIGHWAY;
import static osm.elements.OSMTag.Key.NAME;

import collections.spacial.TwoDTree;
import geometry.Point;
import geometry.Rect;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.util.Pair;
import osm.OSMObserver;
import osm.elements.OSMTag;
import osm.elements.OSMWay;
import osm.elements.SlimOSMNode;

public class NearestNeighbor implements OSMObserver, Serializable {
    private transient List<Pair<SlimOSMNode, String>> nodeCache = new ArrayList<>();
    private TwoDTree<Node> twoDTree;

    public static NearestNeighbor instance;

    public NearestNeighbor() {
        instance = this;
    }

    @Override
    public void onBounds(Rect bounds) {
        twoDTree = new TwoDTree<>((float)bounds.left(),
            (float)bounds.top(),
            (float)bounds.right(),
            (float)bounds.bottom());
    }

    @Override
    public void onWay(OSMWay way) {
        assert twoDTree != null;

        var tags = way.tags();

        if (tags.stream().noneMatch(t -> t.key() == HIGHWAY)) {
            return;
        }

        var name = tags.stream().filter(t -> t.key() == NAME).map(OSMTag::value).findAny().orElse(null);

        if (name == null) {
            return;
        }

        for (var node : way.nodes()) {
            var pair = new Pair<>(node, name);
            nodeCache.add(pair);
        }
    }

    @Override
    public void onFinish() {
        var nodes = nodeCache;
        nodeCache = null;
        //noinspection unchecked
        addToTree(nodes.toArray(((Pair<SlimOSMNode, String>[]) new Pair[0])), 0);

        var road = nearestTo(new Point(0, 0));
    }

    public String nearestTo(Point point) {
        var nearestResult = twoDTree.nearest(point);

        if (nearestResult.value() instanceof AncestorNode node) {
            return node.name();
        }

        var node = (LeafNode) nearestResult.value();
        var points = node.points();
        float shortest = Float.MAX_VALUE;
        var bestIndex = 0;

        for (int i = 0; i < points.size(); i++) {
            var distance = points.get(i).distanceSquaredTo(point);

            if (distance < shortest) {
                shortest = distance;
                bestIndex = i;
            }
        }

        return node.names().get(bestIndex);
    }

    private void addToTree(Pair<SlimOSMNode, String>[] nodes, int level) {
        Arrays.sort(
                nodes,
                (first, second) ->
                        (level & 1) == 0
                                ? Double.compare(first.getKey().lon(), second.getKey().lon())
                                : Double.compare(first.getKey().lat(), second.getKey().lat()));

        var halfSize = nodes.length / 2;
        var median = nodes[halfSize];
        var point = new Point((float) median.getKey().lon(), (float) median.getKey().lat());

        if (nodes.length <= 1000) {
            var points =
                    Arrays.stream(nodes)
                            .map(p -> new Point((float) p.getKey().lon(), (float) p.getKey().lat()))
                            .toList();
            var names = Arrays.stream(nodes).map(Pair::getValue).toList();
            var node = new LeafNode(points, names);
            twoDTree.insert(point, node);

            return;
        }

        var node = new AncestorNode(median.getValue());
        twoDTree.insert(point, node);

        var firstHalf = Arrays.copyOfRange(nodes, 0, halfSize);
        var secondHalf = Arrays.copyOfRange(nodes, halfSize, nodes.length);
        addToTree(firstHalf, level + 1);
        addToTree(secondHalf, level + 1);
    }

    private interface Node extends Serializable {}

    private record AncestorNode(String name) implements Node {}

    private record LeafNode(List<Point> points, List<String> names) implements Node {}
}
