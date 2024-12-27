package ru.abch.acceptgoods5;

import com.bosphere.filelogger.FL;
import com.example.tscdll.TSCActivity;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LabelTSC {
    TSCActivity TscDll = new TSCActivity();//BT
    LabelTSC(String barcode, String description, String article, String brand, String cell) {
        this.article = article;
        this.cell = cell;
        this.barcode = barcode;
        this.description = description;
        this.brand = brand;
        /*
        this.qnt = qnt;
        this.storeman = storeman;

         */
    }
    LabelTSC(String barcode, String description, String article, String brand, int price, String cell) {
        this.article = article;
        this.brand = brand;
        this.barcode = barcode;
        this.description = description;
        this.price = price;
        this.cell = cell;
    }
    private static final String TAG = "LabelTSC";
    String barcode, description, article, cell, brand, line1 = "", line2 = "";
    int storeman, qnt, price;
    private static final int paper_width = 58;
    private static final int paper_height = 30;
    private static final int speed = 4;
    private static final int density = 15;
    private static final int sensor = 0;
    private static final int sensor_distance = 0;
    private static final int sensor_offset = 0;
    public void print(int count) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date today = new Date(System.currentTimeMillis());
        if(count > 0 && count < 16) {
            if(description.length() < 32) {
                line1 = description;
            } else {
                line1 = description.substring(0,32);
                line2 = description.substring(32);
            }
            if(article.length() > 20) article = article.substring(0, 19);
            try {
                TscDll.openport(App.getBthw()); //BT
                TscDll.setup(paper_width, paper_height, speed, density, sensor, sensor_distance, sensor_offset);
                TscDll.sendcommand("SIZE 58 mm, 30 mm\r\n");
                TscDll.sendcommand("GAP 2 mm, 0 mm\r\n");//Gap media
                TscDll.sendcommand("SPEED 4\r\n");
                TscDll.sendcommand("DENSITY 12\r\n");
                TscDll.sendcommand("CODEPAGE UTF-8\r\n");
                TscDll.sendcommand("SET TEAR ON\r\n");
                TscDll.sendcommand("SET COUNTER @1 1\r\n");
                TscDll.sendcommand("@1 = \"0001\"\r\n");
                TscDll.sendcommand("CLS\r\n");
                TscDll.clearbuffer();
                TscDll.sendcommand("TEXT 100,300,\"ROMAN.TTF\",0,12,12,@1\r\n");
//                String sQnt = qnt + " шт.", sStoreman = "кладовщик " + storeman;
//                if(description.length() > 39) description = description.substring(0, 39);
                if(article.length() > 20) article = article.substring(0, 19);
                TscDll.printerfont(10, 10, "2", 0, 1, 1, line1);
                TscDll.printerfont(10, 30, "2", 0, 1, 1, line2);
                TscDll.printerfont(250, 60, "3", 0, 1, 1, brand);
                TscDll.printerfont(20, 60, "3", 0, 1, 1, article);
                TscDll.barcode(40, 150, "39", 40, 0, 0, 2, 5, barcode);
                TscDll.printerfont(20, 200, "2", 0, 1, 1, cell);
                TscDll.printerfont(280, 200, "2", 0, 1, 1, dateFormat.format(today));
                TscDll.printlabel(1, count);
                TscDll.closeport(5000);
            }
            catch (Exception ex) { FL.d(TAG, ex.getMessage());}
        }
    }
    public void printPrice(int count) {
        if(count > 0 && count < 16) {
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Date today = new Date(System.currentTimeMillis());
            String disclaimer1 = App.get().getResources().getString(R.string.price_disclaimer1);
            String disclaimer2 = App.get().getResources().getString(R.string.price_disclaimer2);
//            DecimalFormat dF = new DecimalFormat( "#,###" );
//            String sPrice = dF.format(price) + " руб.";
            String money = Config.ip.equals(Config.ipComtt)? " руб" : " тг";
            String sPrice = price + money;
            if(description.length() < 32) {
                line1 = description;
            } else {
                line1 = description.substring(0,32);
                line2 = description.substring(32);
            }
            try {
                TscDll.openport(App.getBthw()); //BT
                TscDll.setup(paper_width, paper_height, speed, density, sensor, sensor_distance, sensor_offset);
                TscDll.sendcommand("SIZE 58 mm, 30 mm\r\n");
                TscDll.sendcommand("GAP 2 mm, 0 mm\r\n");//Gap media
                TscDll.sendcommand("SPEED 4\r\n");
                TscDll.sendcommand("DENSITY 12\r\n");
                TscDll.sendcommand("CODEPAGE UTF-8\r\n");
                TscDll.sendcommand("SET TEAR ON\r\n");
                TscDll.sendcommand("SET COUNTER @1 1\r\n");
                TscDll.sendcommand("@1 = \"0001\"\r\n");
                TscDll.sendcommand("CLS\r\n");
                TscDll.clearbuffer();
                TscDll.sendcommand("TEXT 100,300,\"ROMAN.TTF\",0,12,12,@1\r\n");
//                String sQnt = qnt + " шт.", sStoreman = "кладовщик " + storeman;
                if(description.length() > 32) description = description.substring(0, 31);
                if(article.length() > 20) article = article.substring(0, 19);
                TscDll.printerfont(10, 10, "2", 0, 1, 1, line1);
                TscDll.printerfont(10, 30, "2", 0, 1, 1, line2);
//                TscDll.printerfont(130, 10, "3", 0, 1, 1, sStoreman);
                TscDll.printerfont(250, 60, "3", 0, 1, 1, brand);
                TscDll.printerfont(20, 60, "3", 0, 1, 1, article);
                /*
                TscDll.printerfont(20, 110, "2", 0, 1, 1, dateFormat.format(today));
                TscDll.printerfont(180, 100, "4", 0, 1, 1, sPrice);

                 */
                TscDll.printerfont(20, 100, "4", 0, 1, 1, sPrice);
                TscDll.printerfont(260, 100, "2", 0, 1, 1, disclaimer1);
                TscDll.printerfont(260, 120, "2", 0, 1, 1, disclaimer2);
                TscDll.barcode(40, 150, "39", 40, 0, 0, 2, 5, barcode);
                TscDll.printerfont(20, 200, "2", 0, 1, 1, cell);
                TscDll.printerfont(280, 200, "2", 0, 1, 1, dateFormat.format(today));
                TscDll.printlabel(1, count);
                TscDll.closeport(5000);
            }
            catch (Exception ex) { FL.d(TAG, ex.getMessage());}
        }
    }
}
