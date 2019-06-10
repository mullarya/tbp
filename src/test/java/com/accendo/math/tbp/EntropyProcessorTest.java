package com.accendo.math.tbp;

import org.junit.Test;

import static org.junit.Assert.*;

public class EntropyProcessorTest {


    @Test
    public void addTail() {

        String s = "12345678";

        assertEquals(s, EntropyProcessor.addTail(s, 1));
        assertEquals(s, EntropyProcessor.addTail(s, 2));
        assertEquals("123456781", EntropyProcessor.addTail(s, 3));
        assertEquals("12345678", EntropyProcessor.addTail(s, 4));
        assertEquals("1234567812", EntropyProcessor.addTail(s, 5));
        assertEquals("123456781234", EntropyProcessor.addTail(s, 6));




    }
}