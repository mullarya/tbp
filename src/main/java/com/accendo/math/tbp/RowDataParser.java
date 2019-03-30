package com.accendo.math.tbp;

/**
 * Created by anna.myullyari on 3/11/18.
 */
public interface RowDataParser<T, D> {

    D parse(T dataRow);
}
