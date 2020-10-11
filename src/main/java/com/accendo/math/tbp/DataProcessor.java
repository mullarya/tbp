package com.accendo.math.tbp;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by anna.myullyari on 1/9/18.
 *    0     0  -.00049  -.00049     18.603    2
 */
public abstract class DataProcessor<T> implements Runnable{

    DatDataParser parser;
    String filePath;
    String marker;

    int trace = 300000;
    int limit = -1;

    Map<String, String> additional;

    int rowLen = 10000;
    int minLen = 1;
    protected String delim = " ";
    PrintWriter output;


    //protected int totalLines = 0;


    public void process(String[] args) throws InterruptedException, IOException {String filePath;
        long t = System.currentTimeMillis();
        if(args.length < 1){
            throw new RuntimeException("Please specify directory to process .dat files");
        }
        if(args.length == 1 && (args[0].equals("help") || args[0].equals("-h"))){
            help();
            return;
        }
        File f = new File(args[0]);
        if(!f.exists()){
            throw new RuntimeException(args[0]+" file do not exist");
        }
        Map<String, String> settings = getSettings(args);
        Arrays.stream(args).filter(s -> s.startsWith("-")).forEach(s -> settings.put(s.substring(0, 2), s.substring(2)));

        int len = 1;
        if(f.isDirectory()) {
            len = processDir(f, settings);
        }
        else{
            String file = f.getPath();
            filePath = file.endsWith(".dat") ? file.substring(0, file.length() - 4) : file;
            initAndRun(filePath, settings);
        }
        t = System.currentTimeMillis() - t;
        System.out.printf("%s finished processing %d files in %d mls.", getClass().getSimpleName(), len, t);
    }

    public Map<String, String> getSettings(String[] args) {
        Map<String, String> settings = Maps.newHashMap();
        Arrays.stream(args).filter(s -> s.startsWith("-")).forEach(s -> settings.put(s.substring(0, 2), s.substring(2)));
        return settings;
    }

    protected void initArgs(String filePath, Map<String, String> settings) throws IOException {
        this.filePath = filePath;

        limit = getIntKey(settings.get("-l"), -1); // -lK  if specified, first K lines from the file will be processed. ex: -l20  first 20 lines (debug option)
        trace = getIntKey(settings.get("-t"), 100000); // -tK   option will output processing info on each K row, K should be number. If -t0 is specified, no console tracing

        parser = new DatDataParser()
                .withPrefixCount(getIntKey(settings.get("-c"), -1)) // -c6 means first 6 space-separated numbers are prefix
                .withPrefixSize(getIntKey(settings.get("-s"), -1)); // -s30 means first 30 characters are prefix



        int nameRowLwn = getIntKey(settings.get("-r"), -1);
        if(nameRowLwn > 0){
            rowLen = Integer.parseInt(filePath.substring(nameRowLwn));
        }
        minLen = getIntKey(settings.get("-m"), 1);
        marker = settings.get("-e");

    }

    public static void help(){
        printFlag("-l", "-lK if specified, first K lines from the file will be processed. ex: -l20  first 20 lines (debug option)");
        printFlag("-t","-tK option will output processing info on each K row, K should be number. If -t0 is specified, no console tracing. Default 100000");
        printFlag("-e","-e<Str> option to specify entropy algorithm to run. Options: zzz - zip,  dfl - String.deflate, kmg - Kolmogorov, mkv - Markov, string impl, mkb - Markov, bitset, mki - Markov int impl");
        printFlag("-m","-mK minimal length (or start) of string");
        printFlag("-r","-rK max row length to process");
        printFlag("-c","-cK number of tokens in prefix. -c6 means first 6 space-separated numbers are prefix");
        printFlag("-s","-sK length of prefix. -s30 means first 30 characters are prefix");
    }

    private static void printFlag(String flag, String descr){
        System.out.println(flag+": "+ descr);
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
                            stats(cnt);
                        }
                        Pair<String, String> data = parser.parse(line);
                        fillAdditional(data, delim);
                        T res = processRow(data.getValue(), data.getKey(), line);
                        if (output != null){
                            output(output, Pair.of(data.getKey(), res));
                        }
                    });
            if(output != null){
                output.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        System.out.println(counter.get()+" rows processed in millisec: "+time);
        return counter.get();
    }

    private int processDir(File f, Map<String, String> settings) throws IOException, InterruptedException {
        String[] list = f.list((dir, name) -> name.endsWith(".dat"));
        ExecutorService runner = null;
        CountDownLatch tracer = null;
        int awaitTime = getIntKey(settings.get("-p"), -1);
        if(list.length > 0 && awaitTime > 0){
            runner = Executors.newFixedThreadPool(list.length);
            tracer = new CountDownLatch(list.length);
        }
        for (String file : list) {
            filePath = f.getName()+File.separator + file.substring(0, file.length() - 4);
            if(runner != null){
                runner.submit(new FileDataRunner(tracer, newInstance(settings), filePath, settings));
            }
            else{
                initAndRun(filePath, settings);
            }

        }
        if(tracer != null){
            tracer.await(awaitTime, TimeUnit.HOURS);
        }
        return list.length;
    }

    public void stats(int cnt) {
        System.out.println(filePath+" trace "+cnt);
    }

    protected void fillAdditional(Pair<String, String> data, String delim){
        if(additional != null) {
            StringBuilder head = new StringBuilder();
            String additionalData = ZipUtil.allArch(data.getValue(), head, delim);
            additional.put("header", head.toString());
            additional.put(data.getKey(), additionalData);
        }
    }


    protected String output() throws IOException {
        String file = filePath+getSubsetMarker();
        System.out.println("Saving to file: "+file);

        try(PrintWriter pw = new PrintWriter(Files.newBufferedWriter(
                Paths.get(file)))) {
            pw.println(parser.markerHeader()+" "+getHeader().stream().collect(Collectors.joining(delim)));
            output(pw);
        }
        return file;
    }

    protected PrintWriter streamOutput() throws IOException {
        String file = filePath+getSubsetMarker();
        System.out.println("Saving to file: "+file);

        PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(file)));
        pw.println(parser.markerHeader()+" "+getHeader().stream().collect(Collectors.joining(delim)));
        return pw;
    }

    abstract T processRow(String dataRow, String marker, String row);
    abstract String getSubsetMarker();
    abstract void output(PrintWriter file, Pair<String, T> entry);
    abstract void output(PrintWriter writer);
    public abstract DataProcessor<T> newInstance(Map<String, String> settings) throws IOException;


    public  void initAndRun(String filePath, Map<String, String> settings) throws IOException {

        System.out.println(getClass().getSimpleName() + " starting " + filePath);
        initArgs(filePath, settings);
        traverseFile();
    }

    protected List<String> getHeader(){
        return new ArrayList<>();
    }


    static class FileDataRunner implements Runnable{
        CountDownLatch trace;
        DataProcessor processor;
        String file;
        Map<String, String> settings;

        public FileDataRunner(CountDownLatch trace, DataProcessor processor, String file, Map<String, String> settings) {
            this.trace = trace;
            this.processor = processor;
            this.file = file;
            this.settings = settings;
        }

        @Override
        public void run() {
            try {
                processor.initAndRun(file, settings);
            }
            catch(Exception e){
                e.printStackTrace();
            }
            trace.countDown();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        new EntropyProcessor().process(args);
    }


}
