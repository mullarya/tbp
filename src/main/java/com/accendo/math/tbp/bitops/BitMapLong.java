package com.accendo.math.tbp.bitops;

import com.sun.tools.javac.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BitMapLong implements CharCounter {

    private Map<Character, Long> map;
    int size;

    public BitMapLong(String s){
        Assert.checkNonNull(s);
        Assert.check(s.length() > 0);
        long binPos = 1 << (s.length() - 1);
        map = new HashMap<>();
        size = s.length();
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            Long mask = map.get(c);
            map.put(c, mask == null ? binPos : mask | binPos);
            binPos /= 2;
        }
    }



    @Override
    public int countChar(char c){
        Long mask = map.get(c);
        return mask == null ? 0 : Long.bitCount(mask);
    }

    @Override
    public int forkCountAperiodic(char left, char right, int lag){
        Long lMask = map.get(left);
        if(lMask == null || lMask == 0){
            return 0;
        }
        Long rMask = map.get(right);
        if(rMask == null || rMask == 0){
            return 0;
        }
        long tmp = lMask >> lag;
        return Long.bitCount(tmp & rMask);

    }

    @Override
    public int forkCountPeriodic(char left, char right, int lag){
        Long lMask = map.get(left);
        if(lMask == null || lMask == 0){
            return 0;
        }
        Long rMask = map.get(right);
        if(rMask == null || rMask == 0){
            return 0;
        }
        return Long.bitCount(shiftRightPeriodic(lMask, lag, size) & rMask);

    }

    @Override
    public Set<Character> allChars() {
        return map.keySet();
    }

    public static long shiftRightPeriodic(long mask, int lag, int size){
        //prepare tail mask
        long tail = ((1 << (lag)) - 1 ) ;
        tail = mask & tail;
        //shift tail bits left
        tail = tail << (size-lag);
        //shift mask right
        mask = mask >> lag;
        //append former tail as a head
        return tail | mask;
    }

    public Long get(char c) {
        return map.get(c);
    }
}
