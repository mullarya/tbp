package com.accendo.math.tbp;

import com.accendo.math.tbp.entropy.CharCounter;
import com.accendo.math.tbp.bitops.BitMapBitSet;
import com.accendo.math.tbp.bitops.BitMapIntSet;
import com.accendo.math.tbp.bitops.BitMapLong;
import org.junit.Test;

import static org.junit.Assert.*;

public class BitUtilsTest {

    @Test
    public void bitMap() {

        BitMapLong map = new BitMapLong("123");
        assertEquals(Long.valueOf(0b100), map.get('1'));

        map = new BitMapLong("1231");
        assertEquals(Long.valueOf(0b1001), map.get('1'));
        assertEquals(Long.valueOf(0b0100), map.get('2'));

        map = new BitMapLong("212311");
        assertEquals(Long.valueOf(0b010011), map.get('1'));
        assertEquals(Long.valueOf(0b101000), map.get('2'));
    }

    @Test
    public void forkCount(){

        CharCounter map = new BitMapBitSet("123");
       // assertEquals(1, map.forkCountPeriodic('1', '2', 1));

        map = new BitMapIntSet("212311");
        // 1: 010011
        // 2: 101000
        // shift-1  01001
        assertEquals(2, map.forkCountPeriodic('1', '2', 1));
        assertEquals(1, map.forkCountPeriodic('3', '1', 1));
        assertEquals(0, map.forkCountPeriodic('1', '3', 1));
        assertEquals(1, map.forkCountPeriodic('1', '1', 1));

    }

    @Test
    public void shiftRightAndFill(){
        assertEquals(0b1000, BitMapLong.shiftRightPeriodic(Long.valueOf(0b0001), 1, 4));
        assertEquals(0b0100, BitMapLong.shiftRightPeriodic(Long.valueOf(0b0001), 2, 4));

        assertEquals(0b1011000, BitMapLong.shiftRightPeriodic(Long.valueOf(0b1000101), 3, 7));
    }
}