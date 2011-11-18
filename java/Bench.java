import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;

public class Bench {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("java Bench SORT_CLASS_NAME");
            System.exit(1);
            return;
        } 
        
        Random random = new Random(0);
        Data orig = new Data(random, 150000, 1000);
        for (String name: args) {
            bench(name, orig.copy(), true);
            bench(name, orig.copy(), false);
        }
    }
    
    static void bench(String name, Data data, boolean warmup) 
        throws Exception {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        
        Class<? extends Sort> sortClass = 
            classLoader.loadClass(name).asSubclass(Sort.class);
        Sort sortObj = sortClass.newInstance();
        
        long start;
        long end;
        
        start = System.currentTimeMillis();
        sortObj.sort(data.natRandom);
        end = System.currentTimeMillis();
        if (!warmup) puts("[" + name + "] natRandom " + (end - start)  + "ns");
        
        start = System.currentTimeMillis();
        sortObj.sort(data.natAsc);
        end = System.currentTimeMillis();
        if (!warmup) puts("[" + name + "] natAsc " + (end - start) + "ns");
        
        start = System.currentTimeMillis();
        sortObj.sort(data.natDesc);
        end = System.currentTimeMillis();
        if (!warmup) puts("[" + name + "] natDesc " + (end - start) + "ns");
        
        start = System.currentTimeMillis();
        sortObj.sort(data.objRandom, data.relation);
        end = System.currentTimeMillis();
        if (!warmup) puts("[" + name + "] objRandom " + (end - start) + "ns");
        
        start = System.currentTimeMillis();
        sortObj.sort(data.objAsc, data.relation);
        end = System.currentTimeMillis();
        if (!warmup) puts("[" + name + "] objAsc " + (end - start) + "ns");
        
        start = System.currentTimeMillis();
        sortObj.sort(data.objDesc, data.relation);
        end = System.currentTimeMillis();
        if (!warmup) puts("[" + name + "] objDesc " + (end - start) + "ns");
    }
    
    static class Data {
        Data() {
        }
        Data(Random random, int size, int range) {
            relation = new EntryRelation();
            natRandom = new Integer[size];
            natAsc = new Integer[size];
            natDesc = new Integer[size];
            objRandom = new Entry[size];
            objAsc = new Entry[size];
            objDesc = new Entry[size];
            
            double rate = ((double) range) / size;
            int value;
            for (int i = 0; i < size; i++) {
                value = random.nextInt(range);
                natRandom[i] = value;
                objRandom[i] = new Entry(value, i);
            }
            
            value = 0;
            for (int i = 0; i < size; i++) {
                if (random.nextDouble() > rate) value++;
                natAsc[i] = value;
                objAsc[i] = new Entry(value, i);
            }
            
            value = size;
            for (int i = 0; i < size; i++) {
                if (random.nextDouble() > rate) value--;
                natDesc[i] = value;
                objDesc[i] = new Entry(value, i);
            }
        }
        
        Data copy() {
            Data data = new Data();
            data.relation = relation;
            data.natRandom = Arrays.copyOf(natRandom, natRandom.length);
            data.natAsc = Arrays.copyOf(natAsc, natAsc.length);
            data.natDesc = Arrays.copyOf(natDesc, natDesc.length);
            data.objRandom = Arrays.copyOf(objRandom, objRandom.length);
            data.objAsc = Arrays.copyOf(objAsc, objAsc.length);
            data.objDesc = Arrays.copyOf(objDesc, objDesc.length);
            return data;
        }
        
        EntryRelation relation;
        Integer[] natRandom;
        Integer[] natAsc;
        Integer[] natDesc;
        Entry[] objRandom;
        Entry[] objAsc;
        Entry[] objDesc;
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
    
    private static void puts(String s) {
        System.out.println(s);
    }
}
