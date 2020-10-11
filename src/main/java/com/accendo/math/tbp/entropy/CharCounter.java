package com.accendo.math.tbp.entropy;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface CharCounter {

    int countChar(char c);

    int forkCountAperiodic(char left, char right, int lag);

    int forkCountPeriodic(char left, char right, int lag);

    Set<Character> allChars();

    default Map<Character, Integer> counts(){
        return allChars().stream().collect(Collectors.toMap(c->c, this::countChar));
    }

    default Map<String, Integer> forkCounts(int lag){
        Set<Character> all = allChars();
        Map<String, Integer> res = Maps.newHashMap();
        all.forEach(i-> all.stream().forEach(j->{
            int fCount = forkCountPeriodic(i, j, lag);
            if(fCount > 0){
                String key = getForkKey(i, j);
                res.put(key, res.getOrDefault(key, 0)+fCount);
            }
        }));
        return res;
    }

    default String getForkKey(char i, char j){
        return i+"-"+j;
    }

    default Pair<Character, Character> getKey(char i, char j){
        return Pair.of(i, j);
    }
}
