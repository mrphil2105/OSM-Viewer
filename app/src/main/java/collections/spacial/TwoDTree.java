package collections.spacial;

import java.util.ArrayList;
import java.util.List;

public class TwoDTree<E> extends SpacialTree<E> {
    private Node root;
    private int size;

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void insert(E value, Point point) {
        if (point == null) {
            throw new IllegalArgumentException("Parameter 'point' cannot be null.");
        }

        if (isEmpty()) {
            root = insert(value, point, root, 0);
            root.rect = new Rect(0, 0, 1, 1);
        } else {
            root = insert(value, point, root, 1);
        }
    }

    private Node insert(E value, Point point, Node node, int level) {
        if (node == null) {
            size++;

            return new Node(value, point, null);
        }

        if (node.point.equals(point)) {
            return node;
        }

        if (level % 2 == 0) {
            if (point.y() < node.y()) {
                node.left = insert(value, point, node.left, level + 1);

                if (node.left.rect == null) {
                    node.left.rect = new Rect(node.rect.xMin(), node.rect.yMin(), node.rect.xMax(), node.y());
                }
            } else {
                node.right = insert(value, point, node.right, level + 1);

                if (node.right.rect == null) {
                    node.right.rect =
                            new Rect(node.rect.xMin(), node.y(), node.rect.xMax(), node.rect.yMax());
                }
            }
        } else {
            if (point.x() < node.x()) {
                node.left = insert(value, point, node.left, level + 1);

                if (node.left.rect == null) {
                    node.left.rect = new Rect(node.rect.xMin(), node.rect.yMin(), node.x(), node.rect.yMax());
                }
            } else {
                node.right = insert(value, point, node.right, level + 1);

                if (node.right.rect == null) {
                    node.right.rect =
                            new Rect(node.x(), node.rect.yMin(), node.rect.xMax(), node.rect.yMax());
                }
            }
        }

        return node;
    }

    @Override
    public boolean contains(Point point) {
        return contains(point, root, 1);
    }

    private boolean contains(Point point, Node node, int level) {
        if (node == null) {
            return false;
        }

        if (node.point.equals(point)) {
            return true;
        }

        if (level % 2 == 0) {
            if (point.y() < node.y()) {
                return contains(point, node.left, level + 1);
            } else {
                return contains(point, node.right, level + 1);
            }
        } else {
            if (point.x() < node.x()) {
                return contains(point, node.left, level + 1);
            } else {
                return contains(point, node.right, level + 1);
            }
        }
    }

    @Override
    public QueryResult nearest(Point query) {
        if (query == null) {
            throw new IllegalArgumentException("Parameter 'query' cannot be null.");
        }

        if (isEmpty()) {
            return null;
        }

        var best = root.point.distanceSquaredTo(query);
        var champ = new QueryResult(root.value, root.point);

        return nearest(query, root, champ, best, 1);
    }

    private QueryResult nearest(Point query, Node node, QueryResult champ, float best, int level) {
        if (node == null || best < node.rect.distanceSquaredTo(query)) {
            return champ;
        }

        best = champ.point().distanceSquaredTo(query);
        var dist = node.point.distanceSquaredTo(query);

        if (dist < best) {
            best = dist;
            champ = new QueryResult(node.value, node.point);
        }

        if (level % 2 == 0) {
            if (node.y() < query.y()) {
                champ = nearest(query, node.right, champ, best, level + 1);

                if (node.left != null
                        && node.left.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, node.left, champ, best, level + 1);
                }
            } else {
                champ = nearest(query, node.left, champ, best, level + 1);

                if (node.right != null
                        && node.right.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, node.right, champ, best, level + 1);
                }
            }
        } else {
            if (node.x() < query.x()) {
                champ = nearest(query, node.right, champ, best, level + 1);

                if (node.left != null
                        && node.left.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, node.left, champ, best, level + 1);
                }
            } else {
                champ = nearest(query, node.left, champ, best, level + 1);

                if (node.right != null
                        && node.right.rect.distanceSquaredTo(query) < champ.point().distanceSquaredTo(query)) {
                    champ = nearest(query, node.right, champ, best, level + 1);
                }
            }
        }

        return champ;
    }

    @Override
    public Iterable<QueryResult> range(Rect query) {
        if (query == null) {
            throw new IllegalArgumentException("Parameter 'query' cannot be null.");
        }

        var results = new ArrayList<QueryResult>();
        range(query, root, results);

        return results;
    }

    private void range(Rect query, Node node, List<QueryResult> results) {
        if (node == null || !node.rect.intersects(query)) {
            return;
        }

        if (query.contains(node.point)) {
            var result = new QueryResult(node.value, node.point);
            results.add(result);
        }

        range(query, node.left, results);
        range(query, node.right, results);
    }

    private class Node {
        private final E value;
        private final Point point;
        private Rect rect;
        private Node left;
        private Node right;

        public Node(E value, Point point, Rect rect) {
            this.value = value;
            this.point = point;
            this.rect = rect;
        }

        public float x() {
            return point.x();
        }

        public float y() {
            return point.y();
        }
    }
}
