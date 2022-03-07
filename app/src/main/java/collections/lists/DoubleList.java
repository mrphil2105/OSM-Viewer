package collections.lists;

import java.io.Serializable;
import java.util.Arrays;

public class DoubleList implements Serializable {
    double[] array = new double[1];
    int n = 0;

    public DoubleList() {}

    public int add(double value) {
        if (n == array.length) {
            grow();
        }

        array[n] = value;
        return n++;
    }

    public double get(int index) {
        return array[index];
    }

    public double set(int index, double value) {
        return array[index] = value;
    }

    public double[] toArray() {
        return copyToSize(n);
    }

    public double[] getArray() {
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

    double[] copyToSize(int sz) {
        var tmp = new double[sz];
        System.arraycopy(array, 0, tmp, 0, n);
        return tmp;
    }

    public void swap(int i, int j) {
        var tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    public int search(double value) {
        return Arrays.binarySearch(array, value);
    }

    public void extend(DoubleList other) {
        var newSize = size() + other.size();
        if (newSize > array.length) setSize(newSize);
        System.arraycopy(other.getArray(), 0, array, n, other.size());
    }
}
