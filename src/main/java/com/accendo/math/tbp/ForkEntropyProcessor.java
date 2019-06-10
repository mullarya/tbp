package com.accendo.math.tbp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Created by anna.myullyari on 3/10/18.
 */
public class ForkEntropyProcessor extends EntropyProcessor{

    public static final String EXT = ".fent";

    /**
     * Calculate E1, E2, ... En for the given row
     * @param row data row where each character is meaningful (no spaces, no delimiters: 12322233)
     * @param marker - any String to mark a row in set, from the data file 12 characters formatted (i,j).
     * @return  List of calculated entropies for the given row, E1 - for fork of size 1, E2 - fork of size 2, the longest fork size is row.length/4+1
     * i.e for the data string of length 110 En will be calculated for the fork of size 28
     */
    @Override
    public List<Double> processRow(String row, String marker, String fullRow) {
        ArrayList<Double> res = IntStream.range(1, row.length()/divider +1)
                //Q: to calculate entropy should I use orig length or cycled ? Currently using cycled
                //should i even increase gap??? what should be result of forkCounts?
                        .mapToObj(i -> {
                            String fullR = addTail(row, i);
                            return entropy(forkCounts(fullR, i), fullR.length()/*row.length()*/, i);
                        })
                        .collect(Collectors.toCollection(ArrayList::new));
        all.add(Pair.of(marker, res));
        return res;
    }

    /// this do not feel right. for random strings it will generate uneven list of integers.
    // //TODO: confirm

    private Collection<Integer> forkCounts(String row, int gap){
        String sub;
        Map<String, Integer> counts = Maps.newHashMap();
        for(int i = 0; i < row.length() - gap; i++){
            sub = row.charAt(i)+""+row.charAt(i+gap);
            Integer count = counts.get(sub);
            counts.put(sub, count == null ? 1 : count + 1);
        }
        return counts.values();
    }

    //121212

    @Override
    public String getSubsetMarker() {
        return EXT;
    }

    public static void main(String[] args) throws IOException {
        new ForkEntropyProcessor().process(args);
    }

}
