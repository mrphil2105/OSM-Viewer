package collections;

import java.io.Serializable;

public class IntList implements Serializable {
    int[] array = new int[1];
    int n = 0;

    public IntList() {
    }

    public int add(int l) {
        if (n == array.length) {
            grow();
        }

        array[n] = l;
        return n++;
    }

    public int get(int index) {
        return array[index];
    }

    public int[] toArray() {
        return copyToSize(n);
    }

    public int size() {
        return n;
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
}
