import java.util.Arrays;
import java.util.Comparator;

public class BuiltinSort extends Sort {
    static class RelationComparator<T> implements Comparator<T> {
        RelationComparator(Relation<? super T> relation) {
            this.relation = relation;
        }
        @Override public int compare(T a, T b) {
            if (relation.lessThan(a, b)) return -1;
            if (relation.lessThan(b, a)) return 1;
            return 0;
        }
        private Relation<? super T> relation;
    }
    
    @Override public <T> T[] sort(T[] array, Relation<? super T> relation) {
        Arrays.sort(array, new RelationComparator<T>(relation));
        return array;
    }
}
