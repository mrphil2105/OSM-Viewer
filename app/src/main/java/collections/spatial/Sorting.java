package collections.spatial;

import java.util.*;

class Sorting {
    // Inspiration: https://stackoverflow.com/a/24688655/3180829
    public static <T> void multiSort(List<T> key, Comparator<T> comparator, List<?>... lists) {
        List<Integer> indices = new ArrayList<>(key.size());

        for (int i = 0; i < key.size(); i++) {
            indices.add(i);
        }

        indices.sort((i, j) -> comparator.compare(key.get(i), key.get(j)));

        // Create a mapping that allows sorting of the List by N swaps.
        Map<Integer, Integer> swapMap = new HashMap<>(indices.size());
        List<Integer> swapFrom = new ArrayList<>(indices.size()),
            swapTo = new ArrayList<>(indices.size());

        for (int i = 0; i < key.size(); i++) {
            int k = indices.get(i);

            while (i != k && swapMap.containsKey(k)) {
                k = swapMap.get(k);
            }

            swapFrom.add(i);
            swapTo.add(k);
            swapMap.put(i, k);
        }

        // use the swap order to sort each list by swapping elements.
        for (List<?> list : lists) {
            for (int i = 0; i < list.size(); i++) {
                Collections.swap(list, swapFrom.get(i), swapTo.get(i));
            }
        }
    }
}
