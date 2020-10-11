package com.accendo.math.tbp;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MarkovEntropyProcessorTest {

    @Test
    public void getMarkovEntropy0() {

        List<Double> res = MarkovEntropyProcessor.getMarkovEntropy("222");
        assertEquals(3, res.size());
        res.stream().forEach(d->assertEquals(Double.valueOf(0), d));

        res = MarkovEntropyProcessor.getMarkovEntropy("212");
        assertEquals(3, res.size());
    }

    @Test
    public void getMarkovEntropy() {

        List<Double> res = MarkovEntropyProcessor.getMarkovEntropy("212");
        assertEquals(3, res.size());

    }

    @Test
    public void charAt() {
    }


}