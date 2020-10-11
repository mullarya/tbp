package com.accendo.math.tbp.entropy;

import com.accendo.math.tbp.bitops.CharCounter;
import com.accendo.math.tbp.bitops.BitMapBitSet;
import com.accendo.math.tbp.bitops.BitMapIntSet;
import com.accendo.math.tbp.bitops.StringSet;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.accendo.math.tbp.CalcUtil.frequency;

public class Markov extends Entropy<Double> {

    int bitMode = 1;
    public static final int BIT_SET = 1;
    public static final int INT_SET = 2;
    public static final int STR_SET = 0;
    public boolean compare = false;

    public Markov(String marker, int border,  int bitMode) {
        super(marker, 1, border);
        this.bitMode = bitMode;
    }

    @Override
    public List<Double> derive(String row) {
        row = row.substring(0, Math.min(border, row.length()));
        border  = row.length();
        return getMarkovEntropy(row);

    }

    List<Double> calculate(CharCounter counter){
        return IntStream.range(minLen, border)
                .mapToObj(i -> markovE(i, counter))
                .map(Pair::getValue)
                .collect(Collectors.toList());
    }


    public  List<Double> getMarkovEntropy(String row) {
        switch (bitMode){
            case BIT_SET:
                return calculate(new BitMapBitSet(row));
            case INT_SET:
                return calculate(new BitMapIntSet(row));
            default:
                return calculate(new StringSet(row));
        }
    }

    Pair<Integer, Double> markovE(int l, CharCounter counter){ //sum(p(i)*p(i+l)*log(p(i+l))
        DoubleAdder sum = new DoubleAdder();
        Map<Character, Integer> aCounts = counter.counts();
        Map<String, Integer> aForkCounts = counter.forkCounts(l);

        //validate(aCounts, aForkCounts);
        aForkCounts.entrySet().forEach(e-> {
            char ci = e.getKey().charAt(0);
            int counts = aCounts.get(ci);
            double pI = frequency(counts, border);

            int forkCounts = e.getValue();
            double pIJ = frequency(forkCounts, counts);
            sum.add(pI * pIJ * Math.log(pIJ));
        });

        return  Pair.of(l, -sum.doubleValue());

    }

    private void validate(Map<Character, Integer> aCounts, Map<String, Integer> aForkCounts) {
        int aCheck = aCounts.values().stream().collect(Collectors.summingInt(i->i));
        assert border == aCheck;
        for(char c: aCounts.keySet()){
            int fCheck = aForkCounts.entrySet().stream().filter(e->e.getKey().startsWith(c+"-")).collect(Collectors.summingInt(e->e.getValue()));
            assert fCheck == aCounts.get(c);
        }
    }

    public Markov withCompare(){
        this.compare = true;
        return this;
    }

    public void stats(){
        switch (bitMode){
            case BIT_SET: BitMapBitSet.stats(); break;
            case INT_SET: BitMapIntSet.stats(); break;
        }
    }

}
