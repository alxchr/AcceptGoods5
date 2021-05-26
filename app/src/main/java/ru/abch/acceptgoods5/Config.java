package ru.abch.acceptgoods5;

import android.util.ArrayMap;

public class Config {
//    private static String TAG = "Config";
    public static final String ip = "tomcat.comtt.ru";
    public static final int port = 8443;
    public static final String scheme = "https";
/*
    public static final String ip = "192.168.21.244";
    public static final int port = 8080;
    public static final String scheme = "http";

 */
    public static final String goodsPath = "/accept/goods/";
    public static final String barcodesPath = "/accept/barcodes/";
    public static long timeShift;
    public static final boolean tts = true;
    public static long toComttTime(long t) {
        return (t - timeShift)/1000;
    }
    public static long toJavaTime(long t) {
        return t*1000 + timeShift;
    }
    public static final long weekInMillis = 7*24*3600*1000;
    public static String formatCell(String c) {
        String ret ="";
        int i = 0;
        if (Integer.parseInt(c.substring(2,6)) == 0) {
            String s = c.substring(6,12);
            while (s.substring(0, 1).equals("0") && s.length() > 0 ) {
                s = s.substring(1);
            }
            i = 0;
            while (!s.substring(i, i+1).equals("0") && s.length() > 0) {
                ret += s.substring(i, i + 1);
                i++;
            }
            if (s.substring(i+1, i+2).equals("0")) {
                ret += s.substring(i, i+1);
                i++;
            }
            ret +="-";
            ret += Integer.parseInt(s.substring(++i));
        } else {
            ret = Integer.parseInt(c.substring(4, 6)) + "-" + Integer.parseInt(c.substring(6, 9));
        }
        return ret;
    }
    public static int getQty(String goods) {
        return Integer.parseInt(goods.substring(2,4));
    }
    public static final int maxDataCount = 30;
    public static final int offlineTimeout = 10 * 60 * 1000;    // 10 min
}
