package ru.abch.acceptgoods5;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.isExternalStorageEmulated;
import static android.text.InputType.TYPE_CLASS_NUMBER;
import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, SoundPool.OnLoadCompleteListener {
    AlertDialog.Builder adbSettings, adbUpload,  ad, adbDCTNum, adbDeviceId, adbPrinter, adbLabel, adbEnterprise;
    String labelDescription, host;
    LinearLayout llPrintLabel;
    GetWSPrice getWSPrice;
    Price currentPrice = null;
    private static String TAG = "MainActivity";
    private static final int REQ_PERMISSION = 1233, REQUEST_ENABLE_BT = 1231;
    private static TextToSpeech mTTS;
    private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA";
    private EditText etDCTNumber,etLabels, etLabelQnt;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    String[] devices = null, hwDevices = null;
    String btmac;
/*
    String sStoreMan;
    static int storeMan = -1;
    TextView  tvPrompt, tvBoxLabel, tvDescription, tvCell, tvGoods, tvHistory, tvToAccept, tvAccepted, tvStore;
    EditText etScan;
    static  EditText etStoreMan;

    Button btCancel;

 */
//    private String input;
//    private final int WAIT_QNT = 0, ERROR = 1, WAIT_GOODS_CODE = 2, WAIT_GOODS_BARCODE = 3, WAIT_CELL = 4;
//    private int state = ERROR;
//    private boolean scanPressed = false, fastMode = false;
    final int MAX_STREAMS = 5;
    ConnectivityManager cm;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    IntentFilter intentFilter;
    public static boolean online = false;
    final int POSTGOODS = 1, POSTLABEL = 2, POSTLOG = 4;
    MainFragment mf;
    String[] names;
    static ProgressBar pbbar;
    URI uri = null;
    String barcodesURL = null;
    String goodsURL = null;
    String acceptedGoodsURL = null;
    GetWSBarcodes getBarCodes;
    GetWSGoods getGoods;
    PostWebservice postWS;
    private Timer mTimer;
    private OfflineTimerTask offlineTimerTask;
    SoundPool soundPool;
    int soundId1, labNum, labelQnt;
    boolean barcodesRequest = false, goodsRequest = false;
    GetWSCells getCells;
    String cellsURL = null;
    String dumpURL = null;
    GetWSDump getWSDump;
    String filenameSD;
    Context ctx;
    ResultFragment rf;
    private static final String ACTION_DECODE = ScanManager.ACTION_DECODE;   // default action
    //    private static final String ACTION_DECODE_IMAGE_REQUEST = "action.scanner_capture_image";
    private static final String ACTION_CAPTURE_IMAGE = "scanner_capture_image_result";
    private static final String BARCODE_STRING_TAG = ScanManager.BARCODE_STRING_TAG;
    private static final String BARCODE_TYPE_TAG = ScanManager.BARCODE_TYPE_TAG;
    private static final String ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER";
    //    private static final String BARCODE_LENGTH_TAG = ScanManager.BARCODE_LENGTH_TAG;
//    private static final String DECODE_DATA_TAG = ScanManager.DECODE_DATA_TAG;
    private ScanManager mScanManager = null;
    //    private static Map<String, BarcodeHolder> mBarcodeMap = new HashMap<String, BarcodeHolder>();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG,"onReceive , action:" + action);
            // Get scan results, including string and byte data etc.
            byte type = intent.getByteExtra(BARCODE_TYPE_TAG, (byte) 0);
            String barcodeStr = intent.getStringExtra(BARCODE_STRING_TAG);
            Log.i(TAG,"barcode type:" + type);
//            String scanResult = new String(barcode, 0, barcodeLen);
            String codeId = " ";
            if(type == 100) {
                if(barcodeStr.length() == 12) {
                    codeId = "c";   //upc
//                    barcodeStr = barcodeStr.substring(0,11);
                } else if(barcodeStr.length() == 13) {
                    codeId = "d";   //ean13
//                    barcodeStr = barcodeStr.substring(0,12);
                }
            }
            if(type == 98) codeId = "b";
            mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
            if (mf != null) mf.processScan(barcodeStr, codeId);
        }
    };

    private boolean initScanUrovo() {
        boolean powerOn = false;
        try {
            mScanManager = new ScanManager();
            powerOn = mScanManager.getScannerState();
        } catch (Exception e) {
            Log.d (TAG, "No Urovo terminal\r\n" + e.getMessage());
            mScanManager = null;
        }
        return powerOn;
    }
    private void registerReceiverUrovo(boolean register) {
        if (register && mScanManager != null) {
            IntentFilter filter = new IntentFilter();
            int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
            String[] value_buf = mScanManager.getParameterString(idbuf);
            if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
                filter.addAction(value_buf[0]);
            } else {
                filter.addAction(ACTION_DECODE);
            }
            filter.addAction(ACTION_CAPTURE_IMAGE);
            registerReceiver(mReceiver, filter);
        } else if (mScanManager != null) {
            mScanManager.stopDecode();
            unregisterReceiver(mReceiver);
        }
    }
    private BroadcastReceiver barcodeDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BARCODE_DATA.equals(intent.getAction())) {

                int version = intent.getIntExtra("version", 0);
                if (version >= 1) {
//                    String aimId = intent.getStringExtra("aimId");
//                    String charset = intent.getStringExtra("charset");
                    String codeId = intent.getStringExtra("codeId");
                    String data = intent.getStringExtra("data");
//                    byte[] dataBytes = intent.getByteArrayExtra("dataBytes");
                    FL.d(TAG, "Scanned=" + data + " codeId=" + codeId);
                    mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
                    if (mf != null) mf.processScan(data, codeId);
                }
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, intentFilter);
        if(cm.getActiveNetworkInfo() != null) {
            FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
        }
        if (initScanUrovo()) {
            registerReceiverUrovo(true);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            barcodeDataReceiver = null;
        } else {
            registerReceiver(barcodeDataReceiver, new IntentFilter(ACTION_BARCODE_DATA));
        }
        if(App.state == App.SHOW_RESULT) {
            gotoResultFragment();
        } else {
            gotoMainFragment();
        }
    }
    @Override
    public void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
        if(barcodeDataReceiver != null) unregisterReceiver(barcodeDataReceiver);
        releaseScanner();
        registerReceiverUrovo(false);
    }
    private void releaseScanner() {
        sendBroadcast(new Intent(ACTION_RELEASE_SCANNER));
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("ru");
            int result = mTTS.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                FL.e(TAG, "TTS: language not supported");
            }
            if (result == TextToSpeech.SUCCESS && App.getStoremanName().isEmpty()) {
                Log.d(TAG, "TTS OK");
                say(getResources().getString(R.string.storeman_number_tts));
            }
        } else {
            FL.e(TAG, "TTS: error");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
//        setContentView(R.layout.activity_one);
        setContentView(R.layout.activity_main);
        FL.init(new FLConfig.Builder(this)
                .minLevel(FLConst.Level.V)
                .logToFile(true)
                .dir(new File(Environment.getExternalStorageDirectory(), App.appName))
                .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                .build());
        FL.setEnabled(true);
        FL.d(TAG,"Start " + App.packageName + " version " + App.versionCode + " store " + App.getStoreName() + " storeman " + App.getStoreMan() +
                " server " + Config.ip);
        /*
        FL.init(new FLConfig.Builder(this)
                .minLevel(FLConst.Level.V)
                .logToFile(true)
                .dir(new File(Environment.getExternalStorageDirectory(), "AcceptGoods5"))
                .retentionPolicy(FLConst.RetentionPolicy.FILE_COUNT)
                .build());
        FL.setEnabled(true);

         */
        GetWSPackages getWSPackages = new GetWSPackages();
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.packagesPath,
                    null, null);
        } catch (URISyntaxException e) {
            FL.e(TAG, e.getMessage());
        }
        try {
            getWSPackages.run(uri.toASCIIString());
        } catch (IOException e) {
            FL.e(TAG, e.getMessage());
        }
        Timer logTimer = new Timer();
        logTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                dumpLog();
            }
        }, 3600000L - System.currentTimeMillis() % 3600000L, 3600000L); //1 hour period
        filenameSD = Database.getCurrentDate() + ".txt";
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.warehousesPath,
                    null, null);
        } catch (URISyntaxException e) {
            FL.e(TAG, e.getMessage());
        }
        String warehousesURL = uri.toASCIIString();
        GetWSWarehouses getWSWarehouses = new GetWSWarehouses();
        try {
            getWSWarehouses.run(warehousesURL);
        } catch (IOException e) {
            FL.e(TAG, e.getMessage());
        }
        String storeId = (App.warehouse == null || App.getStoreId() == null)? "    12SPR" : App.getStoreId();
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.goodsPath + storeId + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        goodsURL = uri.toASCIIString();
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.barcodesPath + storeId + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        barcodesURL = uri.toASCIIString();
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.cellsPath + storeId + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        cellsURL = uri.toASCIIString();
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.dumpPath + storeId + "/" + App.getDctNum() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        dumpURL = uri.toASCIIString();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mTTS = new TextToSpeech(this, this);
        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION);
        }

         */
        ArrayList<String> requestPermissionsList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.BLUETOOTH);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.BLUETOOTH_ADMIN);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(requestPermissionsList.size() > 0) {
            String[] requestPermissionsArray = new String[requestPermissionsList.size()];
            requestPermissionsArray = requestPermissionsList.toArray(requestPermissionsArray);
            ActivityCompat.requestPermissions(this, requestPermissionsArray, REQ_PERMISSION);
        }
