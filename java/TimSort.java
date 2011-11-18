import java.lang.reflect.Array;
import java.util.Arrays;

public class TimSort extends Sort {
    @Override public <T> T[] sort(T[] array, Relation<? super T> relation) {
        timSort(array, 0, array.length, relation);
        return array;
    }
    private static <T> void timSort(T[] array, int first, int last, 
                                      Relation<? super T> relation) {
        SortState<T> state = new SortState<T>(array, first, last, relation);
        
        while (state.nextChunk()) {
            while (state.whenMerge()) state.mergeTwo();
        }
    }
    
    private static <T> void mergeNeighbor(T[] array, int first, int connect, 
                                          int last,
                                          SortState<T> state) {
        int llength = connect - first;
        int rlength = last - connect;
        if (llength < rlength) {
            Merge.mergeLeft(array, first, connect, last, state);
        } else {
            Merge.mergeRight(array, first, connect, last, state);
        }
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
            this.array = array;
            this.remain = first;
            this.last = last;
            int stackMax = stackMax(array.length);
            this.firstStack = new int[stackMax];
            this.lengthStack = new int[stackMax];
            this.stackTop = 0;
            this.minrunSize = minrunSize(array.length);
            
            this.lessThan = lessThan;
            this.lessThanEqual = new LessThanEqual<T>(lessThan);
            buffer = Util.<T>newArray((last - first) >> 1);
            mergeState = new MergeState<T>(this);
            minGallop = MIN_GALLOP;
        }
        
        private int stackMax(int length) {
            int ret = 0;
            while (length > 0) {
                length >>= 1;
                ret++;
            }
            return ret;
        }
        
