package com.accendo.math.tbp.entropy;


import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Zip extends Entropy<Integer> {

    Function<String, Integer> zipMethod;

    public Zip(String marker, int minLen, int border, Function<String, Integer> zipMethod) {
        super(marker, minLen, border);
        this.zipMethod = zipMethod;
    }

    @Override
    public List<Integer> derive(String row) {
         return IntStream.range(minLen, border)
                .mapToObj(i->getZipLength(row.substring(0, i))).collect(Collectors.toList());
    }

    protected Integer getZipLength(String str){
        return zipMethod.apply(str);
    }
}
