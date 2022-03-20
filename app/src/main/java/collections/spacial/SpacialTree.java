package collections.spacial;

public abstract class SpacialTree<E> {
    public abstract int size();

    public abstract void insert(E value, Point point);

    public abstract boolean contains(Point point);

    public abstract QueryResult nearest(Point query);

    public abstract Iterable<QueryResult> range(Rect query);

    public boolean isEmpty() {
        return size() == 0;
    }

    public class QueryResult {
        private final E value;
        private final Point point;

        public QueryResult(E value, Point point) {
            this.value = value;
            this.point = point;
        }

        public E value() {
            return value;
        }

        public Point point() {
            return point;
        }
    }
}