        private int minrunSize(int length) {
            int tail = 0;
            while (length >= 64) {
                tail = tail | length & 1;
                length = length >> 1;
            }
            return length + tail;
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
                Util.reverse(array, remain, cur);
            }
            if (cur - remain < minrunSize) {
                int sortStart = cur;
                cur = Math.min(remain + minrunSize, last);
                Util.binarySort(array, remain, cur, sortStart, lessThan);
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
                          mergerFirst + mergerLength, this);
            lengthStack[stackTop - 2] += mergerLength;
            stackTop--;
        }
        
        int minrunSize;
        T[] array;
        int[] firstStack;
        int[] lengthStack;
        int stackTop;
        int remain;
        int last;
        
        Sort.Relation<? super T> lessThan;
        Sort.Relation<? super T> lessThanEqual;
        T[] buffer;
        MergeState<T> mergeState;
        int minGallop;
        static final int MIN_GALLOP = 7;
    }
    
    private static class MergeState<T> {
        MergeState(SortState<T> state) {
            this.state = state;
        }
        
        SortState<T> state;
        int cur;
        T[] left;
        int lcur;
        int lfirst;
        int llast;
        T[] right;
        int rcur;
        int rfirst;
        int rlast;
        boolean galloping;
        boolean gallopOut;
        int gallopCount;
        boolean selectLeft;
        int selectCount;
        
        void modeInit() {
            galloping = false;
            gallopOut = false;
            selectLeft = true;
            selectCount = 0;
        }
        void modeOnePair(boolean switched) {
            if (switched) {
                selectLeft = !selectLeft;
                selectCount = 0;
            } 
            selectCount++;
            if (selectCount >= state.minGallop) {
                galloping = true;
                selectCount = 0;
            }
        }
        void modeGalloping(int gallopSize) {
            if (gallopSize < state.MIN_GALLOP) {
                if (gallopOut) {
                    galloping = false;
                    gallopOut = false;
                    state.minGallop++;
                } else {
                    gallopOut = true;
                }
            } else {
                gallopOut = false;
            }
        }
    }
    
    private static class Merge {
        static <T> void mergeLeft(T[] array, int first, int connect, 
                                  int last,
                                  SortState<T> state) {
            MergeState<T> m = state.mergeState;
            m.right = array;
            m.rcur = connect;
            m.rlast = last;
            
            m.cur = Util.binSearch(array, first, connect, m.right[m.rcur], 
                                   state.lessThan);
            m.left = state.buffer;
            m.lcur = 0;
            m.llast = connect - m.cur;
            System.arraycopy(array, m.cur, m.left, 0, m.llast);
            
            m.modeInit();
            
            while (m.lcur < m.llast && m.rcur < m.rlast) {
                if (!m.galloping) {
                    leftOnePair(array, state, m);
                } else {
                    leftGalloping(array, state, m);
                }
            }
            
            System.arraycopy(m.left, m.lcur, array, m.cur, m.llast - m.lcur);
        }
        
        
        static <T> void leftOnePair(T[] array, SortState<T> state,
                                    MergeState<T> m) {
            T lval = m.left[m.lcur];
            T rval = m.right[m.rcur];
            if (!state.lessThan.lessThan(rval, lval)) {
                array[m.cur++] = lval;
                m.lcur++;
                m.modeOnePair(!m.selectLeft);
            } else {
                array[m.cur++] = rval;
                m.rcur++;
                m.modeOnePair(m.selectLeft);
            }
        }
    
        static <T> void leftGalloping(T[] array, SortState<T> state,
                                      MergeState<T> m) {
            if (state.minGallop > 0) state.minGallop--;
            T lval = m.left[m.lcur];
            T rval = m.right[m.rcur];
            if (state.lessThanEqual.lessThan(lval, rval)) {
                int end = gallopFirstSearch(m.left, m.lcur + 1, m.llast, 
                                            rval, 
                                            state.lessThan);
                int gallopSize = end - m.lcur;
                System.arraycopy(m.left, m.lcur, array, m.cur, gallopSize);
                m.cur += gallopSize;
                m.lcur += gallopSize;
                m.modeGalloping(gallopSize);
            } else {
                int end = gallopFirstSearch(m.right, m.rcur + 1, m.rlast, 
                                            lval, 
                                            state.lessThanEqual);
                int gallopSize = end - m.rcur;
                System.arraycopy(m.right, m.rcur, array, m.cur, gallopSize);
                m.cur += gallopSize;
                m.rcur += gallopSize;
                m.modeGalloping(gallopSize);
            }
        }
    
        static <T> void mergeRight(T[] array, int first, int connect, 
                                   int last,
                                   SortState<T> state) {
            MergeState<T> m = state.mergeState;
            m.left = array;
            m.lcur = connect;
            m.lfirst = first;
            
            m.cur = Util.binSearch(array, connect, last, m.left[m.lcur - 1], 
                                   state.lessThanEqual);
            
            m.right = state.buffer;
            m.rcur = m.cur - connect;
            m.rfirst = 0;
            System.arraycopy(array, connect, m.right, 0, m.rcur);
            
            m.modeInit();
            while (m.lfirst < m.lcur && m.rfirst < m.rcur) {
                if (!m.galloping) {
                    rightOnePair(array, state, m);
                } else {
                    rightGalloping(array, state, m);
                }
            }
            
            System.arraycopy(m.right, m.rfirst, array, first, 
                             m.rcur - m.rfirst);
        }
    
        static <T> void rightOnePair(T[] array, SortState<T> state,
                                     MergeState<T> m) {
            T lval = m.left[m.lcur - 1];
            T rval = m.right[m.rcur - 1];
            if (state.lessThan.lessThan(rval, lval)) {
                array[--m.cur] = lval;
                --m.lcur;
                m.modeOnePair(!m.selectLeft);
            } else {
                array[--m.cur] = rval;
                --m.rcur;
                m.modeOnePair(m.selectLeft);
            }      
        }
        
        static <T> void rightGalloping(T[] array, SortState<T> state,
                                       MergeState<T> m) {
            if (state.minGallop > 0) state.minGallop--;
            T lval = m.left[m.lcur - 1];
            T rval = m.right[m.rcur - 1];
            if (state.lessThan.lessThan(rval, lval)) {
                int begin = gallopLastSearch(m.left, m.lfirst, m.lcur - 1, 
                                             rval,
                                             state.lessThan);
                int gallopSize = m.lcur - begin;
                System.arraycopy(m.left, begin, array, m.cur - gallopSize, 
                                 gallopSize);
                m.cur -= gallopSize;
                m.lcur -= gallopSize;
                m.modeGalloping(gallopSize);
            } else {
                int begin = gallopLastSearch(m.right, m.rfirst, m.rcur - 1, 
                                             lval,
                                             state.lessThanEqual);
                int gallopSize = m.rcur - begin;
                System.arraycopy(m.right, begin, array, m.cur - gallopSize, 
                                 gallopSize);
                m.cur -= gallopSize;
                m.rcur -= gallopSize;
                m.modeGalloping(gallopSize);
            }
        }
        
        static <T> int gallopFirstSearch(T[] array, int first, int last, 
                                         T value,
                                         Relation<? super T> relation) {
            int pre = 0;
            int offset = 1;
            while (first + offset < last) {
                if (relation.lessThan(value, array[first + offset])) break;
                pre = offset;
                offset = (offset << 1) + 1;
            }
            int searchFirst = first + pre;
            int searchLast = Math.min(first + offset, last);
            return Util.binSearch(array, searchFirst, searchLast, value, 
                                  relation);
        }
        
        static <T> int gallopLastSearch(T[] array, int first, int last, 
                                        T value,
                                        Relation<? super T> relation) {
            int pre = 0;
            int offset = 1;
            while (first < last - offset) {
                if (!relation.lessThan(value, array[last - offset])) break;
                pre = offset;
                offset = (offset << 1) + 1;
            }
            int searchFirst = Math.max(first, last - offset);
            int searchLast = last - pre;
            return Util.binSearch(array, searchFirst, searchLast, value, 
                                  relation);
        }
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
                                         Sort.Relation<? super T> relation) {
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
        
        private static <T> void binarySort(T[] array, int first, int last, 
                                           Sort.Relation<? super T> relation) {
            binarySort(array, first, last, first + 1, relation);
        }
        private static <T> void binarySort(T[] array, int first, int last, 
                                           int start,
                                           Sort.Relation<? super T> relation) {
            for (int i = start; i < last; i++) {
                int point = binSearch(array, first, i, array[i], relation);
                cyclicRShift(array, point, i + 1);
            }
        }
        
        private static <T> void cyclicRShift(T[] array, int first, int last) {
            if (last - first <= 1) return;
            T mostRight = array[last - 1];
            System.arraycopy(array, first, array, first + 1, last - first - 1);
            array[first] = mostRight;
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
        
    }
}
