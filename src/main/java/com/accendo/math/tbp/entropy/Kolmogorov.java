package com.accendo.math.tbp.entropy;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.accendo.math.tbp.CalcUtil.addTail;
import static com.accendo.math.tbp.CalcUtil.entropy;

public class Kolmogorov extends Entropy<Double> {


    public Kolmogorov(int border) {
        super("kmg", 1, border);
    }

    @Override
    public List<Double> derive(String row) {
        return  IntStream.range(1, border)
                .mapToObj(i -> {
                    String fullR = addTail(row, i);
                    return entropy(substringCounts(fullR, i), fullR.length()/i);
                })
                .collect(Collectors.toList());
    }

    // calculate frequencies of substrings of a given length
    protected static Collection<Integer> substringCounts(String row, int substrLength){
        String sub;
        Map<String, Integer> counts = Maps.newHashMap();
        for(int i = 0; i < row.length(); i+= substrLength ){
            sub = row.substring(i, Math.min(row.length(), i+substrLength));
            if(sub.length() == substrLength) {
                Integer count = counts.get(sub);
                counts.put(sub, count == null ? 1 : count + 1);
            }
        }
        return counts.values();
    }

}
