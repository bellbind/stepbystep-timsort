public abstract class Sort {
    public static abstract class Relation<T> {
        public abstract boolean lessThan(T a, T b);
    }
    
    public static class BuiltinRelation<T extends Comparable<T>> 
        extends Relation<T> {
        @Override public boolean lessThan(T a, T b) {
            return a.compareTo(b) < 0;
        }
    }
    
    public abstract <T> T[] sort(T[] array, Relation<? super T> lessThan);
    public final <T extends Comparable<T>> T[] sort(T[] array) {
        return sort(array, new BuiltinRelation<T>());
    }
}
