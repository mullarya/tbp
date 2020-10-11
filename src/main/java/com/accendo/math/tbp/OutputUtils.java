package com.accendo.math.tbp;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static List<String> headSeq(String head, int start, int end){
        List<String> res = new ArrayList<>();
        IntStream.range(start, end).forEach(i->res.add(head+i));
        res.add(head+end);
        return res;
    }

    public static String headSeq(String head, int start, int end, String delim){
        return headSeq(head, start, end).stream().collect(Collectors.joining(delim));
    }


    public static double scale(double res){
        return res == 0 ? res : new BigDecimal(res).setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }


}
