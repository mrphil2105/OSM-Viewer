package collections.spacial;

public abstract class SpacialTree<E> {
    public abstract int size();

    public abstract void insert(Point point, E value);

    public abstract boolean contains(Point point);

    public abstract QueryResult nearest(Point query);

    public abstract Iterable<QueryResult> range(Rect query);

    public boolean isEmpty() {
        return size() == 0;
    }

    public class QueryResult {
        private final Point point;
        private final E value;

        public QueryResult(Point point, E value) {
            this.point = point;
            this.value = value;
        }

        public Point point() {
            return point;
        }

        public E value() {
            return value;
        }
    }
}
