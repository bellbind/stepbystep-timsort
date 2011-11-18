import java.util.Arrays;

public class MergeSort extends Sort {
    @Override public <T> T[] sort(T[] array, Relation<? super T> relation) {
        mergeSort(array, 0, array.length, relation);
        return array;
    }

    private static <T> void mergeSort(T[] array, int first, int last, 
                                      Relation<? super T> relation) {
        if (last - first <= 1) return;
        int mid = last + ((first - last) >> 1);
        mergeSort(array, first, mid, relation);
        mergeSort(array, mid, last, relation);
        mergeNeighbor(array, first, mid, last, relation);
    }
    
    private static <T> void mergeNeighbor(T[] array, int first, int connect, 
                                          int last,
                                          Relation<? super T> relation) {
        T[] left = slice(array, first, connect);
        int lcur = 0;
        int llast = connect - first;
        T[] right = slice(array, connect, last);
        int rcur = 0;
        int rlast = last - connect;
        
        int cur = first;
        while (lcur < llast && rcur < rlast) {
            T lval = left[lcur];
            T rval = right[rcur];
            if (!relation.lessThan(rval, lval)) {
                array[cur++] = lval;
                lcur++;
            } else {
                array[cur++] = rval;
                rcur++;
            }
        }
        
        System.arraycopy(left, lcur, array, cur, llast - lcur);
        System.arraycopy(right, rcur, array, cur, rlast - rcur);
    }

    private static <T> T[] slice(T[] array, int first, int last) {
        return Arrays.copyOfRange(array, first, last);
    }
}