//        names = getResources().getStringArray(R.array.store_names);
//        final String[] ids = getResources().getStringArray(R.array.store_ids);
//        FL.d(TAG, "Store names length = " + names.length);
        /*
        adbSettings = new AlertDialog.Builder(this);
        if (App.warehouse == null) {
            adbSettings.setTitle(R.string.store_choice)
                    .setItems(names, (dialog, which) -> {
                        FL.d(TAG, "Index = " + which + " store id=" + App.warehouses[which].id);
                        App.setWarehouse(App.warehouses[which]);
                    }).create().show();
        }

         */
        pbbar = findViewById(R.id.pbbar);
        pbbar.setVisibility(View.GONE);

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.addDefaultNetworkActiveListener(new ConnectivityManager.OnNetworkActiveListener() {
            @Override
            public void onNetworkActive() {
                FL.d(TAG,"Network active");
            }
        });
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);


        adbUpload = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adbUpload.setMessage(R.string.force_upload);
        adbUpload.setPositiveButton(R.string.upload,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (online) {
//                    refreshData();
                    gotoResultFragment();
                } else {
                    say(getResources().getString(R.string.upload_deferred));
                }
            }
        });
        adbUpload.setCancelable(false);
        refreshData();

        ad = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        ad.setMessage(R.string.exit);
        ad.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                refreshData();
                App.db.close();
                finish();
                System.exit(0);
            }
        });
        ad.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });
        ad.setCancelable(false);

        soundPool= new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(this);
        soundId1 = soundPool.load(this, R.raw.pik, 1);
        dumpLog();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        refreshCells();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            devices = new String[pairedDevices.size()];
            hwDevices = new String[pairedDevices.size()];
            int i = 0;
            for (BluetoothDevice device : pairedDevices) {
                devices[i] = device.getName();
                hwDevices[i++] = device.getAddress();
            }
        }
        btmac = App.getBthw();
        FL.d(TAG, "Bluetooth address " + btmac);
    }   //end onCreate
    public static void say(String text) {
        if (Config.tts) mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }


    public void pushGP(GoodsPosition gp) {
        Database.addAcceptGoods(App.getStoreMan(), gp.id, gp.barcode, gp.qnt, gp.cell, getCurrentTime());
        FL.d(TAG, "Sent to local DB");
        if (Database.addGoodsCount() > Config.maxDataCount) adbUpload.create().show();
        notification();
        sayAccepted(gp.qnt);
    }

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String phrase;
            if(cm.getActiveNetworkInfo() != null) {
                FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
                phrase = "wifi подключен";
                online = true;
//                Database.uploadGoods();
//                uploadGoods();
                if (mTimer != null) {
                    mTimer.cancel();
                }
            } else {
                FL.d(TAG, "Network disconnected");
                phrase = "wifi отключен";
                online = false;
                mTimer = new Timer();
                offlineTimerTask = new OfflineTimerTask();
                mTimer.schedule(offlineTimerTask, Config.offlineTimeout);
            }
            Toast.makeText(context, phrase, Toast.LENGTH_LONG).show();
        }
    };
    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Yekaterinburg"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Add your menu entries here
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
        mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        rf = (ResultFragment) getSupportFragmentManager().findFragmentByTag(ResultFragment.class.getSimpleName());
        if (mf != null) {
            mf.cancel();
        }
        else if (App.state == App.SHOW_RESULT) {
            gotoMainFragment();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings_item:
                FL.d(TAG, "Settings clicked");
                adbSettings = new AlertDialog.Builder(this);
                adbSettings.setTitle(R.string.store_choice)
                            .setItems(names, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    FL.d(TAG, "Index = " + which + " store id=" + App.warehouses[which].id);
                                    App.setWarehouse(App.warehouses[which]);
                                    setStoreName();
                                }
                            }).create().show();
                return true;
            case R.id.refresh_item:
                FL.d(TAG, "Refresh clicked");
                refreshData();
                return true;
            case R.id.dct_num_item:
                FL.d(TAG, "DCT num clicked");
                etDCTNumber = new EditText(this);
                etDCTNumber.setInputType(TYPE_CLASS_NUMBER);
                etDCTNumber.setText(String.valueOf(App.getDctNum()));
                adbDCTNum = new AlertDialog.Builder(this);
                adbDCTNum.setCancelable(false);
                adbDCTNum.setMessage(R.string.dct_num);
                adbDCTNum.setView(etDCTNumber);
                adbDCTNum.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                adbDCTNum.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String num = etDCTNumber.getText().toString();
                        Log.d(TAG, "DCT num " + num);
                        App.setDctNum(num);
                    }
                });
                adbDCTNum.create().show();
                return true;
            case R.id.clear_item:
                FL.d(TAG, "Clear clicked");
                Database.clearGoods();
                return true;
            case R.id.device_item:
                FL.d(TAG, "Device ID clicked");
                adbDeviceId  = new AlertDialog.Builder(this);
                adbDeviceId.setCancelable(false);
                adbDeviceId.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                adbDeviceId.setMessage(App.deviceUniqueIdentifier);
                adbDeviceId.create().show();
                return true;
            case R.id.printer_item:
                FL.d(TAG, "Printer clicked");
                if(devices != null) {
                    adbPrinter = new AlertDialog.Builder(this);
                    adbPrinter.setTitle(R.string.printer_choice).setItems(devices, (dialog, which) -> {
                        Log.d(TAG, "Index = " + which + " printer " + devices[which]);
                        btmac = hwDevices[which];
                        App.setBtHW(btmac);
                    }).create().show();
                }
                return true;
            case R.id.print_price:
                FL.d(TAG, "Print clicked");
                if (App.currentGoods != null && currentPrice != null/*&& App.getPrinterModel() == App.PRINTER_TSC */) {
                    adbLabel = new AlertDialog.Builder(this);
                    adbLabel.setCancelable(false);
                    adbLabel.setMessage(R.string.adb_label);
                    llPrintLabel = (LinearLayout) getLayoutInflater().inflate(R.layout.adb_label, null);
                    etLabels = llPrintLabel.findViewById(R.id.et_labels);
                    etLabelQnt = llPrintLabel.findViewById(R.id.et_label_qnt);
//                    etLabels = new EditText(this);
                    etLabels.setInputType(TYPE_CLASS_NUMBER);
                    etLabels.setText(String.valueOf(App.currentGoods.qnt));
                    etLabels.setSelection(1);
                    etLabelQnt.setInputType(TYPE_CLASS_NUMBER);
                    etLabelQnt.setText("1");
                    etLabelQnt.setSelection(etLabelQnt.getText().length());
                    adbLabel.setView(llPrintLabel);
                    etLabels.requestFocus();
                    adbLabel.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    adbLabel.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String num = etLabels.getText().toString();
                            labelQnt = Integer.parseInt(etLabelQnt.getText().toString());
                            String barcode = "G" + App.currentGoods.id.replaceAll(" ", ".") +
                                    Integer.toString(labelQnt, 36).toUpperCase();
                            labNum = Integer.parseInt(num);
                            /*
                            FL.d(TAG, "Labels num " + num + " goods id " + App.currentGoods.id + " qnt " + labelQnt + " barcode " + barcode);
                            labelDescription = (App.currentGoods.description.length() < 24)? App.currentGoods.description :
                                    App.currentGoods.description.substring(0,24);
                            LabelTSC labelTSC = new LabelTSC(barcode,
                                    labelQnt, labelDescription, App.currentGoods.article,
                                    App.getCurrentCell().descr, App.getStoreMan());
                            labelTSC.print(labNum);


                            try {
                                uri = new URI(
                                        Config.scheme, null, Config.ip, Config.port,
                                        Config.pricePath + App.currentGoods.id,
                                        null, null);
                                getWSPrice = new GetWSPrice();
                                getWSPrice.run(uri.toASCIIString(), labelQnt, labNum);
                            } catch (URISyntaxException | IOException e) {
                                FL.e(TAG, e.getMessage());
                            }
                            */
                            labelDescription = (App.currentGoods.description.length() < 39)? App.currentGoods.description :
                                    App.currentGoods.description.substring(0,39);
                            LabelTSC labelTSC = new LabelTSC(barcode,
                                    App.currentGoods.description, App.currentGoods.article,
                                    App.currentGoods.brand, currentPrice.price*labelQnt, App.labelCell);
                            labelTSC.printPrice(labNum);
                        }
                    });
                    adbLabel.create().show();
                }
                return true;
            case R.id.print_label:
                FL.d(TAG, "Print clicked");
                if (App.currentGoods != null/*&& App.getPrinterModel() == App.PRINTER_TSC */) {
                    adbLabel = new AlertDialog.Builder(this);
                    adbLabel.setCancelable(false);
                    adbLabel.setMessage(R.string.adb_label);
                    llPrintLabel = (LinearLayout) getLayoutInflater().inflate(R.layout.adb_label, null);
                    etLabels = llPrintLabel.findViewById(R.id.et_labels);
                    etLabelQnt = llPrintLabel.findViewById(R.id.et_label_qnt);
//                    etLabels = new EditText(this);
                    etLabels.setInputType(TYPE_CLASS_NUMBER);
                    etLabels.setText(String.valueOf(App.currentGoods.qnt));
                    etLabels.setSelection(1);
                    etLabelQnt.setInputType(TYPE_CLASS_NUMBER);
                    etLabelQnt.setText("1");
                    etLabelQnt.setSelection(etLabelQnt.getText().length());
                    adbLabel.setView(llPrintLabel);
                    etLabels.requestFocus();
                    adbLabel.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    adbLabel.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String num = etLabels.getText().toString();
                            labelQnt = Integer.parseInt(etLabelQnt.getText().toString());
                            String barcode = "G" + App.currentGoods.id.replaceAll(" ", ".") +
                                    Integer.toString(labelQnt, 36).toUpperCase();
                            labNum = Integer.parseInt(num);
                            /*
                            FL.d(TAG, "Labels num " + num + " goods id " + App.currentGoods.id + " qnt " + labelQnt + " barcode " + barcode);
                            labelDescription = (App.currentGoods.description.length() < 24)? App.currentGoods.description :
                                    App.currentGoods.description.substring(0,24);
                            LabelTSC labelTSC = new LabelTSC(barcode,
                                    labelQnt, labelDescription, App.currentGoods.article,
                                    App.getCurrentCell().descr, App.getStoreMan());
                            labelTSC.print(labNum);


                            try {
                                uri = new URI(
                                        Config.scheme, null, Config.ip, Config.port,
                                        Config.pricePath + App.currentGoods.id,
                                        null, null);
                                getWSPrice = new GetWSPrice();
                                getWSPrice.run(uri.toASCIIString(), labelQnt, labNum);
                            } catch (URISyntaxException | IOException e) {
                                FL.e(TAG, e.getMessage());
                            }
                            */
                            labelDescription = (App.currentGoods.description.length() < 39)? App.currentGoods.description :
                                    App.currentGoods.description.substring(0,39);
                            LabelTSC labelTSC = new LabelTSC(barcode, App.currentGoods.description, App.currentGoods.article, App.currentGoods.brand,
                                    App.labelCell);
                            labelTSC.print(labNum);
                        }
                    });
                    adbLabel.create().show();
                }
                return true;
            case R.id.enterprise_item:
                showAdbServer();
                return true;
            default:
                break;
        }
        return false;
    }
    private void showAdbServer() {
        LinearLayout llAdbServer = (LinearLayout) getLayoutInflater().inflate(R.layout.adb_server_choice, null);
        RadioGroup rgServer = llAdbServer.findViewById(R.id.rg_server);
        RadioButton rbComtt = llAdbServer.findViewById(R.id.rb_comtt);
        RadioButton rbTruckExpert = llAdbServer.findViewById(R.id.rb_truckexpert);

        rgServer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.d(TAG, "Radio group " + i);
                switch (i) {
                    case R.id.rb_comtt:
                        host = getResources().getStringArray(R.array.host_names)[0];
                        break;
                    case R.id.rb_truckexpert:
                        host = getResources().getStringArray(R.array.host_names)[1];
                        break;
                    default:
                        break;
                }

            }
        });
        if(Config.ip.equals(Config.ipComtt)) {
            host = getResources().getStringArray(R.array.host_names)[0];
        } else {
            host = getResources().getStringArray(R.array.host_names)[1];
        }
        rbComtt.setChecked(Config.ip.equals(Config.ipComtt));
        rbTruckExpert.setChecked(!Config.ip.equals(Config.ipComtt));
        adbEnterprise = new AlertDialog.Builder(this);
        adbEnterprise.setTitle(R.string.enterprise).setView(llAdbServer);
        adbEnterprise.setCancelable(false);
        adbEnterprise.setNegativeButton(R.string.cancel,
                (dialog, which) -> dialog.cancel());
        adbEnterprise.setPositiveButton(R.string.yes,
                (dialog, which) -> {
                    App.setEnterpriseIp(host);
                    System.exit(0);
                });
        adbEnterprise.create().show();
    }

    public void refreshData() {
        getGoods = new GetWSGoods();
        getBarCodes = new GetWSBarcodes();
        Database.clearData();
        try {
            pbbar.setVisibility(View.VISIBLE);
//            tvPrompt.setVisibility(View.GONE);
            getGoods.run(goodsURL);
            getBarCodes.run(barcodesURL);
            goodsRequest = true;
            barcodesRequest = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadGoodsPosition(GoodsPosition[] gpa) {
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.goodsPath + App.getStoreId() + "/" + App.getStoreMan() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        acceptedGoodsURL = uri.toASCIIString();
        if(gpa != null && gpa.length > 0) {
            GoodsResult gr = new GoodsResult(true, gpa.length);
            gr.Goods = gpa;
            gr.storeman = App.getStoreMan();
            postWS = new PostWebservice();
            postWS.type = POSTGOODS;
            Gson gson = new Gson();
            try {
                postWS.post(acceptedGoodsURL, gson.toJson(gr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void notification() {
        soundPool.play(soundId1, 1, 1, 0, 0, 1);
    }

    private void sayAccepted(int qty) {
        String text = getResources().getString(R.string.accepted) + "  " + qty + getResources().getString(R.string.quantity);
        say(text);
    }

    public void sayAccepted(int qty, int all) {
        String text = getResources().getString(R.string.accepted) + "  " + qty + getResources().getString(R.string.accepted_of) + all;
        say(text);
    }
    @Override
    public void onLoadComplete(SoundPool soundPool, int i, int i1) {
        Log.d(TAG, "onLoadComplete, sampleId = " + i + ", status = " + i1);
    }

    public class GetWSGoods {
        OkHttpClient client;
        String TAG = "GetWSGoods";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    GoodsSet gs = gson.fromJson(resp, GoodsSet.class);
                    if (gs != null) {
                        Log.d(TAG, "Result = " + gs.success + " length = " + gs.counter);
                        if (gs.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < gs.counter; i++) {
//                                Log.d(TAG, " " + gs.goodsRows[i].id + " " + gs.goodsRows[i].description + " " + gs.goodsRows[i].article + " " + gs.goodsRows[i].qnt);
                                Database.addGoods(
                                        gs.goodsRows[i].id,
                                        gs.goodsRows[i].description,
                                        gs.goodsRows[i].cell,
                                        gs.goodsRows[i].article,
                                        gs.goodsRows[i].qnt,
                                        gs.goodsRows[i].brand
                                );
                            }
                            Database.endTr();
                        }
                    }
                    goodsRequest = false;
                    if (!barcodesRequest) runOnUiThread(endProgress);
                }

                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public class GetWSBarcodes {
        OkHttpClient client;
        String TAG = "GetWSBarcodes";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    BarCodesResult bcr = gson.fromJson(resp, BarCodesResult.class);
                    if (bcr != null) {
                        Log.d(TAG, "Result = " + bcr.success + " length = " + bcr.counter);
                        if (bcr.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < bcr.counter; i++) {
//                                Log.d(TAG, " " + bcr.bc[i].goods + " " + bcr.bc[i].barcode+ " " + bcr.bc[i].qnt);
                                Database.addBarCode(
                                        bcr.bc[i].goods,
                                        bcr.bc[i].barcode,
                                        bcr.bc[i].qnt
                                );
                            }
                            Database.endTr();
                        }
                    }
                    barcodesRequest = false;
                    if (!goodsRequest) runOnUiThread(endProgress);
                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public class PostWebservice {
        public final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client;
        String TAG = "PostWebService";
        int type = 0;
        String logFile = null;
        void post(String url, String json) throws IOException {
//            Log.d(TAG, "\n\r" +json + "\n\r");
            if(type == POSTGOODS) writeFileSD(json);
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Log.d(TAG, "POST url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d(TAG, e.getMessage());
//                    say(getResources().getString(R.string.no_connection));
                }

                public void onResponse(Call call, Response response)
                        throws IOException {
//                    final String resp = response.toString();
                    Log.d(TAG, "Responce =" + response);
                    if (response.code() == 200) {
                        if(type == POSTGOODS) getDump();
                        if (type == POSTLOG && logFile != null) {
                            File log = new File(logFile);
                            log.delete();
                        }
                    }
                }
            });
        }
    }
    class OfflineTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(uploadAlert);
        }
    }
    Runnable uploadAlert = new Runnable() {
        @Override
        public void run() {
            adbUpload.create().show();
            say(getResources().getString(R.string.force_upload));
        }
    };
    Runnable endProgress = new Runnable() {
        @Override
        public void run() {
            pbbar.setVisibility(View.GONE);
//            tvPrompt.setVisibility(View.VISIBLE);
        }
    };
    private void refreshCells() {
        getCells = new GetWSCells();
        try {
            getCells.run(cellsURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void getDump() {
        getWSDump = new GetWSDump();
        try{
            getWSDump.run(dumpURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public class GetWSCells {
        OkHttpClient client;
        String TAG = "GetWSCells";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    CellsResult cellsResult = gson.fromJson(resp, CellsResult.class);
                    if (cellsResult != null) {
                        Log.d(TAG, "Result = " + cellsResult.success + " length = " + cellsResult.counter);
                        if (cellsResult.counter > 0) {
                            Database.beginTr();
                            Database.clearCells();
                            for (int i = 0; i < cellsResult.counter; i++) {
//                                Log.d(TAG, " " + bcr.bc[i].goods + " " + bcr.bc[i].barcode+ " " + bcr.bc[i].qnt);
                                Database.addCell(
                                        cellsResult.cells[i].id,
                                        cellsResult.cells[i].name,
                                        cellsResult.cells[i].descr,
                                        cellsResult.cells[i].type,
                                        cellsResult.cells[i].distance,
                                        cellsResult.cells[i].zonein,
                                        cellsResult.cells[i].zonein_descr
                                );
                            }
                            Database.endTr();
                        }
                    }
                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public class GetWSDump {
        OkHttpClient client;
        String TAG = "GetWSDump";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    DumpResult dumpResult = gson.fromJson(resp, DumpResult.class);
                    if (dumpResult != null) {
                        Log.d(TAG, "Result = " + dumpResult.success + " length = " + dumpResult.counter);
                        if (dumpResult.counter > 0) {
                            Database.beginTr();
                            for (int i = 0; i < dumpResult.counter; i++) {
                                Database.setGoodsSent(dumpResult.rows[i]);
                            }
                            Database.endTr();
                        }
                    }
                }
                public void onFailure(Call call, IOException e) {
                    FL.d(TAG, e.getMessage());
                }
            });
        }
    }
    void writeFileSD(String line) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "SDcard not available: " + Environment.getExternalStorageState());
            return;
        }
        File sdPath = null;
        File[] listExternalDirs = ContextCompat.getExternalFilesDirs(this, null);
        for(File f : listExternalDirs) {
//            Log.d(TAG,"External dir " + f.getAbsolutePath() + " emulated " + isExternalStorageEmulated(f));
            if(!isExternalStorageEmulated(f)) {
                sdPath = f;
                break;
            }
        }
        if (sdPath == null) {
//            Toast.makeText(this,getResources().getString(R.string.no_sd_card),Toast.LENGTH_SHORT).show();
            for(File f : listExternalDirs) {
//                Log.d(TAG, "External dir " + f.getAbsolutePath() + " emulated " + isExternalStorageEmulated(f));
                if(f.getAbsolutePath().contains("emulated")) {
                    sdPath = f;
                    break;
                }
            }
        }
        if (sdPath != null) {
            sdPath.mkdirs();
            File sdFile = new File(sdPath, filenameSD);
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, true));
                bw.write(line + "\n\r");
                bw.close();
                Log.d(TAG, "File is written out: " + sdFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,getResources().getString(R.string.sd_card_error),Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,getResources().getString(R.string.sd_card_error),Toast.LENGTH_SHORT).show();
        }
    }
    private void dumpLog() {
        Gson gson = new Gson();
        LogFile logFile;
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    "/filelogger/log/" + App.deviceUniqueIdentifier + "/",
                    null, null);
            String logURL = uri.toASCIIString();
            File logPath = new File(Environment.getExternalStorageDirectory() + "/" + App.appName);
            File[] logFilesArray = logPath.listFiles();
            if (logFilesArray != null) for (File log : logFilesArray) {
                try {
                    FileInputStream fis = new FileInputStream(Environment.getExternalStorageDirectory() + "/" + App.appName + "/" + log.getName());
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String str = "", sBody = "";
                    while ((str = br.readLine()) != null) {
                        sBody += str + "\r\n";
                    }
                    byte[] fileBody = sBody.getBytes(StandardCharsets.UTF_8);
                    logFile = new LogFile(App.appName, log.getName(), Base64.encodeToString(fileBody, Base64.DEFAULT));
                    postWS = new PostWebservice();
                    try {
                        postWS.type = POSTLOG;
                        postWS.logFile = Environment.getExternalStorageDirectory() + "/" + App.appName + "/" + log.getName();
                        postWS.post(logURL, gson.toJson(logFile));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (URISyntaxException e) {
            Log.d(TAG, e.getMessage());
        }
    }
    public class GetWSWarehouses {
        OkHttpClient client;
        String TAG = "GetWSWarehouses";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    WarehousesResult warehousesResult = gson.fromJson(resp, WarehousesResult.class);
                    if (warehousesResult != null) {
                        Log.d(TAG, "Result = " + warehousesResult.success + " length = " + warehousesResult.counter);
                        if (warehousesResult.counter > 0) {
                            App.warehouses = warehousesResult.Warehouses;
                            names = new String[warehousesResult.counter];
                            for (int i = 0; i < warehousesResult.counter; i++) {
                                names[i] = warehousesResult.Warehouses[i].descr;
                            }

                            runOnUiThread(() -> {
                                if (App.warehouse == null) {
                                    adbSettings = new AlertDialog.Builder(ctx);
                                    adbSettings.setTitle(R.string.store_choice)
                                            .setItems(names, (dialog, which) -> {
                                                FL.d(TAG, "Index = " + which + " store id=" + App.warehouses[which].id);
                                                App.setWarehouse(App.warehouses[which]);
                                                setStoreName();
//                                                dialog.cancel();
                                            }).create().show();
                                }
                            });


                        }
                    }
                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });

        }
    }
    public class GetWSPackages{
        OkHttpClient client;
        String TAG = "GetWSPackages";
        void run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    ComttPackagesResult res = gson.fromJson(resp, ComttPackagesResult.class);
                    if (res != null) {
                        Log.d(TAG, "Result = " + res.success + " length = " + res.counter);
                        if (res.counter > 0) {
                            for (int i = 0; i < res.counter; i++) {
                                Log.d(TAG, " " + res.packages[i].id + " " + res.packages[i].version);
                                if(res.packages[i].id.equals(App.packageName) && res.packages[i].version > App.versionCode)  {
                                    runOnUiThread(() -> {
                                        AlertDialog.Builder adbNewVersion =
                                                new AlertDialog.Builder(ctx)
                                                        .setNegativeButton(getResources().getString(R.string.yes), (dialog, which) -> finish())
                                                        .setMessage(getResources().getString(R.string.new_version));
                                        adbNewVersion.create().show();
                                    });
                                }
                            }
                        }
                    }
                }
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
    public void gotoMainFragment() {
        mf = MainFragment.newInstance();
        App.state = App.WAIT_GOODS_CODE;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mf, MainFragment.class.getSimpleName())
                .commitNow();
    }
    public void gotoResultFragment() {
//        ArrayList<GoodsRow> goods = Database.goodsToUpload();
        ArrayList<GoodsPosition> goods = Database.goodsToShow();
        rf = ResultFragment.newInstance(goods);
        App.state = App.SHOW_RESULT;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, rf, ResultFragment.class.getSimpleName())
                .commitNow();
    }
    public void uploadAcceptedGoods() {
        uploadGoodsPosition(Database.goodsToUpload());
    }
    private void setStoreName() {
        mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        if (mf != null) mf.setStoreName();
    }
    public class GetWSPrice {
        OkHttpClient client;
        String TAG = "GetWSPrice";
        void run(String url, int qty, int labels) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Log.d(TAG, "GET url " + url);
            currentPrice = null;
            WebserviceHTTPClient httpClient = new WebserviceHTTPClient(url);
            client = httpClient.getClient();
            Call call = client.newCall(request);
            /*
            String barcode = "G" + App.currentGoods.id.replaceAll(" ", ".") +
                    Integer.toString(qty, 36).toUpperCase();
            labelDescription = (App.currentGoods.description.length() < 64)? App.currentGoods.description :
                    App.currentGoods.description.substring(0,64);

             */
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.body().string();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    currentPrice = gson.fromJson(resp, Price.class);

                    mf = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
                    if (mf != null) {
//                        runOnUiThread(() -> mf.setPrice(currentPrice));
                    }
                }
                public void onFailure (Call call, IOException e){
//                    currentPrice = null;
                    FL.d(TAG, e.getMessage());
                }
            });
        }
    }
    public void getPrice(String goodsId) {
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.pricePath + goodsId,
                    null, null);
            getWSPrice = new GetWSPrice();
            getWSPrice.run(uri.toASCIIString(), labelQnt, labNum);
        } catch (URISyntaxException | IOException e) {
            FL.e(TAG, e.getMessage());
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(App.getLogon()) {
            menu.getItem(1).setEnabled(false);
        }
        return true;
    }
}
