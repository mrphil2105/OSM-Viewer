package navigation;

import static osm.elements.OSMTag.Key.HIGHWAY;
import static osm.elements.OSMTag.Key.NAME;

import collections.spacial.TwoDTree;
import geometry.Point;
import geometry.Rect;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import osm.OSMObserver;
import osm.elements.OSMTag;
import osm.elements.OSMWay;

public class NearestNeighbor implements OSMObserver, Serializable {
    private transient List<Pair<Point, String>> nodeCache = new ArrayList<>();
    private TwoDTree<Node> twoDTree;

    public static NearestNeighbor instance;

    public NearestNeighbor() {
        instance = this;
    }

    @Override
    public void onBounds(Rect bounds) {
        twoDTree = new TwoDTree<>((float)Point.geoToMapX(bounds.left()),
            (float)Point.geoToMapY(bounds.bottom()),
            (float)Point.geoToMapX(bounds.right()),
            (float)Point.geoToMapY(bounds.top()));
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
            var point = new Point((float)Point.geoToMapX(node.lon()), (float)Point.geoToMapY(node.lat()));
            var pair = new Pair<>(point, name);
            nodeCache.add(pair);
        }
    }

    @Override
    public void onFinish() {
        var nodes = nodeCache;
        nodeCache = null;
        addToTree(nodes, 0);

        var road = nearestTo(new Point(0, 0));
    }

    public String nearestTo(Point query) {
        var nearestResult = twoDTree.nearest(query);

        if (nearestResult.value() instanceof AncestorNode node) {
            return node.name();
        }

        var node = (LeafNode) nearestResult.value();
        var points = node.points();
        float shortest = Float.MAX_VALUE;
        var bestIndex = 0;

        for (int i = 0; i < points.size(); i++) {
            var distance = points.get(i).distanceSquaredTo(query);

            if (distance < shortest) {
                shortest = distance;
                bestIndex = i;
            }
        }

        return node.names().get(bestIndex);
    }

    private void addToTree(List<Pair<Point, String>> nodes, int level) {
        nodes.sort((first, second) -> (level & 1) == 0 ?
            Float.compare(first.getKey().x(), second.getKey().x()) :
            Float.compare(first.getKey().y(), second.getKey().y()));

        var halfSize = nodes.size() / 2;
        var median = nodes.get(halfSize);
        var point = median.getKey();

        if (nodes.size() <= 1000) {
            var points = nodes.stream().map(Pair::getKey).toList();
            var names = nodes.stream().map(Pair::getValue).toList();
            var node = new LeafNode(points, names);
            twoDTree.insert(point, node);

            return;
        }

        var node = new AncestorNode(median.getValue());
        twoDTree.insert(point, node);

        var firstHalf = nodes.subList(0, halfSize);
        var secondHalf = nodes.subList(halfSize, nodes.size());
        addToTree(firstHalf, level + 1);
        addToTree(secondHalf, level + 1);
    }

    private interface Node extends Serializable {}

    private record AncestorNode(String name) implements Node {}

    private record LeafNode(List<Point> points, List<String> names) implements Node {}
}
