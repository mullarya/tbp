package com.accendo.math.tbp.bitops;

public class BitMapFactory {

    public static CharCounter toMap(String s){
        return s == null ? null : s.length() < 65 ?
                        new BitMapIntSet(s) :
                        new BitMapBitSet(s);
    }
}
