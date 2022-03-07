package collections.lists;

import java.io.Serializable;
import java.util.Arrays;

public class IntList implements Serializable {
    int[] array = new int[1];
    int n = 0;

    public IntList() {}

    public int add(int value) {
        if (n == array.length) {
            grow();
        }

        array[n] = value;
        return n++;
    }

    public int get(int index) {
        return array[index];
    }

    public int set(int index, int value) {
        return array[index] = value;
    }

    public int[] toArray() {
        return copyToSize(n);
    }

    public int[] getArray() {
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

    int[] copyToSize(int sz) {
        var tmp = new int[sz];
        System.arraycopy(array, 0, tmp, 0, n);
        return tmp;
    }

    public void swap(int i, int j) {
        var tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public int search(int value) {
        return Arrays.binarySearch(array, value);
    }

    public void extend(IntList other) {
        var newSize = size() + other.size();
        if (newSize > array.length) setSize(newSize);
        System.arraycopy(other.getArray(), 0, array, n, other.size());
    }
}
