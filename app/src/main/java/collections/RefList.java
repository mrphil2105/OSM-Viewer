package collections;

import java.util.Arrays;

public class RefList {
    private long[] ids;
    private int size;
    private boolean isSorted;

    public RefList() {
        ids = new long[8];
    }

    public int size() {
        return size;
    }

    public void add(long id) {
        if (size == ids.length) {
            resize(size * 2);
        }

        ids[size++] = id;
        isSorted = false;
    }

    public boolean contains(long id) {
        if (!isSorted) {
            Arrays.sort(ids);
            isSorted = true;
        }

        var index = Arrays.binarySearch(ids, id);

        return index >= 0;
    }

    private void resize(int newSize) {
        ids = Arrays.copyOf(ids, newSize);
    }
}
