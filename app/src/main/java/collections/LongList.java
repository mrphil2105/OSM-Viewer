package collections;

import java.io.Serializable;
import java.util.Arrays;

public class LongList implements Serializable {
    long[] array = new long[1];
    int n = 0;

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

    void swap(int i, int j) {
        var tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    int search(long value) {
        return Arrays.binarySearch(array, value);
    }
}
