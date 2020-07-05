package ru.abch.acceptgoods5;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.bosphere.filelogger.FL;
import java.util.Arrays;

public class Database {
    private static  String TAG = "Database";
    private static final String DB_NAME = "goodsdb";
    private static final int DB_VERSION = 3;
    private static final String DB_TABLE_MOVEGOODS = "movegoods";
    private static final String DB_TABLE_BARCODES = "barcodes";
    private static final String DB_TABLE_GOODS = "goods";
    private static final String DB_TABLE_ADDGOODS = "add_goods";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_WAREHOUSE_CODE = "wh_code";
    private static final String COLUMN_STOREMAN = "storeman";
    private static final String COLUMN_IN1C = "in1c";
//    private static final String COLUMN_MOVEGOODS_ID = "movegoods_id";
    private static final String COLUMN_GOODS_CODE = "goods_code";
    private static final String COLUMN_INPUT_CELL = "input_cell";
    private static final String COLUMN_OUTPUT_CELL  = "output_cell";
    private static final String COLUMN_QNT  = "qnt";
    private static final String COLUMN_MOVEGOODS_SCAN_TIME = "scan_time";
    private static final String COLUMN_BARCODE  = "barcode";
    private static final String COLUMN_GOODS_DESC = "goods_desc";
    private static final String COLUMN_GOODS_ARTICLE = "goods_article";

    private static final String DB_CREATE_MOVEGOODS =
            "create table " + DB_TABLE_MOVEGOODS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_WAREHOUSE_CODE + " text, " +
                    COLUMN_STOREMAN + " integer, " +
                    COLUMN_IN1C + " integer, " +
                    COLUMN_MOVEGOODS_SCAN_TIME + " integer" +
                    ");";

    private static final String DB_CREATE_BARCODES =
                    "create table " + DB_TABLE_BARCODES + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_BARCODE + " text not null, " +
                    COLUMN_QNT + " integer " +
                    ");";
    private static final String DB_CREATE_GOODS =
            "create table " + DB_TABLE_GOODS + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_GOODS_DESC + " text not null, " +
                    COLUMN_OUTPUT_CELL + " text, " +
                    COLUMN_GOODS_ARTICLE + " text, " +
                    COLUMN_QNT + " integer " +
                    ");";
    private static final String DB_CREATE_ADDGOODS =
            "create table " + DB_TABLE_ADDGOODS + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_STOREMAN + " integer, " +
                    COLUMN_GOODS_CODE + " text not null, " +
                    COLUMN_BARCODE + " text not null, " +
                    COLUMN_QNT + " integer, " +
                    COLUMN_INPUT_CELL + " text, " +
                    COLUMN_MOVEGOODS_SCAN_TIME + " text " +
                    ");";
    private final Context mCtx;
    private DBHelper mDBHelper;
    private static SQLiteDatabase mDB;
//    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//    private static ConnectionClass connectionClass;
    Database(Context ctx) {
        mCtx = ctx;
    }

