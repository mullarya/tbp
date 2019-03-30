package com.accendo.math.tbp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by anna.myullyari on 3/11/18.
 */
public class DatDataParser implements RowDataParser<String, Pair<String, String>> {

    int prefixL = 30;

    @Override
    public Pair<String, String> parse(String dataRow) {
        final String row = StringUtils.removeAll(dataRow.substring(prefixL), " ");
        String head = dataRow.substring(0, prefixL);
        return Pair.of(head, row);
    }

    public DatDataParser(int prefixL) {
        this.prefixL = prefixL;
    }

}


