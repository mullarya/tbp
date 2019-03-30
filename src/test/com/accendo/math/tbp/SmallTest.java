package com.accendo.math.tbp;


import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SmallTest{

    static String format = "%8.4f";
    static String intformat = "%8d";

    @Test
    public void testFormat() {
        List<Double> a1 = Arrays.asList(0.0, 0.0, 0.1327, 0.0, 0.0, 0.2237, 0.2449);
        List<Double> a2 = Arrays.asList(0.056, 0.098, 0.2645, 0.1679, 0.1985, 0.4438, 0.4851, 0.536);

        assertEquals("  0.0000  0.0000  0.1327  0.0000  0.0000  0.2237  0.2449", toString(a1));
        assertEquals("  0.0560  0.0980  0.2645  0.1679  0.1985  0.4438  0.4851  0.5360", toString(a2));

        List<String> target = Lists.newArrayList();
        target.add("    pfx ");

        String intn = "3, 555,987654,   1, 2, 0";
        Arrays.stream(intn.split(","))
                .map(s -> formatInt(s))
                .forEachOrdered(target::add);

        String res = target.stream().collect(Collectors.joining(""));
        assertEquals("    pfx        3     555  987654       1       2       0", res);

        // .collectors(Collectors.toList());

    }

    public static String toString(List<Double> a){
        return a.stream().map(d -> String.format(format, d)).collect(Collectors.joining(""));
    }

    public static String formatInt(String s){
        return String.format(intformat, Integer.parseInt(s.trim()));

    }
}