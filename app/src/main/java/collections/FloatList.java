package collections;

import java.io.Serializable;

public class FloatList implements Serializable {
    float[] array = new float[1];
    int n = 0;

    public FloatList() {
    }

    public int add(float l) {
        if (n == array.length) {
            grow();
        }

        array[n] = l;
        return n++;
    }

    public float get(int index) {
        return array[index];
    }

    public float[] toArray() {
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

    float[] copyToSize(int sz) {
        var tmp = new float[sz];
        System.arraycopy(array, 0, tmp, 0, n);
        return tmp;
    }
}
