package ru.abch.acceptgoods5;

import android.Manifest;
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
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bosphere.filelogger.FL;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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

import static org.apache.http.conn.ssl.SSLSocketFactory.SSL;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, SoundPool.OnLoadCompleteListener {
    AlertDialog.Builder adbSettings, adbError, adbUpload,  ad, adScan, adbGoods;
    private static String TAG = "MainActivity";
    private static final int REQ_PERMISSION = 1233;
    private static TextToSpeech mTTS;
    TextView tvStore;
    static  EditText etStoreMan;
    String sStoreMan;
    static int storeMan = -1;
    TextView  tvPrompt, tvBoxLabel, tvDescription, tvCell, tvGoods, tvHistory, tvToAccept;
    EditText etScan, etQnt;
    private String input;
    private final int WAIT_QNT = 0, ERROR = 1, WAIT_GOODS_CODE = 2, WAIT_GOODS_BARCODE = 3, WAIT_CELL = 4;
    private int state = ERROR;
    private boolean scanPressed = false, fastMode = false;
    final int MAX_STREAMS = 5;
    ConnectivityManager cm;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    IntentFilter intentFilter;
    public static boolean online = false;
    final String[] ids = new String[] {"     2   ", "     JCTR", "    10SSR", "    12SPR", "    1ASPR", "    1BSPR", "    1ISPR", "    1LSPR",
            "    1OSPR", "    1PSPR", "    1CSPR", "    1SSPR", "    1USPR", "    15SPR", "    1TSPR", "    28SPR", "    27SPR"};
    int qnt;
    GoodsPosition gp = null, prevGP = null;
    String cell, sQnt = "", sScan = "";
    Button btCancel;
    final String[] storeCode = new String[] {"1908","1909","1907","1901","1900","1902","1906","1904","1903","1905","1900","1900","1900","1910","1900","1911","1912"};
    String[] names;
    static ProgressBar pbbar;
    private static final String ACTION_BARCODE_DATA = "com.honeywell.sample.action.BARCODE_DATA";
    private static final String ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER";
    private static final String ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER";
    private static final String EXTRA_SCANNER = "com.honeywell.aidc.extra.EXTRA_SCANNER";
    private static final String EXTRA_PROFILE = "com.honeywell.aidc.extra.EXTRA_PROFILE";
    private static final String EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES";
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
    int soundId1;
    int toAccept;
    String history = null;
    boolean barcodesRequest = false, goodsRequest = false;
    private BroadcastReceiver barcodeDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BARCODE_DATA.equals(intent.getAction())) {
/*
These extras are available:
"version" (int) = Data Intent Api version
"aimId" (String) = The AIM Identifier
Honeywell Android Data Collection Intent APIAPI DOCUMENTATION
"charset" (String) = The charset used to convert "dataBytes" to "data" string
"codeId" (String) = The Honeywell Symbology Identifier
"data" (String) = The barcode data as a string
"dataBytes" (byte[]) = The barcode data as a byte array
"timestamp" (String) = The barcode timestamp
*/
                int version = intent.getIntExtra("version", 0);
                if (version >= 1) {
                    String aimId = intent.getStringExtra("aimId");
                    String charset = intent.getStringExtra("charset");
                    String codeId = intent.getStringExtra("codeId");
                    String data = intent.getStringExtra("data");
                    byte[] dataBytes = intent.getByteArrayExtra("dataBytes");
                    String dataBytesStr = bytesToHexString(dataBytes);
                    String timestamp = intent.getStringExtra("timestamp");
                    String text = String.format(
                            "\nData:%s\n" +
                                    "Charset:%s\n" +
                                    "Bytes:%s\n" +
                                    "AimId:%s\n" +
                                    "CodeId:%s\n" +
                                    "Timestamp:%s\n",
                            data, charset, dataBytesStr, aimId, codeId, timestamp);
                    Log.d(TAG, "Honeywell scanned:" + text);
                    FL.d(TAG, "Scanned=" + data + " codeId=" + codeId);
                    processScan(data, codeId);
                }
            }
        }
    };

    private String bytesToHexString(byte[] arr) {
        String s = "[]";
        if (arr != null) {
            s = "[";
            for (int i = 0; i < arr.length; i++) {
                s += "0x" + Integer.toHexString(arr[i]) + ", ";
            }
            s = s.substring(0, s.length() - 2) + "]";
        }
        return s;
    }
