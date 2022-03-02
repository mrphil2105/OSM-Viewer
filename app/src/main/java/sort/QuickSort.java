package sort;

import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;

// Inspiration: https://cs.stackexchange.com/questions/104816/implementation-of-quicksort-to-handle-duplicates
public class QuickSort {
    record Partition(int left, int right) {}

    public static void sort(int[] arr, int lo, int hi, IntBinaryOperator cmp, IntBiConsumer swap) {
        if (lo < hi) {
            var part = partition(arr, lo, hi, cmp, swap);

            sort(arr, lo, part.left() - 1, cmp, swap);
            sort(arr, part.right(), hi, cmp, swap);
        }
    }

    public static Partition partition(int[] arr, int lo, int hi, IntBinaryOperator cmp, IntBiConsumer swap) {
        var pivot = arr[(hi + lo) / 2];
        var r = lo;

        while (r <= hi) {
            var res = cmp.applyAsInt(arr[r], pivot);
            if (res < 0) {
                swap.accept(lo, r);
                lo++;
                r++;
            } else if (res > 0) {
                swap.accept(r, hi);
                hi--;
            } else {
                r++;
            }
        }

        return new Partition(lo, r);
    }

    public static void sort(long[] arr, int lo, int hi, LongBinaryOperator cmp, LongBiConsumer swap) {
        if (lo < hi) {
            var part = partition(arr, lo, hi, cmp, swap);

            sort(arr, lo, part.left() - 1, cmp, swap);
            sort(arr, part.right(), hi, cmp, swap);
        }
    }

    public static Partition partition(long[] arr, int lo, int hi, LongBinaryOperator cmp, LongBiConsumer swap) {
        var pivot = arr[(hi + lo) / 2];
        var r = lo;

        while (r <= hi) {
            var res = cmp.applyAsLong(arr[r], pivot);
            if (res < 0) {
                swap.accept(lo, r);
                lo++;
                r++;
            } else if (res > 0) {
                swap.accept(r, hi);
                hi--;
            } else {
                r++;
            }
        }

        return new Partition(lo, r);
    }
}
