package com.accendo.math.tbp.bitops;

import com.accendo.math.tbp.entropy.CharCounter;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringSet implements CharCounter {

    private final String base;
    private final Set<Character> unique;

    public StringSet(String base) {
        this.base = base;
         unique = base.chars().distinct().mapToObj(i->(char)i).collect(Collectors.toSet());
    }

    @Override
    public int countChar(char c) {
        return StringUtils.countMatches(base, c);
    }

    @Override
    public int forkCountAperiodic(char left, char right, int lag) {
        final AtomicInteger count = new AtomicInteger(0);
        IntStream.range(0, base.length()).forEach(ii->{
            if(charAt(base, ii) == left && charAt(base, ii+lag+1) == right){
                count.addAndGet(1);
            }
        });
        return count.get();
    }

    @Override
    public int forkCountPeriodic(char left, char right, int lag) {
        return 0;
    }

    @Override
    public Set<Character> allChars() {
        return unique;
    }


    public static char charAt(String row, int ind){
        return ind < row.length() ? row.charAt(ind) : row.charAt(ind-row.length());
    }
}