/*
    private void claimScanner() {
        Bundle properties = new Bundle();
        properties.putBoolean("DPR_DATA_INTENT", true);
        properties.putString("DPR_DATA_INTENT_ACTION", ACTION_BARCODE_DATA);
        sendBroadcast(new Intent(ACTION_CLAIM_SCANNER)
                .putExtra(EXTRA_SCANNER, "dcs.scanner.imager")
                .putExtra(EXTRA_PROFILE, "MyProfile1")
                .putExtra(EXTRA_PROPERTIES, properties)
        );
    }

    private void releaseScanner() {
        sendBroadcast(new Intent(ACTION_RELEASE_SCANNER));
    }

 */

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, intentFilter);
        if(cm.getActiveNetworkInfo() != null) {
            FL.d(TAG,"Network " + cm.getActiveNetworkInfo().getExtraInfo() + " " + cm.getActiveNetworkInfo().getDetailedState());
        }
        registerReceiver(barcodeDataReceiver, new IntentFilter(ACTION_BARCODE_DATA));
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
        unregisterReceiver(barcodeDataReceiver);
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = new Locale("ru");
            int result = mTTS.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS: language not supported");
            }
            if (result == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TTS OK");
                say(getResources().getString(R.string.storeman_number_tts));
            }
        } else {
            Log.e(TAG, "TTS: error");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION) {
            if (grantResults.length > 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_one);
        setContentView(R.layout.activity_main);
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.goodsPath + App.getStoreId() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        goodsURL = uri.toASCIIString();
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.barcodesPath + App.getStoreId() + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        barcodesURL = uri.toASCIIString();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mTTS = new TextToSpeech(this, this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION);
        }
        names = getResources().getStringArray(R.array.store_names);
