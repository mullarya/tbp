package com.accendo.math.tbp;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by anna.myullyari on 3/10/18.
 */
public class PrefixProcessor extends DataProcessor<Map<String, Integer>[]>{

    public static final String EXT = ".pfx";
    public static final String genKey = "GK";

    private Map<String, Integer>[] entriesBySize;
    private List<Pair<Integer, Double>> globalEntropy = Lists.newLinkedList();

    //minimal subset length
    private int minLen = 3;
    private List<Integer> borders = Lists.newArrayList(3,50,80,101); // default chunks to process
    private List<Integer> defaultBorders = Lists.newArrayList(3,50,80,101);

    private PrintWriter fullOutput;

    public void init(int minLen, int maxLen) {
        entriesBySize = new Map[maxLen];
        this.minLen = minLen;
        fullOutput = null;
    }

    @Override
    public Map<String, Integer>[] processRow(String row, String marker, String fullInput) {

        if(fullOutput == null) { // collecting data
            IntStream.range(minLen, entriesBySize.length)
                    .forEach(k -> fillMap(k - minLen, row.substring(0, k)));
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

    private void fillMap(int ind, String key) {
        Map<String, Integer> map = entriesBySize[ind];
        if(map == null) {
            map = Maps.newHashMap();
            map.put(genKey, ind+minLen);
            entriesBySize[ind] = map;
        }
        Integer count = map.get(key);
        count = count == null ? 1 : count + 1;
        map.put(key, count);
    }

    protected int getCounts(int ind, String key, List<Integer> counts) {
        Map<String, Integer> map = entriesBySize[ind];
        int count =  map != null ? map.get(key) : 0;
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

            borders = adjustBorders();
            globalEntropy = Lists.newLinkedList();
            for (int i = 0; i < borders.size() - 1; i++) {
                String res = processSubset(borders.get(i), borders.get(i + 1));
                if(res != null){
                    toCollect.add(res);
                }
            }
            collectResults(toCollect);
            cleanup(toCollect);
            saveGlobal();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private List<Integer> adjustBorders() {
        if(rowLen < defaultBorders.get(defaultBorders.size() - 1)){
            List<Integer> newB = new ArrayList<>();
            for(Integer i: defaultBorders){
                if(i < rowLen){
                    newB.add(i);
                }
                else{
                    newB.add(rowLen);
                    return  newB;
                }
            }
            return newB;

        }
        return defaultBorders;
    }

    private void saveGlobal() throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(
                Paths.get(filePath+".ge")))) {
            globalEntropy.forEach(e -> pw.println(e.getKey()+" "+e.getValue()));

        }
    }

    private void cleanup(List<String> toCollect) {
        toCollect.stream().map(s -> new File(s)).forEach(f -> f.delete());
    }

    private String processSubset(int min, int max) throws IOException{
        System.out.printf("processing: %d - %d \n", min, max);
        init(min, max);
        int totalRows = traverseFile();
        fillGlobal(totalRows);
        return output();
    }

    private void fillGlobal(int N) {
        IntStream.range(0, entriesBySize.length).forEach(
                i -> CollectionUtils.addIgnoreNull(globalEntropy, getGlobalEntropy(i, N)));

    }

    private Pair<Integer, Double> getGlobalEntropy(int i, int N){
        if(entriesBySize[i] == null){
            System.out.println("no data for size "+(i+minLen));
            return null;
        }
        Collection<Integer> frequencies = entriesBySize[i].values();
        Double ent = EntropyProcessor.entropy(frequencies, N);
        return Pair.of(i+minLen, ent);
    }

    //combining chunk files
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
        Pair<String, List<Integer>> frequencies = collectFrequencies(join);
        List<String> allData = Lists.newArrayList();
        allData.add(frequencies.getKey());
        frequencies.getValue().stream()
                    .map(t -> t.toString())
                    .forEachOrdered(allData::add);
        String res =  allData.stream().collect(Collectors.joining(" "));
        return zipLen == null ? res : res+' '+zipLen.get(frequencies.getKey());
    }

    private Pair<String, List<Integer>> collectFrequencies(List<String> join) {
        List<Integer> allData = Lists.newArrayList();
        String prefix = null;
        for(String s: join){
            if(prefix == null){
                prefix = s.substring(0, s.indexOf('['));
            }
            Arrays.stream(s.substring(s.indexOf('[')+1, s.length()-1).split(","))
                    .map(tt -> tt.trim().length() == 0 ? null : tt.trim())
                    .filter(Predicates.notNull())
                    .map(t -> Integer.parseInt(t))
                    .forEachOrdered(allData::add);
        }
        return Pair.of(prefix, allData);
    }

    @Override
    public void initArgs(String filePath, String[] args){
        if(args.length > 1 && !args[1].startsWith("-")) {
            borders = Arrays.asList(args[1].split("-")).stream().mapToInt(i -> Integer.parseInt(i)).boxed().collect(Collectors.toList());
        }
        super.initArgs(filePath, args);
    }

    public static void main(String[] args) throws IOException {
        new PrefixProcessor().process(args);
    }

}
