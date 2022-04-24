package collections.spatial;

import geometry.Point;
import geometry.Rect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LinearSearchTwoDTree<E> implements SpatialTree<E>, Serializable {
    private final int maxHeight;
    private final float left, top, right, bottom;

    private Node<E> root;
    private int size;

    public LinearSearchTwoDTree(int maxHeight) {
        this(maxHeight, 0, 0, 1, 1);
    }

    public LinearSearchTwoDTree(int maxHeight, float left, float top, float right, float bottom) {
        this.maxHeight = maxHeight;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void insert(Point point, E value) {
        if (point == null) {
            throw new IllegalArgumentException("Parameter 'point' cannot be null.");
        }

        if (isEmpty()) {
            root = insert(point, value, root, 0);
            root.rect = new Rect(top, left, bottom, right);
        } else {
            root = insert(point, value, root, 1);
        }
    }

    private Node<E> insert(Point point, E value, Node<E> node, int level) {
        if (node == null) {
            size++;

            if (level + 1 == maxHeight) {
                return new LeafNode<>(point, value, null);
            }

            return new AncestorNode<>(point, value, null);
        }

        if (node.contains(point)) {
            // Just return if we insert an existing point.
            return node;
        }

        if (node instanceof LeafNode<E> leafNode) {
            // The node is a leaf node, add the point and value to the lists.
            leafNode.add(point, value);

            return node;
        }

        // The node is an ancestor node, so we traverse the tree normally.
        var ancestorNode = (AncestorNode<E>)node;

        // Check if level is even.
        if ((level & 1) == 0) {
            // Search by y-coordinate (point with horizontal partition line).

            // We want to call insert on the left side if the new point is smaller at the y-axis.
            if (point.y() < ancestorNode.y()) {
                // Traverse down the tree. If this is a null child an insert will be performed.
                ancestorNode.left = insert(point, value, ancestorNode.left, level + 1);

                // If the child ancestorNode has an uninitialized rect we initialize it.
                if (ancestorNode.left.rect == null) {
                    ancestorNode.left.rect =
                        new Rect(ancestorNode.rect.top(), ancestorNode.rect.left(), ancestorNode.y(), ancestorNode.rect.right());
                }
            } else {
                ancestorNode.right = insert(point, value, ancestorNode.right, level + 1);

                if (ancestorNode.right.rect == null) {
                    ancestorNode.right.rect =
                        new Rect(ancestorNode.y(), ancestorNode.rect.left(), ancestorNode.rect.bottom(), ancestorNode.rect.right());
                }
            }
        } else {
            // Search by x-coordinate (point with vertical partition line).

            // We want to call insert on the left side if the new point is smaller at the x-axis.
            if (point.x() < ancestorNode.x()) {
                ancestorNode.left = insert(point, value, ancestorNode.left, level + 1);

                if (ancestorNode.left.rect == null) {
                    ancestorNode.left.rect =
                        new Rect(ancestorNode.rect.top(), ancestorNode.rect.left(), ancestorNode.rect.bottom(), ancestorNode.x());
                }
            } else {
                ancestorNode.right = insert(point, value, ancestorNode.right, level + 1);

                if (ancestorNode.right.rect == null) {
                    ancestorNode.right.rect =
                        new Rect(ancestorNode.rect.top(), ancestorNode.x(), ancestorNode.rect.bottom(), ancestorNode.rect.right());
                }
            }
        }

        return node;
    }

    @Override
    public boolean contains(Point point) {
        return contains(point, root, 1);
    }

    private boolean contains(Point point, Node<E> node, int level) {
        if (node == null) {
            return false;
        }

        if (node.contains(point)) {
            return true;
        }

        if (node instanceof LeafNode<E>) {
            // The above contains call did not return true, so the point does not exist.
            return false;
        }

        var ancestorNode = (AncestorNode<E>)node;

        // Check if level is even.
        if ((level & 1) == 0) {
            // Search by y-coordinate (point with horizontal partition line).

            // We want to check the left side if the point is smaller at the y-axis.
            if (point.y() < ancestorNode.y()) {
                return contains(point, ancestorNode.left, level + 1);
            } else {
                return contains(point, ancestorNode.right, level + 1);
            }
        } else {
            // Search by x-coordinate (point with vertical partition line).

            // We want to check the left side if the point is smaller at the x-axis.
            if (point.x() < ancestorNode.x()) {
                return contains(point, ancestorNode.left, level + 1);
            } else {
                return contains(point, ancestorNode.right, level + 1);
            }
        }
    }

    @Override
    public QueryResult<E> nearest(Point query) {
        if (query == null) {
            throw new IllegalArgumentException("Parameter 'query' cannot be null.");
        }

        if (isEmpty()) {
            return null;
        }

        var best = root.distanceSquaredTo(query);
        var champ = new QueryResult<>(best.point, best.value);

        return nearest(query, root, champ, best, 1);
    }

    private QueryResult<E> nearest(Point query, Node<E> node, QueryResult<E> champ, DistanceResult<E> best, int level) {
        // Check if the distance from the query point to the nearest point of
        // the node rectangle is greater than the current best distance.
        if (node == null || best.distance < node.rect.distanceSquaredTo(query)) {
            return champ;
        }

        // Set an actual best distance when we recur.
        best = new DistanceResult<>(champ.point().distanceSquaredTo(query), champ.point(), champ.value());
        var dist = node.distanceSquaredTo(query);

        // Check if the distance from the query point to the traversed point is less than
        // the distance from the query point to the current champion point.
        if (dist.distance < best.distance) {
            best = dist;
            champ = new QueryResult<>(dist.point, dist.value);
        }

        if (node instanceof LeafNode<E>) {
            // We cannot traverse the tree further from a leaf node.
            return champ;
        }

        var ancestorNode = (AncestorNode<E>)node;

        // Check if level is even.
        if ((level & 1) == 0) {
            // Search by y-coordinate (point with horizontal partition line).

            // We want to check the right side if the query point is greater at the y-axis.
            if (ancestorNode.y() < query.y()) {
                // Traverse down the tree to check child nodes.
                champ = nearest(query, ancestorNode.right, champ, best, level + 1);

                // Decide if we need to go down and check the other child.
                // Compare the distance from the query point to the nearest point of the ancestorNode rectangle
                // against the distance from the query point to the current champion point.
                // If it is smaller, there could potentially be a point that is closer to the query point
                // than the current champion point.
                if (ancestorNode.left != null
                    && ancestorNode.left.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, ancestorNode.left, champ, best, level + 1);
                }
            } else {
                champ = nearest(query, ancestorNode.left, champ, best, level + 1);

                if (ancestorNode.right != null
                    && ancestorNode.right.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, ancestorNode.right, champ, best, level + 1);
                }
            }
        } else {
            // Search by x-coordinate (point with vertical partition line).

            // We want to check the right side if the query point is greater at the x-axis.
            if (ancestorNode.x() < query.x()) {
                champ = nearest(query, ancestorNode.right, champ, best, level + 1);

                if (ancestorNode.left != null
                    && ancestorNode.left.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, ancestorNode.left, champ, best, level + 1);
                }
            } else {
                champ = nearest(query, ancestorNode.left, champ, best, level + 1);

                if (ancestorNode.right != null
                    && ancestorNode.right.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, ancestorNode.right, champ, best, level + 1);
                }
            }
        }

        return champ;
    }

    @Override
    public Iterable<QueryResult<E>> range(Rect query) {
        if (query == null) {
            throw new IllegalArgumentException("Parameter 'query' cannot be null.");
        }

        var results = new ArrayList<QueryResult<E>>();
        range(query, root, results);

        return results;
    }

    private void range(Rect query, Node<E> node, List<QueryResult<E>> results) {
        if (node == null || !node.rect.intersects(query)) {
            return;
        }

        if (node instanceof LeafNode<E> leafNode) {
            // For leaf nodes we need to iterate through the point list, and call 'contains' for each.
            for (int i = 0; i < leafNode.points.size(); i++) {
                var point = leafNode.points.get(i);

                if (query.contains(point)) {
                    var result = new QueryResult<>(point, leafNode.values.get(i));
                    results.add(result);
                }
            }

            return;
        }

        var ancestorNode = (AncestorNode<E>)node;

        if (query.contains(ancestorNode.point)) {
            var result = new QueryResult<>(ancestorNode.point, ancestorNode.value);
            results.add(result);
        }

        range(query, ancestorNode.left, results);
        range(query, ancestorNode.right, results);
    }

    private static abstract class Node<E> implements Serializable {
        protected Rect rect;

        public Node(Rect rect) {
            this.rect = rect;
        }

        public abstract boolean contains(Point point);

        public abstract DistanceResult<E> distanceSquaredTo(Point query);
    }

    private static class AncestorNode<E> extends Node<E> {
        private final Point point;
        private final E value;
        private Node<E> left;
        private Node<E> right;

        public AncestorNode(Point point, E value, Rect rect) {
            super(rect);

            this.point = point;
            this.value = value;
        }

        public float x() {
            return point.x();
        }

        public float y() {
            return point.y();
        }

        @Override
        public boolean contains(Point point) {
            return this.point.equals(point);
        }

        @Override
        public DistanceResult<E> distanceSquaredTo(Point query) {
            var distance = point.distanceSquaredTo(query);

            return new DistanceResult<>(distance, point, value);
        }
    }

    private static class LeafNode<E> extends Node<E> {
        private final List<Point> points;
        private final List<E> values;

        public LeafNode(Point firstPoint, E firstValue, Rect rect) {
            super(rect);

            points = new ArrayList<>();
            values = new ArrayList<>();

            add(firstPoint, firstValue);
        }

        public void add(Point point, E value) {
            points.add(point);
            values.add(value);
        }

        @Override
        public boolean contains(Point point) {
            return points.contains(point);
        }

        @Override
        public DistanceResult<E> distanceSquaredTo(Point query) {
            var shortestDistance = Float.MAX_VALUE;
            var bestIndex = -1;

            for (int i = 0; i < points.size(); i++) {
                var point = points.get(i);
                var distance = point.distanceSquaredTo(query);

                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    bestIndex = i;
                }
            }

            // 'bestIndex' should not be -1 here, as there should always be at least one point in the list.
            assert bestIndex != -1;

            var point = points.get(bestIndex);
            var value = values.get(bestIndex);

            return new DistanceResult<>(shortestDistance, point, value);
        }
    }

    private record DistanceResult<E>(float distance, Point point, E value) {
    }
}
