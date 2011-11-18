import java.lang.reflect.Array;
import java.util.Arrays;

public class SmallerBufferMergeSort extends Sort {
    @Override public <T> T[] sort(T[] array, Relation<? super T> relation) {
        mergeSort(array, 0, array.length, relation);
        return array;
    }
    
    private static class LessThanEqual<T> extends Sort.Relation<T> {
        public LessThanEqual(Sort.Relation<? super T> lessThanRelation) {
            this.lessThanRelation = lessThanRelation;
        }
        public @Override boolean lessThan(T a, T b) {
            return !lessThanRelation.lessThan(b, a);
        }
        private Sort.Relation<? super T> lessThanRelation;
    }
    
    private static class SortState<T> {
        SortState(T[] array, int first, int last,
                  Sort.Relation<? super T> lessThan) {
            this.lessThan = lessThan;
            this.lessThanEqual = new LessThanEqual<T>(lessThan);
            buffer = Util.<T>newArray((last - first) >> 1);
        }
        Sort.Relation<? super T> lessThan;
        Sort.Relation<? super T> lessThanEqual;
        T[] buffer;
    }
    
    private static <T> void mergeSort(T[] array, int first, int last, 
                                      Relation<? super T> relation) {
        SortState<T> state = new SortState<T>(array, first, last, relation);
        mergeSort(array, first, last, state);
    }
    private static <T> void mergeSort(T[] array, int first, int last, 
                                      SortState<T> state) {
        if (last - first <= 1) return;
        int mid = last + ((first - last) >> 1);
        mergeSort(array, first, mid, state);
        mergeSort(array, mid, last, state);
        mergeNeighbor(array, first, mid, last, state);
    }
    
    private static <T> void mergeNeighbor(T[] array, int first, int connect, 
                                          int last,
                                          SortState<T> state) {
        int llength = connect - first;
        int rlength = last - connect;
        if (llength < rlength) {
            mergeLeft(array, first, connect, last, state);
        } else {
            mergeRight(array, first, connect, last, state);
        }
    }
    
    private static <T> void mergeLeft(T[] array, int first, int connect, 
                                      int last,
                                      SortState<T> state) {
        T[] right = array;
        int rcur = connect;
        int rlast = last;
        
        int cur = Util.binSearch(array, first, connect, right[rcur], 
                                 state.lessThan);
        T[] left = state.buffer;
        int lcur = 0;
        int llast = connect - cur;
        System.arraycopy(array, cur, left, 0, llast);
        
        while (lcur < llast && rcur < rlast) {
            T lval = left[lcur];
            T rval = right[rcur];
            if (!state.lessThan.lessThan(rval, lval)) {
                array[cur++] = lval;
                lcur++;
            } else {
                array[cur++] = rval;
                rcur++;
            }
        }
        
        System.arraycopy(left, lcur, array, cur, llast - lcur);
    }
    
    private static <T> void mergeRight(T[] array, int first, int connect, 
                                       int last,
                                       SortState<T> state) {
        T[] left = array;
        int lcur = connect;
        int lfirst = first;
        
        int cur = Util.binSearch(array, connect, last, left[lcur - 1], 
                                 state.lessThanEqual);
        
        T[] right = state.buffer;
        int rcur = cur - connect;
        int rfirst = 0;
        System.arraycopy(array, connect, right, 0, rcur);
        
        while (lfirst < lcur && rfirst < rcur) {
            T lval = left[lcur - 1];
            T rval = right[rcur - 1];
            if (state.lessThan.lessThan(rval, lval)) {
                array[--cur] = lval;
                --lcur;
            } else {
                array[--cur] = rval;
                --rcur;
            }
        }
        
        System.arraycopy(right, rfirst, array, first, rcur - rfirst);
    }
    
    
    private static class Util {
        private static <T> T[] newArray(int length) {
            @SuppressWarnings({"unchecked"})
                T[] ret = (T[]) new Object[length];
            return ret;
        }
        
        private static <T> T[] slice(T[] array, int first, int last) {
            return Arrays.copyOfRange(array, first, last);
        }
        
        private static <T> int binSearch(T[] array, int first, int last, 
                                         T value,
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
    }
}
