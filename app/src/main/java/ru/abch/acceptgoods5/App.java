package ru.abch.acceptgoods5;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.pm.PackageInfoCompat;

import com.bosphere.filelogger.FL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.GregorianCalendar;


public class App extends Application {
    static App instance = null;
    static String TAG = "App";
    static SharedPreferences sp;
    private static int storeMan;
    private static String dctNum, bthw;
    /*
    private static final String storeIndexKey = "store_index";
    private static final String storeIdKey = "store_id";
    private static final String storeNameKey = "store_name";

     */
    private static final String storeManKey = "store_man", enterpriseIpKey = "enterprise_ip";
    private static final String dctNumKey = "dctnum";
    public static Database db;
    public static String deviceUniqueIdentifier;
    public static String packageName;
    public static final String appName = "AcceptGoods5";
    public static final int WAIT_QNT = 0, ERROR = 1, WAIT_GOODS_CODE = 2, WAIT_GOODS_BARCODE = 3, WAIT_CELL = 4, SHOW_RESULT = 5;
    public static int versionCode = 0, state = ERROR;
    private static final String storeKey = "store";
    public static Warehouse2[] warehouses;
    private static final String bthwKey = "bt_hw";
    public static Warehouse2 warehouse = null;
    public static GoodsPosition currentGoods;
    public static String labelCell = "";
    public static boolean firstRun = true;
    private static String storemanName = "";
    public static final Uri loginURI = Uri.parse("content://ru.abch.comttwarehouse.LoginProvider/login");
    public static String getStoremanName() {
        return storemanName;
    }
    public static void setStoremanName (String name) {
        storemanName = name;
    }
    private static boolean logon = false;
    public static boolean getLogon() {
        return logon;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        packageName = this.getPackageName();
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(packageName, 0);
            versionCode = (int) PackageInfoCompat.getLongVersionCode(pInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
        deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        instance = this;
        sp = getSharedPreferences();
//        sp = getSharedPreferences("ru.abch.acceptgoods5_preferences", this.MODE_PRIVATE);
        Config.ip = sp.getString(enterpriseIpKey, Config.ipComtt);
        storeMan = sp.getInt(storeManKey, 0);
        dctNum = sp.getString(dctNumKey,"");
        bthw = sp.getString(bthwKey, "");
        String storeJSON = sp.getString(storeKey, "");

        db = new Database(this);
        db.open();
        Cursor loginCursor = getContentResolver().query(App.loginURI, null, null,
                null, null);
        if (loginCursor != null) {
            if(loginCursor.moveToNext()) {
                Log.d(TAG, "Storeman " + loginCursor.getInt(0) + " " + loginCursor.getString(1) + " "
                        + loginCursor.getString(3) + " " + loginCursor.getInt(4) + " " + loginCursor.getString(5));
                setStoreMan(loginCursor.getInt(0));
                setStoremanName(loginCursor.getString(3));
                storeJSON = loginCursor.getString(5);
                sp.edit().putString(storeKey,storeJSON).commit();
                logon = true;
            } else {
                Log.d(TAG, "Not logged in");
            }
            loginCursor.close();
        } else {
            Log.d(TAG, "No login provider");
        }
        if(storeJSON != null && !storeJSON.isEmpty()) {
            warehouse = getWarehouse(storeJSON);
//            Log.d(TAG,"Start application store id =" + warehouse.id + " store " + warehouse.descr);
        }
    }

    public static SharedPreferences getSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(App.get());
        return sharedPrefs;
    }


    public static App get() {
        return instance;
    }

//    public static int getStoreIndex() {return storeIndex;};
    public static String getStoreId() {
        if(warehouse == null) return "";
        else return warehouse.id;
    }
    public static String getStoreName(){
        if(warehouse == null) return "";
        else return warehouse.descr;}
    public static int getStoreMan() { return storeMan;}

    public static void setStoreMan(int sm) {
        storeMan = sm;
        sp.edit().putInt(storeManKey, sm).apply();
    }
    public static void setDctNum(String dct) {
        dctNum = dct.substring(0,4);
        sp.edit().putString(dctNumKey,dctNum).apply();
    }
    public static String getDctNum() {
        return deviceUniqueIdentifier;
    }
    public static Warehouse2 getWarehouse(String storeJSON) {
        Warehouse2 ret;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ret  = gson.fromJson(storeJSON, Warehouse2.class);
        return ret;
    }
    public static void setWarehouse(Warehouse2 wh) {
        Gson gson = new Gson();
        String storeJSON = gson.toJson(wh);
        Log.d(TAG, "Store " + storeJSON);
        sp.edit().putString(storeKey,storeJSON).commit();
        warehouse = wh;
    }
    public static void setBtHW(String hwaddr) {
        bthw = hwaddr;
        sp.edit().putString(bthwKey, hwaddr).apply();
    }
    public static String getBthw() {return  bthw;}
    public static void setEnterpriseIp(String enterpriseIp) {
        sp.edit().putString(enterpriseIpKey,enterpriseIp).commit();
        Config.ip = enterpriseIp;
    }
}
