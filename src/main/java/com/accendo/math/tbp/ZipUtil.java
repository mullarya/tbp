package com.accendo.math.tbp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

/**
 * Created by annamyullyari on 3/29/19.
 */
public class ZipUtil {
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

    public static String allArch(String str){
        if (str == null || str.length() == 0) {
            return "";
        }
        String st = cutZero(str);
        return  Integer.toString(archSize(str))+" "+
                Integer.toString(deflate(str))+" "+
                Integer.toString(archSize(st))+" "+
                Integer.toString(deflate(st));
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
        String string = "11111111333333333333333";
        String zip = ZipUtil.compress(string);
        System.out.println(archSize(string)+" after compress: "+zip);
        System.out.println(zip);
    }
}