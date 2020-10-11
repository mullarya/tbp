package com.accendo.math.tbp;

import com.accendo.math.tbp.entropy.Entropy;
import com.accendo.math.tbp.entropy.EntropyFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class CustomEntropyProcessor<N extends Number> extends DataProcessor<List<N>>{


    //protected List<Pair<String, List<N>>> all = Lists.newLinkedList();

    public static String format = "%8.4f";

    Entropy<N> entropy;



    /**
     * Calculate E1, E2, ... En for the given row
     * @param row data row where each character is meaningful (no spaces, no delimiters: 12322233)
     * @param marker - any String to mark a row in set, from the data file 12 characters formatted (i,j).
     * @return  List of calculated entropies for the given row, E1 - for substring of size 1, E2 - substrings of size 2, the longest substring size is row.length/4+1
     * i.e for the data string of length 110 En will be calculated for substring of size 28
     */
    @Override
    public List<N> processRow(String row, String marker, String fullRow) {
       return entropy.derive(row.replace(" ", ""));
       // all.add(Pair.of(marker, res));
       // return res;
    }

    @Override
    public String getSubsetMarker() {
        return '.'+entropy.getMarker();
    }

    @Override
    public void output(PrintWriter file, Pair<String, List<N>> entry){
         file.println(formatEntry(entry));
    }

    @Override
    public void output(PrintWriter file){
        //all.stream().forEach(e-> output(file, e));
    }

    @Override
    public DataProcessor<List<N>> newInstance(Map<String, String> settings) throws IOException {
        initArgs(null, settings);
        entropy = EntropyFactory.getEntropy(marker, minLen, rowLen);
        if(entropy != null){
            CustomEntropyProcessor res = new CustomEntropyProcessor();
            res.entropy = entropy;
            return res;
        }
        return null;
    }

    @Override
    protected void initArgs(String filePath, Map<String, String> settings) throws IOException {
        super.initArgs(filePath, settings);
        entropy = EntropyFactory.getEntropy(marker, minLen, rowLen);
        if(settings.containsKey("-o")){
            output = streamOutput();
        }
        entropy.setup(settings);

    }

    @Override
    protected List<String> getHeader() {
       return entropy.headers();
    }

    public  String formatEntry(Pair<String, List<N>> entry){
        String res =  entry.getKey()+delim+
                entry.getValue().stream().map(d -> formatNumber(d)).collect(Collectors.joining(delim));
        return additional == null ? res : res+delim+ additional.get(entry.getKey());
    }

    private String formatNumber(N d) {
        return d instanceof Double ? String.format(format, OutputUtils.scale((Double)d)) : d.toString();
    }

    public  void initAndRun(String filePath, Map<String, String> settings){
        try {
            initArgs(filePath, settings);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        run();
    }

    @Override
    public void run(){
        traverseFile();
        entropy.stats();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        new CustomEntropyProcessor<>().process(args);
    }

    public void stats(int cnt) {
        System.out.println(filePath+" trace "+cnt);
        entropy.stats();
    }


}