    void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        try {
            mDB = mDBHelper.getWritableDatabase();
//            connectionClass = new ConnectionClass();
        } catch (SQLException s) {
            new Exception("Error with DB Open");
        }
    }
    void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }


    public static void beginTransaction() {
        mDB.beginTransaction();
    }
    public static void endTransaction() {
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_MOVEGOODS);
            db.execSQL(DB_CREATE_BARCODES);
            db.execSQL(DB_CREATE_GOODS);
            db.execSQL(DB_CREATE_ADDGOODS);
            Log.d(TAG, "onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrade DB from " + oldVersion + " to " + newVersion);
            String dropAddGoods = "drop table if exists " + DB_TABLE_ADDGOODS;
            String dropMoveGoods = "drop table if exists " + DB_TABLE_MOVEGOODS;
            String dropGoods = "drop table if exists " + DB_TABLE_GOODS;
            String dropBarcodes = "drop table if exists " + DB_TABLE_BARCODES;
            if (oldVersion == 1 && newVersion == 2) db.execSQL(DB_CREATE_BARCODES);
            if (newVersion > 2) {
                db.execSQL(dropAddGoods);
                db.execSQL(dropMoveGoods);
                db.execSQL(dropGoods);
                db.execSQL(dropBarcodes);
                db.execSQL(DB_CREATE_MOVEGOODS);
                db.execSQL(DB_CREATE_BARCODES);
                db.execSQL(DB_CREATE_GOODS);
                db.execSQL(DB_CREATE_ADDGOODS);
            }
        }
    }

    static void clearData() {
        mDB.delete(DB_TABLE_BARCODES, null, null);
        mDB.delete(DB_TABLE_GOODS, null, null);
        mDB.delete(DB_TABLE_ADDGOODS, null, null);
        FL.d(TAG,"Clear tables");
    }

    public static long addBarCode(String goodsCode, String barCode, int qnt) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_CODE, goodsCode);
        cv.put(COLUMN_BARCODE, barCode);
        cv.put(COLUMN_QNT, qnt);
        try {
            ret = mDB.insert(DB_TABLE_BARCODES, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }

    public static long addGoods(String goodsCode, String desc, String cell, String article, int total) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_GOODS_CODE, goodsCode);
        cv.put(COLUMN_GOODS_DESC, desc);
        cv.put(COLUMN_OUTPUT_CELL, cell);
        cv.put(COLUMN_GOODS_ARTICLE, article);
        cv.put(COLUMN_QNT, total);
        try {
            ret = mDB.insert(DB_TABLE_GOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }

    static GoodsPosition getGoodsPosition(String barcode) {
        GoodsPosition ret = null;
        String goodsCode;
        String barCode = barcode;
        int qnt, total;
        String barcodeTable = DB_TABLE_BARCODES;
        String goodsTable = DB_TABLE_GOODS;
        String description, cell, article;
        //  Honeywell EDA50K trims EAN-13 last digit
        if (barcode.length() == 12) {
            int [] resDigit = new int[12];
            for (int i = 0; i < 12; i++) {
                resDigit[i] = Integer.parseInt(barcode.substring(i, i+1));
            }
            int e = (resDigit[1] + resDigit[3] + resDigit[5] +resDigit[7] + resDigit[9] + resDigit[11]) * 3;
            int o = resDigit[0] + resDigit[2] + resDigit[4] +resDigit[6] + resDigit[8] + resDigit[10];
            String r = String.valueOf(o+e);
            int c = 10 - Integer.parseInt(r.substring(r.length() -1));
            if (c == 10) c = 0;
            barCode = barcode + c;
        }
        Log.d(TAG, "Goods position barcode =" + barCode + " scan =" + barcode);

        Cursor c = mDB.query( barcodeTable, null,COLUMN_BARCODE + "=?", new String[]{barCode},
                null, null, null, null );
/*
        Cursor c = mDB.query( barcodeTable, null,COLUMN_BARCODE + " like ?", new String[]{barcode+"%"},
                null, null, null, null ); */
        if (c.moveToFirst()) {
            goodsCode = c.getString(1);
            qnt = c.getInt(3);
            Log.d(TAG, "Found goods position code = " + goodsCode + " qnt = " + qnt);
            Cursor cGoods = mDB.query( goodsTable, null,COLUMN_GOODS_CODE + "=?", new String[]{goodsCode},
                    null, null, null, null );
            if (cGoods.moveToFirst()) {
                description = cGoods.getString(2);
                cell = cGoods.getString(3);
                total = cGoods.getInt(5);
                article = cGoods.getString(4);
                ret = new GoodsPosition(goodsCode, barCode, description, cell, qnt, article, total);
                Log.d(TAG, "Found goods position desc = " + description + " cell = " + cell);
            }
        }
        return ret;
    }

    static GoodsPosition getGoodsPositionById(String goodsCode) {
        GoodsPosition ret = null;
        int total;
        String goodsTable = DB_TABLE_GOODS;
        String description, cell, article;
        Cursor cGoods = mDB.query( goodsTable, null,COLUMN_GOODS_CODE + "=?", new String[]{goodsCode},
                null, null, null, null );
        if (cGoods.moveToFirst()) {
            description = cGoods.getString(2);
            cell = cGoods.getString(3);
            total = cGoods.getInt(5);
            article = cGoods.getString(4);
            ret = new GoodsPosition(goodsCode, "", description, cell, 0, article, total);
            Log.d(TAG, "Found goods position desc = " + description + " cell = " + cell);
        }
        return ret;
    }
    static long addAcceptGoods(int storeman, String goodsId, String barcode, int qnt, String cell, String datetime) {
        long ret = 0;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOREMAN,storeman);
        cv.put(COLUMN_GOODS_CODE,goodsId);
        cv.put(COLUMN_BARCODE, barcode);
        cv.put(COLUMN_QNT, qnt);
        cv.put(COLUMN_INPUT_CELL, cell);
        cv.put(COLUMN_MOVEGOODS_SCAN_TIME, datetime);
        try {
            ret = mDB.insert(DB_TABLE_ADDGOODS, null, cv);
        }  catch (SQLiteException ex) {
            FL.e(TAG, Arrays.toString(ex.getStackTrace()));
        }
        return ret;
    }

    static GoodsPosition[] searchGoods(String searchPattern) {
        GoodsPosition[] ret = null;
        int qnt, total;
        String goodsCode, description, cell, barcode, article;
        Cursor c = mDB.query( true, DB_TABLE_GOODS, new String[] {COLUMN_GOODS_CODE, COLUMN_GOODS_DESC, COLUMN_OUTPUT_CELL, COLUMN_GOODS_ARTICLE, COLUMN_QNT},
                COLUMN_GOODS_DESC + " like ? or " + COLUMN_GOODS_ARTICLE + " like ? COLLATE NOCASE",
                new String[] {"%" + searchPattern + "%","%" + searchPattern + "%"},null, null, null, null);
        int count = c.getCount();
        Log.d(TAG, "Found " + count + " rows for " + searchPattern);
        if (count > 0) {
            ret = new GoodsPosition[count];
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                goodsCode = c.getString(0);
                description = c.getString(1);
                Cursor c1 = mDB.query(true, DB_TABLE_BARCODES,null,COLUMN_GOODS_CODE+" =?", new String[] {goodsCode},
                        null, null, null,"1");
                Log.d(TAG, "Barcode count = " + c1.getCount());
                barcode = "";
                qnt = 0;
                while (c1.moveToNext()) {
                    String id = c1.getString(1);
                    barcode = c1.getString(2);
                    qnt = c1.getInt(3);
//                    Log.d(TAG, "Id =" + id + " barcode =" + barcode + " qnt=" + qnt);
                }
                cell = c.getString(2);
                article = c.getString(3);
                total = c.getInt(4);
                GoodsPosition gp = new GoodsPosition(goodsCode,barcode, description, cell, qnt, article, total);
                ret[i] = gp;
                c.moveToNext();
                c1.close();
            }
            c.close();
        }
        return ret;
    }
    static GoodsPosition[] goodsToUpload() {
        GoodsPosition[] ret = null;
        int qnt, total;
        String goodsCode, description, cell, barcode, article;
        Cursor c = mDB.query(DB_TABLE_ADDGOODS, null, null, null, null, null, null);
        int count = c.getCount();
        Log.d(TAG, "Found " + count + " rows for upload");
        if (count > 0) {
            ret = new GoodsPosition[count];
            c.moveToFirst();
            for (int i = 0; i < count; i++) {
                goodsCode = c.getString(2);
                description = "";
                barcode = "";
                cell = c.getString(5);
                article = c.getString(3);
                total = c.getInt(4);
                GoodsPosition gp = new GoodsPosition(goodsCode, barcode, description, cell, total, article, total);
                gp.time = c.getString(6);
                ret[i] = gp;
                c.moveToNext();
            }
            c.close();
        }
        return ret;
    }
    static int addGoodsCount(){
        Cursor c = mDB.query(DB_TABLE_ADDGOODS, null, null, null, null, null, null);
        return c.getCount();
    }
    static void beginTr() {
        mDB.beginTransaction();
    }
    static void endTr() {
        mDB.setTransactionSuccessful();
        mDB.endTransaction();
    }
    static void clearGoods() {
        mDB.delete(DB_TABLE_ADDGOODS, null, null);
    }
}
