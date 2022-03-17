package collections.spacial;

public interface SpacialTree<E> {
    boolean isEmpty();

    int size();

    void insert(E value, Point point);

    boolean contains(Point point);

    Point nearest(Point query);

    Iterable<Point> range(Rect rect);
}
