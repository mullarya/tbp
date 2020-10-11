package com.accendo.math.tbp.bitops;


import com.accendo.math.tbp.entropy.CharCounter;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BitMapBitSet implements CharCounter {

    private Map<Character, BitSet> map;
    int size;

    static long init = 0;
    static long count = 0;

    public BitMapBitSet(String s){
        size = s.length();

        long t = System.currentTimeMillis();
        map = new HashMap<>();
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            BitSet mask = map.get(c);
            if(mask == null){
                mask = new BitSet();
                map.put(c, mask);
            }
            mask.set(i, true);
        }
        init+= System.currentTimeMillis() - t;

    }

    public  Map<Character, BitSet> initLong(String s){
        long k = System.currentTimeMillis();
        Map<Character, long[]> map = new HashMap<>();
        int sz = s.length()/64 +1;
        for(int i = 0; i < s.length(); i++){

            char c = s.charAt(i);
            long[] mask = map.get(c);
            if(mask == null){
                mask = new long[sz];
                map.put(c, mask);
            }
            int ind = i/64;
            int dt = i % 64;

            mask[ind] = mask[ind] | BigInteger.ONE.shiftLeft(dt).longValue();

        }

        Map<Character, BitSet> res = new HashMap<>();
        for(char c: map.keySet()){
            res.put(c, BitSet.valueOf(map.get(c)));
        }
        //vo+= (System.currentTimeMillis() - k);
        return res;
    }

    public static Map<Character, BitSet> initSet(String s){
        Map<Character, BitSet> map = new HashMap<>();
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            BitSet mask = map.get(c);
            if(mask == null){
                mask = new BitSet();
                map.put(c, mask);
            }
            mask.set(i, true);
        }
        return map;
    }

    @Override
    public int countChar(char c){
        BitSet mask = map.get(c);
        return mask == null ? 0 : mask.cardinality();
    }

    @Override
    public int forkCountAperiodic(char left, char right, int lag){
        BitSet lMask = map.get(left);
        if(lMask == null){
            return 0;
        }
        BitSet rMask = map.get(right);
        if(rMask == null){
            return 0;
        }
        BitSet shift = lMask.get(0, size - lag);
        shift.and(rMask);
        return shift.cardinality();

    }

    @Override
    public int forkCountPeriodic(char left, char right, int lag){

        BitSet lMask = map.get(left);
        if(lMask == null){
            return 0;
        }
        BitSet rMask = map.get(right);
        if(rMask == null){
            return 0;
        }
        long t = System.currentTimeMillis();
        BitSet shift = shiftRightPeriodic(lMask, lag, size);
        count+= System.currentTimeMillis() - t;
        shift.and(rMask);
        int res =  shift.cardinality();
        return res;

    }

    @Override
    public Set<Character> allChars() {
        return map.keySet();
    }


    //TODO implement with Long shifts
    public static BitSet shiftRightPeriodic(BitSet mask, int lag, int size){
        /*
        long[] arr = mask.toLongArray();
        int ind = lag / 64;
        int dt = lag % 64;
        long[] shifted = new long[size/64+1];
        for (int i = ind; i < arr.length; i++){
            shifted[i-ind] = arr[i] >> dt;
        }
        for(int i = 0; i < ind; i++){
            shifted[i+ind] = arr[i] >> dt;
        }
        return BitSet.valueOf(shifted);
        */

        BitSet shift = mask.get(size-lag, size) ;
        for(int i = lag; i < size; i++){
            shift.set(i, mask.get(i-lag));
        }
        return shift;

    }

    public static void stats(){
        System.out.println("BMBS (mls) init:"+ init+" ; count:"+count);
    }

    public static void main (String[] args){
        System.out.println(new BitMapBitSet("2122111111555").map);
    }
}
