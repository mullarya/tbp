package com.accendo.math.tbp;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.accendo.math.tbp.PrefixStorage.StorageType.I;

/**
 * Created by anna.myullyari on 3/10/18.
 */
public class PrefixProcessorBase<T> extends DataProcessor<PrefixStorage<T>[]>{

    public static final String EXT = ".pfxb";

    private PrefixStorage<T>[] entriesBySize;

    //minimal subset length
    private int minLen = 3;
    private List<Integer> borders = Lists.newArrayList(3,50,80,100); // default chunks to process

    private PrintWriter fullOutput;

    private int printTop = 10;
    PrefixStorage.StorageType type = I;

    public PrefixProcessorBase(PrefixStorage.StorageType type) {
        this.type = type;
    }

    public void init(int minLen, int maxLen) {
        entriesBySize = new PrefixStorage[maxLen];
        this.minLen = minLen;
        fullOutput = null;
    }

    @Override
    public PrefixStorage<T>[] processRow(String row, String marker, String fullInput) {
        if(fullOutput == null) { // collecting data
            IntStream.range(minLen, entriesBySize.length)
                    .forEach(k -> fillMap(k - minLen, row.substring(0, k), marker));
        }
        else{ // printing data after all calculated
            final List<Integer> res = Lists.newLinkedList();
            IntStream.range(minLen, entriesBySize.length)
                    .forEach(k -> getCounts(k - minLen, row.substring(0, k), res));
            fullOutput.println(marker+res);
        }
        return entriesBySize;
    }

    @Override
    public String getSubsetMarker() {
        return EXT+"."+minLen+"_"+entriesBySize.length;
    }

    private void fillMap(int ind, String key, String marker) {
        PrefixStorage<T>  st = entriesBySize[ind];
        if(st == null) {
            st = PrefixStorage.createStorage(type, ind+minLen);
            entriesBySize[ind] = st;
        }
        st.updateStorage(key, marker);
    }



    protected int getCounts(int ind, String key, List<Integer> counts) {
        PrefixStorage<T>  st = entriesBySize[ind];
        int count = st != null ? st.prefixCount(key) : 0;
        counts.add(count);
        return count;
    }

    @Override
    public void output(PrintWriter pw){
        fullOutput = pw;
        traverseFile(); // 2nd time just to print subset
    }

    @Override
    public void run(){
        try {
            List<String> toCollect = Lists.newLinkedList();
            for (int i = 0; i < borders.size() - 1; i++) {
                toCollect.add(processSubset(borders.get(i), borders.get(i + 1)));
            }
            collectResults(toCollect);
            cleanup(toCollect);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void cleanup(List<String> toCollect) {
        toCollect.stream().map(s -> new File(s)).forEach(f -> f.delete());
    }

    private String processSubset(int min, int max) throws IOException{
        System.out.printf("processing: %d - %d \n", min, max);
        init(min, max);
        traverseFile();
        if(printTop > 0){
            OutputUtils.trace(entriesBySize, printTop);
        }
        return output();
    }

    private void collectResults(List<String> files) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(
                Paths.get(filePath+EXT)))) {
            List<Iterator<String>> iterators = Lists.newArrayList();
            for (String file : files) {
                iterators.add(Files.lines(Paths.get(file)).iterator());
            }
            while(true) {
                List<String> join = Lists.newLinkedList();
                for (Iterator<String> it : iterators) {
                    if(!it.hasNext()){
                        return;
                    }
                    join.add(it.next());
                }
                pw.println(toOneStringFormatted(join));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toOneString(List<String> join) {
        String res = "";
        for(String s: join){
            int start = res.length() == 0 ? 0 : s.indexOf('[')+1;
            int end = s.length()-1;
            res = res + ", "+ s.substring(start, end);
        }
        return res.substring(2)+']';
    }

    private String toOneStringFormatted(List<String> join) {
        List<String> allData = Lists.newArrayList();
        for(String s: join){
            if(allData.isEmpty()){
                allData.add(s.substring(0, s.indexOf('[')));
            }
            Arrays.stream(s.substring(s.indexOf('[')+1, s.length()-1).split(","))
                    .map(t -> String.format("%8d", Integer.parseInt(t.trim())))
                    .forEachOrdered(allData::add);
        }
        return allData.stream().collect(Collectors.joining(""));
    }

    @Override
    public void initArgs(String filePath, String[] args){
        if(args.length > 1 && !args[1].startsWith("-")) {
            borders = Arrays.asList(args[1].split("-")).stream().mapToInt(i -> Integer.parseInt(i)).boxed().collect(Collectors.toList());
        }
        super.initArgs(filePath, args);
    }

    public static void main(String[] args) throws IOException {
        new PrefixProcessorBase(I).process(args);

    }

}
