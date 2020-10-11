package com.accendo.math.tbp;

import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * Created by annamyullyari on 3/29/19.
 */
public class ZipUtil {

    static String prefix;

    public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toString();
    }

    public static int archSize(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.size();
    }

    public static int deflate(String str){
        if (str == null || str.length() == 0) {
            return 0;
        }
        byte[] input = str.getBytes();
        // Compress the bytes
        byte[] output = new byte[100];
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();
        return compresser.deflate(output);
    }

    public static String allArch(String str, StringBuilder head, String delim){
        if (str == null || str.length() == 0) {
            return "";
        }
        String st = cutZero(str);
        return  Integer.toString(archSize(str))+delim+
                Integer.toString(deflate(str))+delim+
                Integer.toString(archSize(st))+delim+
                Integer.toString(deflate(st));
    }

    public static String allArchHead(String delim){
        StringBuilder head = new StringBuilder();
        head.append("archFull").append(delim)
                .append("deflateFull").append(delim)
                .append("archCutZero").append(delim)
                .append("deflateCutZero");
        return head.toString();

    }

    public static List<String> allArchHead(){
        return Arrays.asList("archFull", "deflateFull", "archCutZero", "deflateCutZero");
    }


    private static String cutZero(String str){
        for(int i = str.length()-1; i >= 0; i--){
            if(str.charAt(i) != '0'){
                return str.substring(0, i);
            }
        }
        return str;
    }


    public static void main(String[] args) throws IOException {
       sampleFile("Data256.head");
    }

    public static void sampleFile(String fileName){
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream
                    .forEach(line->{
                        String s = line.substring(43).replace(" ", "");
                        System.out.println(s);

                        try {
                            String zip = ZipUtil.compress(s);
                            System.out.println(zip);
                            comparePrefix(zip, 11);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void comparePrefix(String s, int i) {
        if (prefix == null){
            prefix = s.substring(0, i);
        }
        else if(!prefix.equals(s.substring(0, i))){
            System.out.println("NE: "+s.substring(0, i));
        }
    }


}