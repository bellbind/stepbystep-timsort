public class BinarySort extends Sort {
    @Override public <T> T[] sort(T[] array, Relation<? super T> relation) {
        binarySort(array, 0, array.length, relation);
        return array;
    }
    
    private static <T> void binarySort(T[] array, int first, int last, 
                                       Relation<? super T> relation) {
        for (int i = first + 1; i < last; i++) {
            int point = binSearch(array, first, i, array[i], relation);
            cyclicRShift(array, point, i + 1);
        }
    }
    
    private static <T> int binSearch(T[] array, int first, int last, T value,
                                     Relation<? super T> relation) {
        while (first < last) {
            int mid = last + ((first - last) >> 1);
            if (relation.lessThan(value, array[mid])) {
                last = mid;
            } else {
                first = mid + 1;
            }
        }
        return first;
    }
    
    private static <T> void cyclicRShift(T[] array, int first, int last) {
        if (last - first <= 1) return;
        T mostRight = array[last - 1];
        System.arraycopy(array, first, array, first + 1, last - first - 1);
        array[first] = mostRight;
    }
}
