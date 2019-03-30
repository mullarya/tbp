package com.accendo.math.tbp;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Created by anna.myullyari on 1/9/18.
 */
public abstract class DataProcessor<T> implements Runnable{

    DatDataParser parser;
    String filePath;

    int trace = 300000;
    int limit = -1;

    Map<String, String> zipLen;

    //protected int totalLines = 0;


    public void process(String[] args){
        long t = System.currentTimeMillis();
        if(args.length < 1){
            throw new RuntimeException("Please specify directory to process .dat files");
        }
        File f = new File(args[0]);
        if(!f.exists()){
            throw new RuntimeException(args[0]+" file do not exist");
        }
        int len = 1;
        if(f.isDirectory()) {
            String[] list = f.list((dir, name) -> name.endsWith(".dat"));
            for (String file : list) {
                filePath = args[0] + file.substring(0, file.length() - 4);
                System.out.println(getClass().getSimpleName() + " starting " + file);
                initArgs(args);
                run();
            }
            len = list.length;
        }
        else{
            String file = f.getPath();
            filePath = file.endsWith(".dat") ? file.substring(0, file.length() - 4) : file;
            System.out.println(getClass().getSimpleName() + " starting " + file);
            initArgs(args);
            run();
        }
        t = System.currentTimeMillis() - t;
        System.out.printf("%s finished processing %d files in %d mls.", getClass().getSimpleName(), len, t);
    }

    protected void initArgs(String[] args){
        Map<String, String> settings = Maps.newHashMap();
        Arrays.stream(args).filter(s -> s.startsWith("-")).forEach(s -> settings.put(s.substring(0, 2), s.substring(2)));

        limit = getIntKey(settings.get("-l"), -1); // -lK  if specified, first K lines from the file will be processed. ex: -l20  first 20 lines (debug option)
        trace = getIntKey(settings.get("-t"), 100000); // -tK   option will output processing info on each K row, K should be number. If -t0 is specified, no console tracing
        parser = new DatDataParser(getIntKey(settings.get("-c"), 30)); // -cK  option to specify coordinates length in each line of '.dat' file. example -c30
    }

    private int getIntKey(String value, int def ){
        return value == null ? def : Integer.parseInt(value);
    }

    public int traverseFile(){
        final AtomicInteger counter  = new AtomicInteger();
        long time = System.currentTimeMillis();
        String fileName = filePath+".dat";
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream
                    .limit(limit > 0 ? limit : Integer.MAX_VALUE)
                    .forEach(line->{
                        int cnt = counter.getAndIncrement();
                        if (trace > 0 && (cnt % trace) == 0 && cnt > 0){
                            System.out.println("trace "+cnt);
                        }
                        Pair<String, String> data = parser.parse(line);
                        fillZip(data);
                        processRow(data.getValue(), data.getKey(), line);
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        System.out.println(counter.get()+" rows processed in millisec: "+time);
        return counter.get();
    }

    protected void fillZip(Pair<String, String> data){
        if(zipLen != null) {
            zipLen.put(data.getKey(), ZipUtil.allArch(data.getValue()));
        }
    }


    protected String output() throws IOException {
        String file = filePath+getSubsetMarker();
        System.out.println("Saving to file: "+file);

        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(
                Paths.get(file)))) {
            output(pw);
        }
        return file;
    }

    abstract T processRow(String dataRow, String marker, String row);
    abstract String getSubsetMarker();
    abstract void output(PrintWriter writer);

}
