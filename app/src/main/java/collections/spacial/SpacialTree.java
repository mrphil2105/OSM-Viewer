package collections.spacial;

public abstract class SpacialTree<E> {
    public abstract boolean isEmpty();

    public abstract int size();

    public abstract void insert(E value, Point point);

    public abstract boolean contains(Point point);

    public abstract NearestResult nearest(Point query);

    public abstract Iterable<Point> range(Rect rect);

    public class NearestResult {
        private final E value;
        private final Point point;

        public NearestResult(E value, Point point) {
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