//        final String[] ids = getResources().getStringArray(R.array.store_ids);
        FL.d(TAG, "Store names length = " + names.length);
        adbSettings = new AlertDialog.Builder(this);
        if(App.getStoreIndex() < 0) {
            adbSettings.setTitle(R.string.store_choice)
                    .setItems(names, new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position
                            // of the selected item
                            FL.d(TAG, "Index = " + which + " store id=" + ids[which]);
                            App.setStoreIndex(which);
                            App.setStoreId(ids[which]);
                            App.setStoreName(names[which]);
                        }

                    }).create().show();
        }
        pbbar = findViewById(R.id.pbbar);
        pbbar.setVisibility(View.GONE);
        tvStore = findViewById(R.id.tvStore);
        tvStore.setText(App.getStoreName());
        etStoreMan = findViewById(R.id.et_storeman);
        tvCell = findViewById(R.id.tvCell);
        etScan = findViewById(R.id.etScan);

        if(App.getStoreMan() > 0 ) {
            etStoreMan.setText(String.valueOf(App.getStoreMan()));
            storeMan = App.getStoreMan();
        }
        etStoreMan.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        (i == KeyEvent.KEYCODE_ENTER)){
                    sStoreMan = ((EditText) view).getText().toString();
                    try {
                        storeMan = Integer.parseInt(sStoreMan);
                        FL.d(TAG, "Storeman = " + storeMan);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (storeMan > 0) {
                        App.setStoreMan(storeMan);
//                        etStoreMan.setEnabled(false);
                        view.setEnabled(false);
                        etScan.setEnabled(true);
                        etScan.requestFocus();
                        etScan.getText().clear();
                        tvPrompt.setText(getResources().getString(R.string.scan_goods));
                        state = WAIT_GOODS_BARCODE;
                        refreshData();
                    }
                }
                return false;
            }
        });
        etStoreMan.requestFocus();
        etScan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "On text changed =" + charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input;
                int entIndex;
                if(editable.length() > 2) {
                    input = editable.toString();
//                    Log.d(TAG, "After text changed =" + editable.toString());
                    if (input.contains("\n") && input.indexOf("\n") == 0) {
                        input = input.substring(1);
//                        Log.d(TAG, "Enter char begins string =" + input);
                    }
                    if (input.contains("\n") && input.indexOf("\n") > 0) {
                        entIndex = input.indexOf("\n");
                        input = input.substring(0, entIndex);
//                        Log.d(TAG, "Enter at " + entIndex + " position of input =" + input);
                        if (input.length() > 2) {
                            etScan.setEnabled(false);
                            processScan(input);
                            etScan.setEnabled(true);
                        } else {
                            say(getResources().getString(R.string.enter_again));
                        }
                        etScan.setText("");
                    }
                }
            }
        });

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.addDefaultNetworkActiveListener(new ConnectivityManager.OnNetworkActiveListener() {
            @Override
            public void onNetworkActive() {
                FL.d(TAG,"Network active");
            }
        });
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);

        tvBoxLabel = findViewById(R.id.tvBoxLabel);

        adbError = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adbError.setMessage(R.string.error);
        adbError.setNegativeButton(R.string.dismiss,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                state = WAIT_GOODS_BARCODE;
                etQnt.setText("");
                etScan.setText("");
                tvCell.setText("");
                tvDescription.setText("");
                tvPrompt.setText(getResources().getString(R.string.scan_goods));
                tvGoods.setText("");
            }
        });
        adbError.setPositiveButton(R.string.enter_again,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                etQnt.setText("");
                etQnt.requestFocus();
            }
        });
        adbError.setCancelable(false);


        adbUpload = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adbUpload.setMessage(R.string.force_upload);
        adbUpload.setPositiveButton(R.string.upload,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                if (online) {
                    refreshData();
                } else {
                    say(getResources().getString(R.string.upload_deferred));
                }
            }
        });
        adbUpload.setCancelable(false);
        tvGoods = findViewById(R.id.tvGoods);
        etQnt = findViewById(R.id.etQty);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvPrompt.setTextColor(getResources().getColor(R.color.purple_500));
        tvDescription = findViewById(R.id.tvDescription);
        refreshData();
        etQnt.setOnKeyListener(new View.OnKeyListener() {
            long row = 0;
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        (i == KeyEvent.KEYCODE_ENTER)){
                    int entIndex;
                    input = ((EditText) view).getText().toString();
                    Log.d(TAG, "qnt =" + input);
//                    etScan.getText().clear();
                    Log.d(TAG, "After cleaning text length =" + etQnt.getText().toString().length());
                    if (input.contains("\n")) {
                        entIndex = input.indexOf("\n");
                        Log.d(TAG, "Enter char index = " + entIndex);
                        if (entIndex == 0) {
                            input = input.substring(1);
                        } else input = input.substring(0, entIndex);
                    }
                    try {
                        qnt = Integer.parseInt(input);
                        FL.d(TAG, "qnt = " + qnt);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        qnt = -1;
                    }

                    FL.d(TAG, "Insert storage =" + App.getStoreId() +
                            " storeman=" + storeMan + " goods id =" + gp.id + " barcode =" + gp.barcode +
                            " qnt =" + qnt + " cell =" + cell + " time =" + getCurrentTime());
                    if (qnt > 0 && qnt < 10000) {
                        etQnt.setEnabled(false);
                        etScan.setEnabled(true);
                        etScan.requestFocus();
                        etScan.getText().clear();
                        state = WAIT_GOODS_BARCODE; //go down
                        gp.setQnt(qnt);
                        gp.setCell(cell);
                        gp.setTime(getCurrentTime());
                        if (qnt == toAccept) {
                            notification();
                        } else {
                            sayAccepted(qnt, toAccept);
                        }
                        pushGP(gp);
                        Database.updateTotal(gp.id,toAccept-qnt);
                        int gap1 = tvDescription.getText().toString().indexOf(" ");
                        if (gap1 == -1) {
                            history = tvDescription.getText().toString();
                        } else {
                            history = tvDescription.getText().toString().substring(0, gap1);
                        }
                        history += " " + gp.article + getResources().getString(R.string.accepted_to_cell) + Config.formatCell(gp.cell) + " " + getResources().getString(R.string.qty) + " " + qnt + " из " + toAccept;
                        tvHistory.setText(history);
                        etQnt.setText("");
                        etScan.setText("");
                        tvCell.setText("");
                        tvDescription.setText("");
                        tvPrompt.setText(getResources().getString(R.string.scan_goods));
                        tvGoods.setText("");
                        tvToAccept.setText("");
                    } else {
                        etQnt.getText().clear();
                        say(getResources().getString(R.string.wrong_qnt));
                        FL.d(TAG,getResources().getString(R.string.wrong_qnt) + " = " + qnt);
                        etQnt.setText("");
                        etQnt.requestFocus();
                    }
                }
                return false;
            }
        });

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
        adScan = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        adScan.setMessage(R.string.close_scan);
        adScan.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                state = WAIT_GOODS_BARCODE;
                etQnt.setText("");
                etScan.setText("");
                tvCell.setText("");
                tvDescription.setText("");
                tvPrompt.setText(getResources().getString(R.string.scan_goods));
                tvGoods.setText("");
                etScan.setEnabled(true);
                etScan.requestFocus();
                etQnt.setEnabled(false);
            }
        });
        adScan.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
