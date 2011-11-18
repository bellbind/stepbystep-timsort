import java.lang.reflect.Array;
import java.util.Arrays;

public class GallopSearchMergeSort extends Sort {
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
            mergeState = new MergeState<T>(this);
        }
        Sort.Relation<? super T> lessThan;
        Sort.Relation<? super T> lessThanEqual;
        T[] buffer;
        MergeState<T> mergeState;
        static final int MIN_GALLOP = 7;
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
            if (selectCount >= state.MIN_GALLOP) {
                galloping = true;
                selectCount = 0;
            }
        }
        void modeGalloping(int gallopSize) {
            if (gallopSize < state.MIN_GALLOP) {
                if (gallopOut) {
                    galloping = false;
                    gallopOut = false;
                } else {
                    gallopOut = true;
                }
            } else {
                gallopOut = false;
            }
        }
    }

    private static <T> void mergeLeft(T[] array, int first, int connect, 
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
    
    private static <T> void leftOnePair(T[] array, SortState<T> state,
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

    private static <T> void leftGalloping(T[] array, SortState<T> state,
                                          MergeState<T> m) {
        T lval = m.left[m.lcur];
        T rval = m.right[m.rcur];
        if (state.lessThanEqual.lessThan(lval, rval)) {
            int end = gallopFirstSearch(m.left, m.lcur + 1, m.llast, rval, 
                                        state.lessThan);
            int gallopSize = end - m.lcur;
            System.arraycopy(m.left, m.lcur, array, m.cur, gallopSize);
            m.cur += gallopSize;
            m.lcur += gallopSize;
            m.modeGalloping(gallopSize);
        } else {
            int end = gallopFirstSearch(m.right, m.rcur + 1, m.rlast, lval, 
                                        state.lessThanEqual);
            int gallopSize = end - m.rcur;
            System.arraycopy(m.right, m.rcur, array, m.cur, gallopSize);
            m.cur += gallopSize;
            m.rcur += gallopSize;
            m.modeGalloping(gallopSize);
        }
    }
    
    
    private static <T> void mergeRight(T[] array, int first, int connect, 
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
        
        System.arraycopy(m.right, m.rfirst, array, first, m.rcur - m.rfirst);
    }
    
    private static <T> void rightOnePair(T[] array, SortState<T> state,
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
    
    private static <T> void rightGalloping(T[] array, SortState<T> state,
                                           MergeState<T> m) {
        T lval = m.left[m.lcur - 1];
        T rval = m.right[m.rcur - 1];
        if (state.lessThan.lessThan(rval, lval)) {
            int begin = gallopLastSearch(m.left, m.lfirst, m.lcur - 1, rval,
                                         state.lessThan);
            int gallopSize = m.lcur - begin;
            System.arraycopy(m.left, begin, array, m.cur - gallopSize, 
                             gallopSize);
            m.cur -= gallopSize;
            m.lcur -= gallopSize;
            m.modeGalloping(gallopSize);
        } else {
            int begin = gallopLastSearch(m.right, m.rfirst, m.rcur - 1, lval,
                                         state.lessThanEqual);
            int gallopSize = m.rcur - begin;
            System.arraycopy(m.right, begin, array, m.cur - gallopSize, 
                             gallopSize);
            m.cur -= gallopSize;
            m.rcur -= gallopSize;
            m.modeGalloping(gallopSize);
        }
    }
    
    private static <T> int gallopFirstSearch(T[] array, int first, int last, 
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
        return Util.binSearch(array, searchFirst, searchLast, value, relation);
    }
    
    private static <T> int gallopLastSearch(T[] array, int first, int last, 
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
        return Util.binSearch(array, searchFirst, searchLast, value, relation);
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
