package com.accendo.math.tbp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.accendo.math.tbp.PrefixStorage.StorageType.L;


/**
 * Created by anna.myullyari on 5/10/18.
 */
public abstract class PrefixStorage<T> {

    public enum StorageType{I, L}
     int size;
     Map<String, T> map = Maps.newHashMap();

    public PrefixStorage(int size) {
        this.size = size;
    }

    public void updateStorage(String key, String marker){
        T st = map.get(key);
        if(st == null){
            st = init();
            map.put(key, init());
        }
        updateMarker(st, marker);
    }

    int prefixCount(String key){
        T storage = map.get(key);
        return storage == null ? 0 : count(storage);
    }

    Comparator<T> countComparator(){
        return (o1, o2) -> Integer.compare(count(o2), count(o1));
    }

    abstract void updateMarker(T storage, String marker);
    abstract T init();
    abstract int count(T storage);

    public static PrefixStorage createStorage(StorageType mode, int size){
        return mode == L ?
                listStorage(size) :
                countStorage(size);
    }

    public static PrefixStorage<AtomicInteger> countStorage(int size){
        return new PrefixStorage<AtomicInteger>(size) {
            @Override
            void updateMarker(AtomicInteger storage, String marker) {
                storage.incrementAndGet();
            }

            @Override
            AtomicInteger init() {
                return new AtomicInteger(0);
            }

            @Override
            int count(AtomicInteger storage) {
                return storage.get();
            }
        };
    }

    public static PrefixStorage<List<String>> listStorage(int size){
        return new PrefixStorage<List<String>>(size) {
            @Override
            void updateMarker(List<String> storage, String marker) {
                storage.add(marker);
            }

            @Override
            List<String> init() {
                return Lists.newArrayList();
            }

            @Override
            int count(List<String> storage) {
                return storage.size();
            }
        };
    }




    public  <T>void printTop(PrintWriter pw,  final int top) {
        pw.print(headPrefixSize());
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(countComparator()))
                .limit(top > 0 ? top : Integer.MAX_VALUE)
                .forEach(e -> {
                   printCounts(pw, e.getKey());
                });
    }

    protected <T>void printCounts(PrintWriter pw, String key){
        int count = prefixCount(key);
        if(count > 1){
            pw.println(StringUtils.rightPad(Integer.toString(count), 12)+key);
        }
    }

    public String headPrefixSize(){
        return "\n> "+size+'('+(map.size())+")\n";
    }
}
