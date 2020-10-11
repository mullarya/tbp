package com.accendo.math.tbp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.accendo.math.tbp.CalcUtil.entropy;


/**
 * Created by anna.myullyari on 3/10/18.
 */
public class EntropyProcessor extends DataProcessor<List<Double>>{

    public static final String EXT = ".ent";

    protected List<Pair<String, List<Double>>> all = Lists.newLinkedList();
    protected int divider = 4;

    public static String format = "%8.4f";

    private int headLength = 0;

    private MarkovEntropyProcessor markov;

    /**
     * Calculate E1, E2, ... En for the given row
     * @param row data row where each character is meaningful (no spaces, no delimiters: 12322233)
     * @param marker - any String to mark a row in set, from the data file 12 characters formatted (i,j).
     * @return  List of calculated entropies for the given row, E1 - for substring of size 1, E2 - substrings of size 2, the longest substring size is row.length/4+1
     * i.e for the data string of length 110 En will be calculated for substring of size 28
     */
    @Override
    public List<Double> processRow(String row, String marker, String fullRow) {
        int dt = row.length() % divider == 0 ? 1 : 2;
        int border = row.length()/divider + dt;
        headLength = Math.max(headLength, border);
        List<Double> res = Lists.newLinkedList();
        if(markov !=  null){
            res.addAll(markov.processRow(row, marker, fullRow));
        }
        res.addAll(chainEntropy(row, border));
        all.add(Pair.of(marker, res));
        return res;
    }

    public static List<Double> chainEntropy(String row, int border){
        // if mod == 0, then do not add tail...
        return  IntStream.range(1, border)
                .mapToObj(i -> {
                    String fullR = addTail(row, i);
                    return entropy(counts(fullR, i), fullR.length()/i);
                })
                .collect(Collectors.toList());
    }


    @Override
    public String getSubsetMarker() {
        return EXT;
    }

    @Override
    void output(PrintWriter file, Pair<String, List<Double>> entry) {

    }

    @Override
    public void output(PrintWriter file){
        all.stream().map(p -> formatEntry(p)).forEach(file::println);
    }

    @Override
    public DataProcessor<List<Double>> newInstance(Map<String, String> settings) {
        return new EntropyProcessor();
    }

    @Override
    protected List<String> getHeader() {
        List<String> res = OutputUtils.headSeq("ss", 1, headLength);
        if (additional != null) {
            res.addAll(ZipUtil.allArchHead());
        }
        if (markov != null){
            res.addAll(markov.getHeader());
        }
        return res;
    }

    public  String formatEntry(Pair<String, List<Double>> entry){
        String res =  entry.getKey()+delim+
                entry.getValue().stream().map(d -> String.format(format, OutputUtils.scale(d))).collect(Collectors.joining(delim));
        return additional == null ? res : res+delim+ additional.get(entry.getKey());
    }

    // calculate frequencies of substrings of a given length
    protected static Collection<Integer> counts(String row, int substrLength){
        String sub;
        Map<String, Integer> counts = Maps.newHashMap();
        for(int i = 0; i < row.length(); i+= substrLength ){
            sub = row.substring(i, Math.min(row.length(), i+substrLength));
            if(sub.length() == substrLength) {
                Integer count = counts.get(sub);
                counts.put(sub, count == null ? 1 : count + 1);
            }
        }
        return counts.values();
    }

    static String addTail(String row, int dt) {
        int mod = row.length() % dt;
        return mod == 0 ? row : row+row.substring(0, dt-mod);
    }

    @Override
    public void run(){
        try {
            all = Lists.newLinkedList();
            additional = new HashMap<>(); //this will trigger compress calculation
            traverseFile();
            output();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


}
