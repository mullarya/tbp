package com.accendo.math.tbp;


import com.accendo.math.tbp.bitops.CharCounter;
import com.accendo.math.tbp.bitops.BitMapFactory;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.IntStream;

import static com.accendo.math.tbp.CalcUtil.frequency;


/**
 * characters are 1,2,3. length 100 or  256 or whatever, prefix changed a bit..
 * 1. Calc Sannon ent.
 */

public class MarkovEntropyProcessor  extends EntropyProcessor {

    private int eLenght = 0;

    @Override
    public List<Double> processRow(String row, String marker, String fullRow) {
        List<Double> res = //getMarkovEntropy(row);
                getMarkovEntropyBit(row);
        eLenght = Math.max(eLenght, res.size());
        all.add(Pair.of(marker, res));
        return res;
    }

    public static List<Double> getMarkovEntropy(String row) {
        List<Double> res = new LinkedList<>();
        IntStream.range(0, row.length())
                .mapToObj(i -> markovE(row, i)).forEach(d-> res.add(d.getKey(),d.getValue()));
        return res;
    }

    public static List<Double> getMarkovEntropyBit(String row) {
        List<Double> res = new LinkedList<>();
        CharCounter bitMap = BitMapFactory.toMap(row);
        IntStream.range(0, row.length())
                .mapToObj(i -> markovE(row, i, bitMap)).forEach(d-> res.add(d.getKey(),d.getValue()));
        return res;
    }

    static Pair<Integer, Double> markovE(String row,  int l, CharCounter bitMap){ //sum(p(i)*p(i+l)*log(p(i+l))
        DoubleAdder sum = new DoubleAdder();
        for(int i = 0; i < row.length(); i++) {

            int counts = bitMap.countChar(row.charAt(i));
            double pI = frequency(counts, row.length());

            int forkCounts = bitMap.forkCountPeriodic(row.charAt(i), charAt(row, i+l+1), l+1);
            double pIJ = frequency(forkCounts, row.length());
            sum.add(pI *  pIJ * Math.log(pIJ));
        }
        return  Pair.of(l, -sum.doubleValue());

    }


    static Pair<Integer, Double> markovE(String row,  int l){ //sum(p(i)*p(i+l)*log(p(i+l))
        DoubleAdder sum = new DoubleAdder();
        for(int i = 0; i < row.length(); i++) {

            int counts = StringUtils.countMatches(row, row.charAt(i));
            double pI = frequency(counts, row.length());

            int forkCounts = forkCounts(row, i, l);
            double pIJ = frequency(forkCounts, row.length());
            sum.add(pI *  pIJ * Math.log(pIJ));
        }
        return  Pair.of(l, -sum.doubleValue());

    }

    private static int forkCounts( String row,  int i, int l){//l==0 is a transition to next
        final AtomicInteger count = new AtomicInteger(0);
        char ri = charAt(row, i);
        char rj = charAt(row, i+l);
        IntStream.range(0, row.length()).forEach(ii->{
            if(charAt(row, ii) == ri && charAt(row, ii+l) == rj){
                count.addAndGet(1);
            }
        });
        return count.get();
    }

    static char charAt(String row, int ind){
        return ind < row.length() ? row.charAt(ind) : row.charAt(ind-row.length());
    }

    @Override
    public void run() {

        try {
            all = Lists.newLinkedList();
            traverseFile();
            output();
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public String getSubsetMarker() {
        return ".bent";
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        new MarkovEntropyProcessor().process(args);
    }

    @Override
    public DataProcessor<List<Double>> newInstance(Map<String, String> settings) {
        return new MarkovEntropyProcessor();
    }

    @Override
    protected List<String> getHeader() {
        return OutputUtils.headSeq("m", 1, eLenght);
    }


}