//                etScan.requestFocus();
            }
        });
        adScan.setCancelable(false);
        btCancel = findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        soundPool= new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(this);
        soundId1 = soundPool.load(this, R.raw.pik, 1);
        tvHistory = findViewById(R.id.tvHistory);
        tvToAccept  = findViewById(R.id.tvToAccept);
    }   //end onCreate
    public static void say(String text) {
        if (Config.tts) mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }
    private void processScan(String scan, String codeId) {
        if(state == ERROR) {
            say(getResources().getString(R.string.storeman_number_tts));
        } else
        if (codeId.equals("c") && scan.length() == 11) {
            Log.d(TAG, "Scan UPC =" + scan);
            //Rebuild UPC check digit
            String res = scan;
            int[] resDigit = new int[11];
            for (int i = 0; i < 11; i++) {
                resDigit[i] = Integer.parseInt(res.substring(i, i + 1));
            }
            int e = resDigit[1] + resDigit[3] + resDigit[5] + resDigit[7] + resDigit[9];
            int o = resDigit[0] + resDigit[2] + resDigit[4] + resDigit[6] + resDigit[8] + resDigit[10];
            o *= 3;
            String r = String.valueOf(o + e);
            int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
            if (c == 10) c = 0;
            res = res + c;
            Log.d(TAG, "Rebuilt UPC code =" + res);
            processScan(res);
        } else
        if (codeId.equals("d")  && scan.length() == 12) {
            Log.d(TAG, "Scan EAN13 =" + scan);
            //Rebuild EAN13 check digit
            String res = scan;
            int[] resDigit = new int[12];
            for (int i = 0; i < 12; i++) {
                resDigit[i] = Integer.parseInt(res.substring(i, i + 1));
            }
            int e = (resDigit[1] + resDigit[3] + resDigit[5] + resDigit[7] + resDigit[9] + resDigit[11]) * 3;
            int o = resDigit[0] + resDigit[2] + resDigit[4] + resDigit[6] + resDigit[8] + resDigit[10];
            String r = String.valueOf(o + e);
            int c = 10 - Integer.parseInt(r.substring(r.length() - 1));
            if (c == 10) c = 0;
            res = res + c;
            Log.d(TAG, "Rebuilt EAN13 code =" + res);
            processScan(res);
        } else
        if (codeId.equals("b")) {
            Log.d(TAG, "Scan code39 =" + scan);
            if(CheckCode.checkGoods39(scan)) {
                String goodsId = scan.substring(1,10).replaceAll("\\."," ");
                String goodsQnt = scan.substring(10).replaceAll("\\.","");
                int qnt = Integer.parseInt(goodsQnt, 36);
                Log.d(TAG, "Goods id ='" + goodsId + "' goods qnt ='" + goodsQnt + "' " + qnt);
                gp = Database.getGoodsPositionById(goodsId);
                if (gp == null) {
                    say(getResources().getString(R.string.wrong_goods));
                } else {
                    gp.qnt = qnt;
                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                    Log.d(TAG, "q=" + q + " qnt=" +gp.qnt);
                    etQnt.setText(String.valueOf(q));
                    tvGoods.setText(gp.article);
                    tvCell.setText(gp.cell);
                    tvDescription.setText(gp.description);
                    tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
                    sayAddress(gp.cell);
                    state = WAIT_CELL;
                    toAccept = gp.total;
                    tvToAccept.setText(String.valueOf(toAccept));
                }
            } else {
                processScan(scan);
            }
        }   else processScan(scan);
    }
    private void processScan(String scan) {
        final String[]goodsDescriptions;
        Log.d(TAG, "Scanned " + scan);
        switch (state) {
            case WAIT_GOODS_BARCODE:
                gp = Database.getGoodsPosition(scan);
                if (gp == null) {
                    final GoodsPosition[] searchResult = Database.searchGoods(scan);
                    if (searchResult != null && searchResult.length > 0){
                        if (searchResult.length == 1) {
                            gp = searchResult[0];
                            qnt = gp.qnt;
                            int q = (gp.qnt == 0) ? 1 : gp.qnt;
                            etQnt.setText(String.valueOf(q));
                            tvGoods.setText(gp.article);
                            tvCell.setText(gp.cell);
                            tvDescription.setText(gp.description);
                            tvPrompt.setText(getResources().getString(R.string.scan_cell));
                            sayAddress(gp.cell);
                            state = WAIT_CELL;
                            toAccept = gp.total;
                            tvToAccept.setText(String.valueOf(toAccept));
                        } else {
                            goodsDescriptions = new String[searchResult.length];
                            for (int j = 0; j < searchResult.length; j++) {
                                Log.d(TAG, "Goods code=" + searchResult[j].id + " desc=" + searchResult[j].description + " barcode=" + searchResult[j].barcode);
                                goodsDescriptions[j] = searchResult[j].description;
                            }
                            adbGoods = new AlertDialog.Builder(this);
                            adbGoods.setTitle(R.string.goods_choice).setItems(goodsDescriptions, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    FL.d(TAG, "Index = " + which + "goods=" + goodsDescriptions[which]);
                                    gp = searchResult[which];
                                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                                    etQnt.setText(String.valueOf(q));
                                    tvGoods.setText(gp.article);
                                    tvCell.setText(gp.cell);
                                    tvDescription.setText(gp.description);
                                    tvPrompt.setText(getResources().getString(R.string.scan_cell));
                                    sayAddress(gp.cell);
                                    state = WAIT_CELL;
                                    toAccept = gp.total;
                                    tvToAccept.setText(String.valueOf(toAccept));
                                }
                            }).create().show();
                        }
                    } else {
                        say(getResources().getString(R.string.wrong_goods));
                    }
                } else {
                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                    Log.d(TAG, "q=" + q + " qnt=" +gp.qnt);
                    etQnt.setText(String.valueOf(q));
                    tvGoods.setText(gp.article);
                    tvCell.setText(gp.cell);
                    tvDescription.setText(gp.description);
                    tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
                    sayAddress(gp.cell);
                    state = WAIT_CELL;
                    toAccept = gp.total;
                    tvToAccept.setText(String.valueOf(toAccept));
                }
                break;
            case WAIT_CELL:
                cell = scan;
                if (CheckCode.checkCellStr(scan)){
                    int prefix, suffix;
                    String result;
                    prefix = Integer.parseInt(scan.substring(0, scan.indexOf(".")));
                    suffix = Integer.parseInt(scan.substring(scan.indexOf(".") + 1));
                    result = storeCode[App.getStoreIndex()] + String.format("%02d",prefix) + String.format("%03d",suffix) + "000";
                    int [] resDigit = new int[12];
                    for (int i = 0; i < 12; i++) {
                        resDigit[i] = Integer.parseInt(result.substring(i, i+1));
                    }
                    int e = (resDigit[1] + resDigit[3] + resDigit[5] +resDigit[7] + resDigit[9] + resDigit[11]) * 3;
                    int o = resDigit[0] + resDigit[2] + resDigit[4] +resDigit[6] + resDigit[8] + resDigit[10];
                    String r = String.valueOf(o+e);
                    int c = 10 - Integer.parseInt(r.substring(r.length() -1));
                    if (c == 10) c = 0;
                    cell = result + c;
                    FL.d(TAG,"Manual input =" + scan + " cell =" + cell);
                }
                if (CheckCode.checkCell(cell)) {
                    String goodsMsg;
                    /*
                    if (scan.length() == 12) {
                        String res = scan;
                        int [] resDigit = new int[12];
                        for (int i = 0; i < 12; i++) {
                            resDigit[i] = Integer.parseInt(res.substring(i, i+1));
                        }
                        int e = (resDigit[1] + resDigit[3] + resDigit[5] +resDigit[7] + resDigit[9] + resDigit[11]) * 3;
                        int o = resDigit[0] + resDigit[2] + resDigit[4] +resDigit[6] + resDigit[8] + resDigit[10];
                        String r = String.valueOf(o+e);
                        int c = 10 - Integer.parseInt(r.substring(r.length() -1));
                        if (c == 10) c = 0;
                        cell = res + c;
                        Log.d(TAG, "Cell =" + cell);
                    }

                     */
                    etScan.setEnabled(false);
                    etQnt.setEnabled(true);
                    etQnt.requestFocus();
                    etQnt.setSelection(etQnt.getText().length());
                    tvCell.setText(Config.formatCell(cell));
                    tvPrompt.setText(getResources().getString(R.string.qnt));
                    state = WAIT_QNT;
                    prevGP = gp;    //Now goods position may be pushed into DB
                    int q =  (gp.qnt == 0) ? 1 : gp.qnt;
                    int iGap = gp.description.indexOf(" ");
                    int lArticle = gp.article.length();
                    if (iGap < 0 ) {
                        goodsMsg = gp.description + " " + gp.article.substring(lArticle <= 3 ? 0 : lArticle - 3) +
                                " "  + q + " "+ getResources().getString(R.string.quantity);
                    } else {
                        goodsMsg = gp.description.substring(0, iGap) + " " + gp.article.substring(lArticle <= 3 ? 0 : lArticle - 3) +
                                " " + q + " " + getResources().getString(R.string.quantity);
                    }
                    say(goodsMsg);
                } else {
                    prevGP = gp;
                    gp = Database.getGoodsPosition(scan);
                    if (gp == null) {
                        say(getResources().getString(R.string.wrong_cell));
                        gp = prevGP;
                        prevGP = null;
                        state = WAIT_CELL;
                        toAccept = gp.total;
                        tvToAccept.setText(String.valueOf(toAccept));
                    } else {
                     /* Another box scanned & found*/
                        prevGP = null;
                        int q = (gp.qnt == 0) ? 1 : gp.qnt;
                        etQnt.setText(String.valueOf(q));
                        tvGoods.setText(gp.article);
                        tvCell.setText(gp.cell);
                        tvDescription.setText(gp.description);
                        tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
                        sayAddress(gp.cell);
                        toAccept = gp.total;
                        tvToAccept.setText(String.valueOf(toAccept));
                    }
                }
                break;
            case WAIT_QNT:
                Log.d(TAG, "Wait qnt input, scanned =" + scan);
                if (CheckCode.checkCell(scan)) {
                    FL.d(TAG, "Have cell scanned " + scan);
                    say(getResources().getString(R.string.have_scanned_cell));
                    break;
                }
                if (prevGP != null) {
                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                    prevGP.setQnt(q);
                    prevGP.setCell(cell);
                    prevGP.setTime(getCurrentTime());
                    if (qnt == toAccept) {
                        notification();
                    } else {
                        sayAccepted(qnt, toAccept);
                    }
                    pushGP(prevGP);
                    Database.updateTotal(prevGP.id,prevGP.total-prevGP.qnt);
                    int gap1 = tvDescription.getText().toString().indexOf(" ");
                    if (gap1 == -1) {
                        history = tvDescription.getText().toString();
                    } else {
                        history = tvDescription.getText().toString().substring(0, gap1);
                    }
                    history += " " + gp.article + getResources().getString(R.string.accepted_to_cell) + Config.formatCell(gp.cell) + " " + getResources().getString(R.string.qty) + " " + qnt + " из " + toAccept;
                    tvHistory.setText(history);
                    say(getResources().getString(R.string.next));
                    prevGP = null;
                    tvCell.setText("");
                    tvGoods.setText("");
                    tvDescription.setText("");
                    etQnt.setText("");
                    etScan.setEnabled(true);
                    etQnt.setEnabled(false);
                    etScan.requestFocus();
                    state = WAIT_GOODS_BARCODE;
                }
                gp = Database.getGoodsPosition(scan);
                if (gp == null) {
                    final GoodsPosition []searchResult = Database.searchGoods(scan);
                    if (searchResult != null && searchResult.length > 0){
                        if (searchResult.length == 1) {
                            Log.d(TAG, "Goods position " + gp.getId());
                            gp = searchResult[0];
                            qnt = gp.qnt;
                            int q = (gp.qnt == 0) ? 1 : gp.qnt;
                            Log.d(TAG, "q=" + q + " qnt=" +gp.qnt);
                            etQnt.setText(String.valueOf(q));
                            tvGoods.setText(gp.article);
                            tvCell.setText(gp.cell);
                            tvDescription.setText(gp.description);
                            tvPrompt.setText(getResources().getString(R.string.scan_cell));
                            sayAddress(gp.cell);
                            state = WAIT_CELL;
                            toAccept = gp.total;
                            tvToAccept.setText(String.valueOf(toAccept));
                        } else {
                            goodsDescriptions = new String[searchResult.length];
                            for (int j = 0; j < searchResult.length; j++) {
                                Log.d(TAG, "Goods code=" + searchResult[j].id + " desc=" + searchResult[j].description + " barcode=" + searchResult[j].barcode);
                                goodsDescriptions[j] = searchResult[j].description;
                            }
                            adbGoods = new AlertDialog.Builder(this);
                            adbGoods.setTitle(R.string.goods_choice).setItems(goodsDescriptions, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    FL.d(TAG, "Index = " + which + "goods=" + goodsDescriptions[which]);
                                    gp = searchResult[which];
                                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                                    etQnt.setText(String.valueOf(q));
                                    tvGoods.setText(gp.article);
                                    tvCell.setText(gp.cell);
                                    tvDescription.setText(gp.description);
                                    tvPrompt.setText(getResources().getString(R.string.scan_cell));
                                    String addr = gp.cell;
                                    if (addr.length() == 0) addr = getResources().getString(R.string.not_set);
                                    say(getResources().getString(R.string.address) + addr);
                                    state = WAIT_CELL;
                                    toAccept = gp.total;
                                    tvToAccept.setText(String.valueOf(toAccept));
                                }
                            }).create().show();
                        }
                    } else {
                        say(getResources().getString(R.string.wrong_goods));
                        break;
                    }
                } else {
                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                    Log.d(TAG, "q=" + q + " qnt=" +gp.qnt);
                    etQnt.setText(String.valueOf(q));
                    tvGoods.setText(gp.article);
                    tvCell.setText(gp.cell);
                    tvDescription.setText(gp.description);
                    tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
                    sayAddress(gp.cell);
                    state = WAIT_CELL;
                    toAccept = gp.total;
                    tvToAccept.setText(String.valueOf(toAccept));
                }
                break;
            case ERROR:
                say(getResources().getString(R.string.storeman_number_tts));
                break;
            default:
                Log.d(TAG,"WTF switch");
                break;
        }
    }
    void pushGP(GoodsPosition gp) {
        Database.addAcceptGoods(storeMan, gp.id, gp.barcode, gp.qnt, gp.cell, getCurrentTime());
        FL.d(TAG, "Sent to local DB");
        if (Database.addGoodsCount() > Config.maxDataCount) adbUpload.create().show();
        if (online) {
            uploadGoods();
        }
//        notification();
//        sayAccepted(gp.qnt);
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
                uploadGoods();
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
        cancel();
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
                                    FL.d(TAG, "Index = " + which + " store id=" + ids[which]);
                                    App.setStoreIndex(which);
                                    App.setStoreId(ids[which]);
                                    App.setStoreName(names[which]);
                                }
                            }).create().show();
                return true;
            case R.id.refresh_item:
                FL.d(TAG, "Refresh clicked");
                refreshData();
                return true;
            default:
                break;
        }
        return false;
    }
    private void cancel() {
        if (state == WAIT_GOODS_BARCODE || state == WAIT_GOODS_CODE) {
            ad.show();
            FL.d(TAG, "Exit");
        }
        if (state == WAIT_CELL) {
            FL.d(TAG, "Finish scan");
            adScan.show();
        }
        if (state == WAIT_QNT) {
            state = WAIT_CELL;
//            tvPrompt.setText(getResources().getString(R.string.scan_cell));
            tvCell.setText(gp.cell);
            int q = (gp.qnt == 0) ? 1 : gp.qnt;
            etQnt.setText(String.valueOf(q));
            etScan.setEnabled(true);
            etQnt.setEnabled(false);
            etScan.requestFocus();
            tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
            sayAddress(gp.cell);
//            toAccept = q;
            toAccept = gp.total;
            tvToAccept.setText(String.valueOf(toAccept));
        }
    }
    private void refreshData() {
        getGoods = new GetWSGoods();
        getBarCodes = new GetWSBarcodes();
        Database.clearData();
        try {
            pbbar.setVisibility(View.VISIBLE);
            tvPrompt.setVisibility(View.GONE);
            getGoods.run(goodsURL);
            getBarCodes.run(barcodesURL);
            goodsRequest = true;
            barcodesRequest = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void uploadGoods() {
        uploadGoodsPosition(Database.goodsToUpload());
//  Clear on successful POST
//        Database.clearData();
    }

    private void uploadGoodsPosition(GoodsPosition gp) {
        GoodsPosition[] gpa = new GoodsPosition[1];
        gpa[0] = gp;
        uploadGoodsPosition(gpa);
    }
    private void uploadGoodsPosition(GoodsPosition[] gpa) {
        try {
            uri = new URI(
                    Config.scheme, null, Config.ip, Config.port,
                    Config.goodsPath + App.getStoreId() + "/" + storeMan + "/",
                    null, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        acceptedGoodsURL = uri.toASCIIString();
        if(gpa != null && gpa.length > 0) {
            GoodsResult gr = new GoodsResult(true, gpa.length);
            gr.Goods = gpa;
            postWS = new PostWebservice();
            Gson gson = new Gson();
            try {
                postWS.post(acceptedGoodsURL, gson.toJson(gr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void notification() {
        soundPool.play(soundId1, 1, 1, 0, 0, 1);
    }
    private void sayAddress(String text) {
        String addr = text;
        if (addr.trim().length() == 0) addr = getResources().getString(R.string.not_set);
        say (addr);
    }
    private void sayAccepted(int qty) {
        String text = getResources().getString(R.string.accepted) + "  " + qty + getResources().getString(R.string.quantity);
        say(text);
    }
    private void sayAccepted(int qty, int all) {
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
            try {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                InputStream clientCertificateContent = getResources().openRawResource(R.raw.terminal);
                keyStore.load(clientCertificateContent, "".toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, "".toCharArray());
                InputStream myTrustedCAFileContent = getResources().openRawResource(R.raw.chaincert);
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate myCAPublicKey = (X509Certificate) certificateFactory.generateCertificate(myTrustedCAFileContent);
                KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustedStore.load(null);
                trustedStore.setCertificateEntry(myCAPublicKey.getSubjectX500Principal().getName(), myCAPublicKey);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustedStore);
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws
                                    CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws
                                    CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                final SSLContext sslContext = SSLContext.getInstance(SSL);
                sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                if (url.contains("https:")) {
                    client = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(120, TimeUnit.SECONDS)
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
                            .build();
                } else client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(120, TimeUnit.SECONDS)
                        .build();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
                e.printStackTrace();
            }

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
                                        gs.goodsRows[i].qnt
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
            try {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                InputStream clientCertificateContent = getResources().openRawResource(R.raw.terminal);
                keyStore.load(clientCertificateContent, "".toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, "".toCharArray());
                InputStream myTrustedCAFileContent = getResources().openRawResource(R.raw.chaincert);
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate myCAPublicKey = (X509Certificate) certificateFactory.generateCertificate(myTrustedCAFileContent);
                KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustedStore.load(null);
                trustedStore.setCertificateEntry(myCAPublicKey.getSubjectX500Principal().getName(), myCAPublicKey);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustedStore);
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws
                                    CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws
                                    CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                final SSLContext sslContext = SSLContext.getInstance(SSL);
                sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                if (url.contains("https:")) {
                    client = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
                            .build();
                } else client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
                e.printStackTrace();
            }

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
        void post(String url, String json) throws IOException {
            Log.d(TAG, "\n\r" +json + "\n\r");
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Log.d(TAG, "POST url " + url);
            try {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                InputStream clientCertificateContent = getResources().openRawResource(R.raw.terminal);
                keyStore.load(clientCertificateContent, "".toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, "".toCharArray());
                InputStream myTrustedCAFileContent = getResources().openRawResource(R.raw.chaincert);
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate myCAPublicKey = (X509Certificate) certificateFactory.generateCertificate(myTrustedCAFileContent);
                KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustedStore.load(null);
                trustedStore.setCertificateEntry(myCAPublicKey.getSubjectX500Principal().getName(), myCAPublicKey);
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustedStore);
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {

                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws
                                    CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                           String authType) throws
                                    CertificateException {
                            }
                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                final SSLContext sslContext = SSLContext.getInstance(SSL);
                sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                if (url.contains("https:")) {
                    client = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String hostname, SSLSession session) {
                                    return true;
                                }
                            })
                            .build();
                } else client = new OkHttpClient();
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
                e.printStackTrace();
            }

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d(TAG, e.getMessage());
//                    say(getResources().getString(R.string.no_connection));
                }

                public void onResponse(Call call, Response response)
                        throws IOException {
                    final String resp = response.toString();
                    Log.d(TAG, "Responce =" + response);
                    if (response.code() ==200) Database.clearGoods();
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
            tvPrompt.setVisibility(View.VISIBLE);
        }
    };
}
