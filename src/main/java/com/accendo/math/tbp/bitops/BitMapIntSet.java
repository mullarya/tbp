package com.accendo.math.tbp.bitops;

import com.sun.tools.javac.util.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BitMapIntSet implements CharCounter {

    private Map<Character, Set<Integer>> map = new HashMap<>();
    int size;

    static long init = 0;
    static long count = 0;

    public BitMapIntSet(String s){
        long t = System.currentTimeMillis();
        Assert.checkNonNull(s);
        Assert.check(s.length() > 0);
        size = s.length();
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            Set<Integer> mask = map.get(c);
            if(mask == null){
                mask = new HashSet<>();
                map.put(c, mask);
            }
            mask.add(i);
        }
        init+= System.currentTimeMillis() - t;
    }

    @Override
    public int countChar(char c){
        Set<Integer> mask = map.get(c);
        return mask == null ? 0 : mask.size();
    }

    @Override
    public int forkCountAperiodic(char left, char right, int lag){
        Set<Integer> lMask = map.get(left);
        if(lMask == null){
            return 0;
        }
        Set<Integer> rMask = map.get(right);
        if(rMask == null){
            return 0;
        }

        return 0;

    }

    @Override
    public int forkCountPeriodic(char left, char right, int lag){
        long t = System.currentTimeMillis();
        Set<Integer> lMask = map.get(left);
        Set<Integer> rMask = map.get(right);

        int res =  intersectWithShift(rMask, lMask, lag, size);
        count+= System.currentTimeMillis() - t;
        return res;
        //return intersect.size();
    }

    @Override
    public Set<Character> allChars() {
        return map.keySet();
    }

    @Override
    public Map<Character, Integer> counts() {
        return null;
    }

    public static int intersectWithShift(Set<Integer> rMask, Set<Integer> lMask, int lag, int size) {
        int dt;
        Set<Integer> core, it;

        if(rMask.size() < lMask.size()){
            dt = -lag;
            core = lMask;
            it = rMask;

        }
        else{
            dt = lag;
            core = rMask;
            it = lMask;
        }
        AtomicInteger cnt = new AtomicInteger();
         it.stream().forEach(i-> {
             if (core.contains(modIndex(i + dt, size))) {
                 cnt.incrementAndGet();
             }
         });
        return cnt.get();
    }

    private static int modIndex(int ind, int size){
        return ind < 0 ? size+ind : ind < size ? ind : ind - size;
    }

    public static void stats(){
        System.out.println("BMS (mls) init:"+ init+" ; count:"+count);
    }

}
