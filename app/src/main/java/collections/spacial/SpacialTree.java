package collections.spacial;

public abstract class SpacialTree<E> {
    public abstract int size();

    public abstract void insert(Point point, E value);

    public abstract boolean contains(Point point);

    public abstract QueryResult<E> nearest(Point query);

    public abstract Iterable<QueryResult<E>> range(Rect query);

    public boolean isEmpty() {
        return size() == 0;
    }
}
