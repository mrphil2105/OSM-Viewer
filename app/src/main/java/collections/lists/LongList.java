package collections.lists;

import java.io.Serializable;
import java.util.Arrays;

public class LongList implements Serializable {
    private long[] array = new long[1];
    private int n = 0;

    public LongList() {}

    public int add(long value) {
        if (n == array.length) {
            grow();
        }

        array[n] = value;
        return n++;
    }

    public long get(int index) {
        return array[index];
    }

    public long set(int index, long value) {
        return array[index] = value;
    }

    public long[] toArray() {
        return copyToSize(n);
    }

    public long[] getArray() {
        return array;
    }

    public int size() {
        return n;
    }

    public void truncate(int count) {
        n -= count;
    }

    void grow() {
        setSize(array.length * 2);
    }

    void setSize(int sz) {
        array = copyToSize(sz);
    }

    long[] copyToSize(int sz) {
        var tmp = new long[sz];
        System.arraycopy(array, 0, tmp, 0, n);
        return tmp;
    }

    public void swap(int i, int j) {
        var tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public int search(long value) {
        return Arrays.binarySearch(array, 0, n, value);
    }

    public void extend(LongList other) {
        var newSize = size() + other.size();
        if (newSize > array.length) setSize(newSize);
        System.arraycopy(other.getArray(), 0, array, n, other.size());
    }
}
