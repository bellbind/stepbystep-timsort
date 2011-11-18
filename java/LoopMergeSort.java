import java.util.Arrays;

public class LoopMergeSort extends Sort {
    @Override public <T> T[] sort(T[] array, Relation<? super T> relation) {
        mergeSort(array, 0, array.length, relation);
        return array;
    }

    private static <T> void mergeSort(T[] array, int first, int last, 
                                      Relation<? super T> relation) {
        int stackMax = stackMax(last - first);
        int[] firstStack = new int[stackMax];
        int[] lengthStack = new int[stackMax];
        int stackTop = 0;
        
        int remain = first;
        while (remain < last) {
            firstStack[stackTop] = remain;
            lengthStack[stackTop] = 1;
            stackTop++;
            remain++;
            
            while (stackTop > 1 &&
                   (remain >= last ||
                    lengthStack[stackTop - 2] < 
                    lengthStack[stackTop - 1] * 2)) {
                int preFirst = firstStack[stackTop - 2];
                int preLength = lengthStack[stackTop - 2];
                int curFirst = firstStack[stackTop - 1];
                int curLength = lengthStack[stackTop - 1];
                assert preFirst + preLength == curFirst;
                
                mergeNeighbor(array, preFirst, curFirst, curFirst + curLength,
                              relation);
                lengthStack[stackTop - 2] += curLength;
                stackTop--;
            }
        }
    }
    
    private static int stackMax(int length) {
        int ret = 0;
        while (length > 0) {
            length >>= 1;
            ret++;
        }
        return ret;
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
