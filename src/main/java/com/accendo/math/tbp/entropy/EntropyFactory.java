package com.accendo.math.tbp.entropy;

import com.accendo.math.tbp.ZipUtil;

public class EntropyFactory {

    public static Entropy getEntropy(String marker, int minLen, int border){
        switch (marker){
            case "kmg": return new Kolmogorov(border);
            case "zzz": return new Zip(marker, minLen, border, ZipUtil::archSize);
            case "dfl": return new Zip(marker, minLen, border, ZipUtil::deflate);
            case "mkv": return  new Markov(marker, 256, Markov.STR_SET);
            case "mkb": return  new Markov(marker, 256, Markov.BIT_SET);
            case "mki": return  new Markov(marker, 256, Markov.INT_SET);
            default: return null;
        }
    }
}
