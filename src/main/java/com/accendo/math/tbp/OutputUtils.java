package com.accendo.math.tbp;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.accendo.math.tbp.PrefixProcessor.genKey;

/**
 * Created by anna.myullyari on 4/9/18.
 */
public class OutputUtils {


    public void traceEntropy(List<Pair<String, List<Double>>> entropy, int top){
        entropy.stream().limit(top).forEach(p -> System.out.println(p.getKey()+p.getValue()));
    }


    public static void traceSubsets(Map<String, Integer>[] subsets, int top){
        PrintWriter pw = new PrintWriter(System.out);
        Arrays.stream(subsets).filter(Predicates.notNull()).forEach(map -> printTop(pw, map, top));
    }

    private static void printTop(PrintWriter pw, Map<String, Integer> map, final int top) {
        pw.print(headPrefixSize(map));
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .filter(entry -> entry.getValue() > 1 && !entry.getKey().equals(genKey))
                .limit(top > 0 ? top : Integer.MAX_VALUE)
                .forEach(e -> {
                    pw.println(formatPrefix(e.getKey(), e.getValue()));
                });
    }

    public static String formatPrefix(String prefix, int count){
        return StringUtils.rightPad(Integer.toString(count), 12)+prefix;
    }

    public static String headPrefixSize(Map<String, Integer> map){
        return "\n> "+map.get(genKey)+'('+(map.size()-1)+")\n";
    }

    public static<T> void trace(PrefixStorage<T>[] subsets, int top){
        PrintWriter pw = new PrintWriter(System.out);
        Arrays.stream(subsets).filter(Predicates.notNull()).forEach(storage -> storage.printTop(pw, top));
    }


}
