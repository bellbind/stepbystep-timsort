import java.util.Arrays;

public class RunsTimSort extends Sort {
    @Override public <T> T[] sort(T[] array, Relation<? super T> relation) {
        timSort(array, 0, array.length, relation);
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
                  Relation<? super T> lessThan) {
            this.array = array;
            this.lessThan = lessThan;
            this.lessThanEqual = new LessThanEqual<T>(lessThan);
            this.remain = first;
            this.last = last;
            int stackMax = stackMax(array.length);
            this.firstStack = new int[stackMax];
            this.lengthStack = new int[stackMax];
            this.stackTop = 0;
        }
        
        private int stackMax(int length) {
            int ret = 0;
            while (length > 0) {
                length >>= 1;
                ret++;
            }
            return ret;
        }
        
        public final boolean nextChunk() {
            if (remain >= last) return false;
            if (last - remain <= 1) {
                pushChunk(last);
                return true;
            }
            int cur = remain;
            T preVal = array[cur++];
            T curVal = array[cur++];
            if (lessThanEqual.lessThan(preVal, curVal)) {
                preVal = curVal;
                while (cur < last) {
                    curVal = array[cur];
                    if (!lessThanEqual.lessThan(preVal, curVal)) break;
                    preVal = curVal;
                    cur++;
                }
            } else {
                preVal = curVal;
                while (cur < last) {
                    curVal = array[cur];
                    if (!lessThan.lessThan(curVal, preVal)) break;
                    preVal = curVal;
                    cur++;
                }
                reverse(array, remain, cur);
            }
            pushChunk(cur);
            return true;
        }
        
        private void pushChunk(int chunkLast) {
            int length = chunkLast - remain;
            firstStack[stackTop] = remain;
            lengthStack[stackTop] = length;
            stackTop++;
            remain = chunkLast;
        }
        
        public final boolean whenMerge() {
            if (remain == last) return stackTop > 1;
            if (stackTop <= 1) return false;
            int curLength = lengthStack[stackTop - 1];
            int preLength = lengthStack[stackTop - 2];
            if (stackTop == 2) return preLength <= curLength;
            int pre2Length = lengthStack[stackTop - 3];
            return pre2Length <= preLength + curLength;
        }
        
        public final void mergeTwo() {
            if (stackTop > 2 &&
                lengthStack[stackTop - 3] < lengthStack[stackTop - 1]) {
                int curFirst = firstStack[stackTop - 1];
                int curLength = lengthStack[stackTop - 1];
                stackTop--;
                mergeHeads();
                firstStack[stackTop] = curFirst;
                lengthStack[stackTop] = curLength;
                stackTop++;
            } else {
                mergeHeads();
            }
        }
        
        private void mergeHeads() {
            int mergerFirst = firstStack[stackTop - 1];
            int mergerLength = lengthStack[stackTop - 1];
            int mergedFirst = firstStack[stackTop - 2];
            int mergedLength = lengthStack[stackTop - 2];
            mergeNeighbor(array, mergedFirst, mergerFirst,
                          mergerFirst + mergerLength, lessThan);
            lengthStack[stackTop - 2] += mergerLength;
            stackTop--;
        }
        
        Sort.Relation<? super T> lessThan;
        Sort.Relation<? super T> lessThanEqual;
        T[] array;
        int[] firstStack;
        int[] lengthStack;
        int stackTop;
        int remain;
        int last;
    }
    
    private <T> void timSort(T[] array, int first, int last, 
                             Relation<? super T> relation) {
        SortState<T> state = new SortState<T>(array, first, last, relation);
        
        while (state.nextChunk()) {
            while (state.whenMerge()) state.mergeTwo();
        }
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
    
    private static <T> void reverse(T[] array, int first, int last) {
        last--;
        while (first < last) {
            swap(array, first++, last--);
        }
    }
    private static <T> void swap(T[] array, int a, int b) {
        T tmp = array[a];
        array[a] = array[b];
        array[b] = tmp;
    }
    
    private static <T> T[] slice(T[] array, int first, int last) {
        return Arrays.copyOfRange(array, first, last);
    }
}

