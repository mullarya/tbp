package com.accendo.math.tbp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by anna.myullyari on 3/11/18.
 */
public class DatDataParser implements RowDataParser<String, Pair<String, String>> {

    Integer pSize = 30;
    Integer pCount;

    @Override
    public Pair<String, String> parse(String dataRow) {
        if(pCount == null) {
            return fixedSizePfx(dataRow);
        }
        else {
            return fixedCountPfx(dataRow);
        }
    }

    private Pair<String, String> fixedSizePfx(String dataRow) {
        final String row = StringUtils.removeAll(dataRow.substring(pSize), " ");
        String head = dataRow.substring(0, pSize);
        return Pair.of(head, row);
    }

    private Pair<String, String> fixedCountPfx(String dataRow) {
        String[] str = StringUtils.split(dataRow, " \t");
        String head = Arrays.stream(str, 0, pCount).collect(Collectors.joining(" "));
        String row = Arrays.stream(str, pCount, str.length).collect(Collectors.joining());
        return Pair.of(head, row);
    }


    public DatDataParser withPrefixCount(int count){
        if(count > 0){
            pCount = count;
        }
        return this;
    }

    public DatDataParser withPrefixSize(int count){
        if(count > 0){
            pSize = count;
        }
        return this;
    }

    public String markerHeader(){
        return pCount != null ? OutputUtils.headSeq("h", 1, pCount, " ") : pSize+"-char-head";
    }

}


