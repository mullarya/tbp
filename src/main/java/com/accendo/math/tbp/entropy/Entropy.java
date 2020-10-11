package com.accendo.math.tbp.entropy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Entropy<N extends Number> {

    String marker;
    int minLen;
    int border = 500;

    public Entropy(String marker, int minLen, int border) {
        this.marker = marker;
        this.minLen = minLen;
        this.border = border;
    }

    public void setup(Map<String, String> settings){

    }

    public List<String> headers() {
        return IntStream.range(minLen, border).mapToObj(i->marker+i).collect(Collectors.toList());
    }

    public abstract List<N> derive(String row);

    public String getMarker() {
        return marker;
    }

    public void stats(){

    }
}
