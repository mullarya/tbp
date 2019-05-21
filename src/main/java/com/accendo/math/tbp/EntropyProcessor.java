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
public class EntropyProcessor extends DataProcessor<List<Double>>{

    public static final String EXT = ".ent";

    private List<Pair<String, List<Double>>> all = Lists.newLinkedList();
    private int divider = 4;

    public static String format = "%8.4f";

    /**
     * Calculate E1, E2, ... En for the given row
     * @param row data row where each character is meaningful (no spaces, no delimiters: 12322233)
     * @param marker - any String to mark a row in set, from the data file 12 characters formatted (i,j).
     * @return  List of calculated entropies for the given row, E1 - for substring of size 1, E2 - substrings of size 2, the longest substring size is row.length/4+1
     * i.e for the data string of length 110 En will be calculated for substring of size 28
     */
    @Override
    public List<Double> processRow(String row, String marker, String fullRow) {
        ArrayList<Double> res = IntStream.range(1, row.length()/divider +1)
                        .mapToObj(i -> entropy(counts(row, i), row.length(), i))
                        .collect(Collectors.toCollection(ArrayList::new));
        all.add(Pair.of(marker, res));
        return res;
    }

    @Override
    public String getSubsetMarker() {
        return EXT;
    }

    @Override
    public void output(PrintWriter file){
        all.stream().map(p -> formatEntry(p)).forEach(file::println);
    }

    public  String formatEntry(Pair<String, List<Double>> entry){
        String res =  entry.getKey()+" "+
                entry.getValue().stream().map(d -> String.format(format, d)).collect(Collectors.joining(""));
        return zipLen == null ? res : res+" "+zipLen.get(entry.getKey());
    }

    // calculate frequencies of substrings of a given length
    private Collection<Integer> counts(String row, int substrLength){
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

    private Double entropy(Collection<Integer> frequency, int rowLen, int substrLength){
        int n = rowLen/ substrLength;
        // this is fixed, do not count tail...
        //+ (rowLen % substrLength == 0 ? 0 : 1); // max number of substrings, tail < strLength is a separate substing
        return entropy(frequency, n);
    }

    public static Double entropy(Collection<Integer> frequency, int n){
        DoubleAdder sum = new DoubleAdder();
        frequency.stream().mapToDouble(
                //i -> eS(pI(i, n)))
                i -> eS(pI(i, 1)))
                .forEach(sum::add);
        double res = sum.doubleValue();
        return res == 0 ? res : -new BigDecimal(res).setScale(4, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }

    private static double pI(int count, int n){
        return (double)count/n;
    }

    private static double eS(double p){
        return p* Math.log(p);
    }

    @Override
    public void run(){
        try {
            all = Lists.newLinkedList();
            zipLen = new HashMap<>(); //this will trigger compress calculation
            traverseFile();
            output();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new EntropyProcessor().process(args);
    }



}
