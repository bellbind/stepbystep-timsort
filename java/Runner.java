import java.util.Random;
import java.util.ArrayList;

public class Runner {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("java Runner SORT_CLASS_NAME");
            System.exit(1);
            return;
        } 
        String className = args[0];
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        
        Class<? extends Sort> sortClass = 
            classLoader.loadClass(className).asSubclass(Sort.class);
        Sort sortObj = sortClass.newInstance();
        
        runBasic(sortObj);
        runMiniSize(sortObj);
        runLargeRandomWithStability(sortObj);
    }
    
    private static void runBasic(Sort sortObj) {
        puts(toS(sortObj.sort(new Integer[]{})));
        puts(toS(sortObj.sort(new Integer[]{1})));
        puts(toS(sortObj.sort(new Integer[]{2,1})));
        puts(toS(sortObj.sort(new Integer[]{1,2})));
        puts(toS(sortObj.sort(new Integer[]{3,2,1})));
        puts(toS(sortObj.sort(new Integer[]{1,2,3})));
    }
    
    private static void runMiniSize(Sort sortObj) {
        puts(toS(sortObj.sort(new Integer[]{
                        1,2,3,9,8,7,10,11,12,})));
        puts(toS(sortObj.sort(new Integer[]{
                        1,2,3,21,22,23,4,13,6,7,8,9,0,11,12,33,
                        14,15,16,17,18,19,20,24,30,25,26,27,28,29,31,32,})));
    }
    
    static class Entry {
        int value;
        int index;
        public Entry(int value, int index) {
            this.index = index;
            this.value = value;
        }
        @Override public String toString() {
            return "{value: " + value + ", index: " + index + "}";
        }
    }
    
    static class EntryRelation extends Sort.Relation<Entry> {
        @Override public boolean lessThan(Entry a, Entry b) {
            return a.value < b.value;
        }
    }
    
    static void runLargeRandomWithStability(Sort sortObj) {
        Random random = new Random(0);
        Entry[] a = new Entry[15000];
        for (int i = 0; i < a.length; i++) {
            int value = random.nextInt(100);
            a[i] = new Entry(value, i);
        }
        EntryRelation relation = new EntryRelation();
        sortObj.sort(a, relation);
        ArrayList<String> failures = new ArrayList<String>();
        for (int i = 1; i < a.length; i++) {
            if (a[i-1].value > a[i].value) {
                failures.add("at " + i + " pre: " + a[i-1] + " cur: " + a[i]);
            } else if (a[i-1].value == a[i].value && 
                       a[i-1].index > a[i].index) {
                failures.add("at " + i + " pre: " + a[i-1] + " cur: " + a[i]);
            }
        }
        if (failures.size() == 0) {
            puts("large sort success");
        } else {
            puts("large sort failed");
            for (String failure : failures) {
                puts(failure);
            }
        }
    }
    
    private static <T> String toS(T[] array) {
        StringBuffer buf = new StringBuffer("[");
        for (T elem : array) {
            buf.append(elem.toString()).append(", ");
        }
        buf.append("]");
        return buf.toString();
    }
    
    private static void puts(String s) {
        System.out.println(s);
    }
}
