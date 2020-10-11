package com.accendo.math.tbp;

import java.util.Collection;
import java.util.concurrent.atomic.DoubleAdder;

public class CalcUtil {

    public static String addTail(String row, int dt) {
        int mod = row.length() % dt;
        return mod == 0 ? row : row+row.substring(0, dt-mod);
    }

    public static Double entropy(Collection<Integer> frequency, int n){ //sum()
        DoubleAdder sum = new DoubleAdder();
        frequency.stream().map(
                i -> sumEntry(i, n))
                .forEach(sum::add);
        return -sum.doubleValue();
    }

    public static double sumEntry(int frequencyI, int n){
        double pI = frequency(frequencyI, n);
        return eS(pI);
    }


    public static double frequency(int count, int n){
        return (double)count/n;
    }

    public static double eS(double p){
        return p* Math.log(p);
    }


}
