package ru.abch.acceptgoods5;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bosphere.filelogger.FL;


public class MainFragment extends Fragment {
    private final String TAG = "MainFragment";

    AlertDialog.Builder adbError, ad, adScan, adbGoods;
    int toAccept;
    String history = null;
    TextView tvPrompt, tvDescription, tvCell, tvGoods, tvHistory, tvToAccept, tvAccepted, tvStore;
    EditText etScan, etStoreMan;
    Button btCancel;
    Context ctx;
    String sStoreMan;
    static int storeMan = -1;
    int qnt;
    GoodsPosition gp = null, prevGP = null;
    String cell;
    public MainFragment() {
        // Required empty public constructor
    }


    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        tvAccepted = view.findViewById(R.id.tvAccepted);
        tvCell = view.findViewById(R.id.tvCell);
//        tvBoxLabel = view.findViewById(R.id.tvBoxLabel);
        tvPrompt = view.findViewById(R.id.tvPrompt);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvGoods = view.findViewById(R.id.tvGoods);
        tvHistory = view.findViewById(R.id.tvHistory);
        tvToAccept = view.findViewById(R.id.tvToAccept);
        tvStore = view.findViewById(R.id.tvStore);
        if(App.warehouse != null) setStoreName();
        etScan = view.findViewById(R.id.etScan);
        etStoreMan = view.findViewById(R.id.et_storeman);
        btCancel = view.findViewById(R.id.btCancel);
        btCancel.setText(getResources().getString(R.string.finish_accept));
        if(App.getStoremanName().isEmpty()) {
            if (App.getStoreMan() > 0) {
                etStoreMan.setText(String.valueOf(App.getStoreMan()));
                storeMan = App.getStoreMan();
            }
            etStoreMan.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                            (i == KeyEvent.KEYCODE_ENTER)) {
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
                            App.firstRun = false;
                            view.setEnabled(false);
                            etScan.setEnabled(true);
                            etScan.requestFocus();
                            etScan.getText().clear();
                            tvPrompt.setText(getResources().getString(R.string.scan_goods));
                            etScan.setBackgroundColor(getResources().getColor(R.color.color_goods));
//                        etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                            tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_goods));
                            App.state = App.WAIT_GOODS_BARCODE;
                            btCancel.setText(getResources().getString(R.string.finish_accept));
                            ((MainActivity) requireActivity()).refreshData();
                        }
                    }
                    return false;
                }
            });

        } else {
//            etStoreMan.setFocusable(false);
            etStoreMan.setText(App.getStoremanName());
        }
        if (App.firstRun && App.getStoremanName().isEmpty()) {
            etStoreMan.requestFocus();
        } else {
            etStoreMan.setFocusable(false);
            etScan.setEnabled(true);
            etScan.requestFocus();
            etScan.getText().clear();
            tvPrompt.setText(getResources().getString(R.string.scan_goods));
            etScan.setBackgroundColor(getResources().getColor(R.color.color_goods));
            tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_goods));
            App.state = App.WAIT_GOODS_BARCODE;
            btCancel.setText(getResources().getString(R.string.finish_accept));
            ((MainActivity) requireActivity()).refreshData();
        }
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
                if(editable.length() > 1) {
                    input = editable.toString();
                    if (input.contains("\n") && input.indexOf("\n") == 0) {
                        input = input.substring(1);
                    }
                    if (input.contains("\n") && input.indexOf("\n") > 0) {
                        entIndex = input.indexOf("\n");
                        input = input.substring(0, entIndex);
                        if (App.state == App.WAIT_QNT) {
                            Log.d(TAG, "qnt =" + input);
//                    etScan.getText().clear();
                            Log.d(TAG, "After cleaning text length =" + etScan.getText().toString().length());
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
                                    " qnt =" + qnt + " cell =" + cell + " time =" + MainActivity.getCurrentTime());
                            if (qnt > 0 && qnt < 10000) {
//                                etQnt.setEnabled(false);
                                etScan.setEnabled(true);
                                etScan.requestFocus();
                                etScan.getText().clear();
                                App.state = App.WAIT_GOODS_BARCODE; //go down
                                btCancel.setText(getResources().getString(R.string.finish_accept));
//                                etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                                gp.setQnt(qnt);
                                gp.setCell(cell);
                                gp.setTime(MainActivity.getCurrentTime());
                                if (qnt == toAccept) {
                                    ((MainActivity) requireActivity()).notification();
                                } else {
                                    ((MainActivity) requireActivity()).sayAccepted(qnt, toAccept);
                                }
                                ((MainActivity) requireActivity()).pushGP(gp);
                                Database.updateTotal(gp.id,toAccept-qnt);
                                /*
                                int gap1 = tvDescription.getText().toString().indexOf(" ");
                                if (gap1 == -1) {
                                    history = tvDescription.getText().toString();
                                } else {
                                    history = tvDescription.getText().toString().substring(0, gap1);
                                }
                                history += " " + gp.article + getResources().getString(R.string.accepted_to_cell) + Config.formatCell(gp.cell) + " " + getResources().getString(R.string.qty) + " " + qnt + " из " + toAccept;

                                 */
                                history = getResources().getString(R.string.to_send) + Database.goodsToShow().size() + getResources().getString(R.string.positions);
                                tvHistory.setText(history);
//                                etQnt.setText("");
                                etScan.setText("");
                                tvCell.setText("");
                                tvDescription.setText("");
                                tvPrompt.setText(getResources().getString(R.string.scan_goods));
                                tvGoods.setText("");
                                tvToAccept.setText("");
                                tvAccepted.setText("");
                                etScan.setBackgroundColor(getResources().getColor(R.color.color_goods));
//                                etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                                tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_goods));
                            } else {
                                etScan.getText().clear();
                                MainActivity.say(getResources().getString(R.string.wrong_qnt));
                                FL.d(TAG,getResources().getString(R.string.wrong_qnt) + " = " + qnt);
                                etScan.setText("");
                                etScan.requestFocus();
                            }
                        } else {
                            if (input.length() > 2) {
                                etScan.setEnabled(false);
                                processScan(input);
                                etScan.setEnabled(true);
                            } else {
                                MainActivity.say(getResources().getString(R.string.enter_again));
                            }
                            etScan.setText("");
                        }
                    }
                }
            }
        });
        tvPrompt.setTextColor(getResources().getColor(R.color.purple_500));
        adbError = new AlertDialog.Builder(ctx, R.style.MyAlertDialogTheme);
        adbError.setMessage(R.string.error);
        adbError.setNegativeButton(R.string.dismiss,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                App.state = App.WAIT_GOODS_BARCODE;
                btCancel.setText(getResources().getString(R.string.finish_accept));
//                etQnt.setText("");
                etScan.setText("");
                tvCell.setText("");
                tvDescription.setText("");
                tvPrompt.setText(getResources().getString(R.string.scan_goods));
                tvGoods.setText("");
            }
        });
        adbError.setPositiveButton(R.string.enter_again,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                etScan.setText("");
                etScan.requestFocus();
            }
        });
        adbError.setCancelable(false);
        adScan = new AlertDialog.Builder(ctx, R.style.MyAlertDialogTheme);
        adScan.setMessage(R.string.close_scan);
        adScan.setPositiveButton(R.string.yes,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                App.state = App.WAIT_GOODS_BARCODE;
                btCancel.setText(getResources().getString(R.string.finish_accept));
//                etQnt.setText("");
                etScan.setText("");
                tvCell.setText("");
                tvDescription.setText("");
                tvPrompt.setText(getResources().getString(R.string.scan_goods));
                tvGoods.setText("");
                etScan.setEnabled(true);
                etScan.requestFocus();
//                etQnt.setEnabled(false);
                tvAccepted.setText("");
                etScan.setBackgroundColor(getResources().getColor(R.color.color_goods));
//                etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_goods));
            }
        });
        adScan.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
//                etScan.requestFocus();
            }
        });
        adScan.setCancelable(false);
//        btCancel = findViewById(R.id.btCancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
        ad = new AlertDialog.Builder(ctx, R.style.MyAlertDialogTheme);
        ad.setMessage(R.string.finish_accept);
        ad.setPositiveButton(R.string.yes, (dialog, arg1) -> {
            if(Database.goodsToShow().isEmpty()) {
                System.exit(0);
            } else {
                ((MainActivity) requireActivity()).gotoResultFragment();
            }
        });
        ad.setNegativeButton(R.string.no, (dialog, arg1) -> {

        });
        ad.setCancelable(false);
        return view;
    }
    @Override
    public void onAttach(@NonNull Context activity) {
        super.onAttach(activity);
        ctx = activity;
    }
    private void processScan(String scan) {
        final String[]goodsDescriptions;
        Log.d(TAG, "Scanned " + scan);
        switch (App.state) {
            case App.WAIT_GOODS_BARCODE:
                gp = Database.getGoodsPosition(scan);
                if (gp == null) {
                    final GoodsPosition[] searchResult = Database.searchGoods(scan);
                    if (searchResult != null && searchResult.length > 0){
                        if (searchResult.length == 1) {
                            gp = searchResult[0];
                            App.currentGoods = gp;
                            ((MainActivity) requireActivity()).getPrice(gp.id);
                            tvGoods.setText(gp.article);
                            tvCell.setText(gp.cell);
                            App.labelCell = gp.cell;
                            tvDescription.setText(gp.description);
                            tvPrompt.setText(getResources().getString(R.string.scan_cell));
                            sayAddress(gp.cell);
                            App.state = App.WAIT_CELL;
                            btCancel.setText(getResources().getString(R.string.cancel));
                            toAccept = gp.total;
                            tvToAccept.setText(String.valueOf(toAccept));
                            tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                            etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//                            etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                            tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
                        } else {
                            goodsDescriptions = new String[searchResult.length];
                            for (int j = 0; j < searchResult.length; j++) {
                                Log.d(TAG, "Goods code=" + searchResult[j].id + " desc=" + searchResult[j].description + " barcode=" + searchResult[j].barcode);
                                goodsDescriptions[j] = searchResult[j].description;
                            }
                            adbGoods = new AlertDialog.Builder(ctx);
                            adbGoods.setTitle(R.string.goods_choice).setItems(goodsDescriptions, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    FL.d(TAG, "Index = " + which + "goods=" + goodsDescriptions[which]);
                                    gp = searchResult[which];
                                    App.currentGoods = gp;
                                    ((MainActivity) requireActivity()).getPrice(gp.id);
                                    /*
                                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                                    etQnt.setText(String.valueOf(q));

                                     */
//                                    etQnt.setText("");
                                    tvGoods.setText(gp.article);
                                    tvCell.setText(gp.cell);
                                    App.labelCell = gp.cell;
                                    tvDescription.setText(gp.description);
                                    tvPrompt.setText(getResources().getString(R.string.scan_cell));
                                    sayAddress(gp.cell);
                                    App.state = App.WAIT_CELL;
                                    btCancel.setText(getResources().getString(R.string.cancel));
                                    toAccept = gp.total;
                                    tvToAccept.setText(String.valueOf(toAccept));
                                    tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                                    etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//                                    etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                                    tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
                                }
                            }).create().show();
                        }
                    } else {
                        MainActivity.say(getResources().getString(R.string.wrong_goods));
                    }
                } else {
                    App.currentGoods = gp;
                    ((MainActivity) requireActivity()).getPrice(gp.id);
                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                    Log.d(TAG, "q=" + q + " qnt=" +gp.qnt);
                    etScan.setText(String.valueOf(q));
                    tvGoods.setText(gp.article);
                    tvCell.setText(gp.cell);
                    App.labelCell = gp.cell;
                    tvDescription.setText(gp.description);
                    tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
                    sayAddress(gp.cell);
                    App.state = App.WAIT_CELL;
                    btCancel.setText(getResources().getString(R.string.cancel));
                    toAccept = gp.total;
                    tvToAccept.setText(String.valueOf(toAccept));
                    tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                    etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//                    etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                    tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
                }
                break;
            case App.WAIT_CELL:
                cell = scan;
                String cellName = null;
                if (CheckCode.checkCellStr(scan)){  //manual cell input
                    int prefix, suffix;
                    String result;
                    prefix = Integer.parseInt(scan.substring(0, scan.indexOf(".")));
                    suffix = Integer.parseInt(scan.substring(scan.indexOf(".") + 1));
                    cellName = String.format("%02d",prefix) + String.format("%03d",suffix);
//                    result = App.storeCode[App.getStoreIndex()] + cellName + "000";
                    result = App.warehouse.storeCode + cellName + "000";
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
                if (cellName == null) {     //cell was scanned
                    if (CheckCode.checkCell(cell)) cellName = Config.getCellName(cell);
                }
                Log.d(TAG, "Cell name " + cellName);
                if (CheckCode.checkCell(cell) && cellName != null && Database.getCellByName(cellName) != null) {    //cell matches pattern & was found in database
                    String goodsMsg;
                    etScan.setEnabled(true);
//                    etQnt.setEnabled(true);
//                    etQnt.requestFocus();
                    etScan.requestFocus();
//                    etQnt.setSelection(etQnt.getText().length());
                    etScan.setSelection(etScan.getText().length());
                    tvCell.setText(Config.formatCell(cell));
                    App.labelCell = Config.formatCell(cell);
                    tvPrompt.setText(getResources().getString(R.string.qnt));
                    App.state = App.WAIT_QNT;
                    btCancel.setText(getResources().getString(R.string.cancel));
//                    etQnt.setBackgroundColor(getResources().getColor(R.color.color_qty));
//                    etScan.setBackgroundColor(getResources().getColor(R.color.color_white));
                    etScan.setBackgroundColor(getResources().getColor(R.color.color_qty));
                    tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_qty));
                    prevGP = gp;    //Now goods position may be pushed into DB
//                    int q =  (gp.qnt == 0) ? 1 : gp.qnt;
//                    String strQnt = etQnt.getText().toString();
                    String strQnt = etScan.getText().toString();
                    int iGap = gp.description.indexOf(" ");
                    int lArticle = gp.article.length();
                    if (iGap < 0 ) {
                        goodsMsg = gp.description + " " + gp.article.substring(lArticle <= 3 ? 0 : lArticle - 3);
//                                + " "  + q + " "+ getResources().getString(R.string.quantity);
                    } else {
                        goodsMsg = gp.description.substring(0, iGap) + " " + gp.article.substring(lArticle <= 3 ? 0 : lArticle - 3);
//                                + " " + q + " " + getResources().getString(R.string.quantity);
                    }
                    if (strQnt.length() > 0) goodsMsg += " " + strQnt + " " + getResources().getString(R.string.quantity);
                    MainActivity.say(goodsMsg);
                } else {
                    prevGP = gp;
                    gp = Database.getGoodsPosition(scan);
                    if (gp == null) {
                        MainActivity.say(getResources().getString(R.string.wrong_cell));
                        gp = prevGP;
                        App.currentGoods = gp;
                        ((MainActivity) requireActivity()).getPrice(gp.id);
                        prevGP = null;
                        App.state = App.WAIT_CELL;
                        btCancel.setText(getResources().getString(R.string.cancel));
                        toAccept = gp.total;
                        tvToAccept.setText(String.valueOf(toAccept));
                        tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                    } else {
                        /* Another box scanned & found*/
                        App.currentGoods = gp;
                        ((MainActivity) requireActivity()).getPrice(gp.id);
                        prevGP = null;
                        int q = (gp.qnt == 0) ? 1 : gp.qnt;
//                        etQnt.setText(String.valueOf(q));
                        etScan.setText(String.valueOf(q));
                        tvGoods.setText(gp.article);
                        tvCell.setText(gp.cell);
                        App.labelCell = gp.cell;
                        tvDescription.setText(gp.description);
                        tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
                        sayAddress(gp.cell);
                        toAccept = gp.total;
                        tvToAccept.setText(String.valueOf(toAccept));
                        tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                        etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//                        etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                        tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
                    }
                }
                break;
            case App.WAIT_QNT:
                Log.d(TAG, "Wait qnt input, scanned =" + scan);
                if (CheckCode.checkCell(scan)) {
                    FL.d(TAG, "Have cell scanned " + scan);
                    MainActivity.say(getResources().getString(R.string.have_scanned_cell));
                    break;
                }
                if (prevGP != null) {
                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                    prevGP.setQnt(q);
                    prevGP.setCell(cell);
                    prevGP.setTime(MainActivity.getCurrentTime());
                    if (qnt == toAccept) {
                        ((MainActivity) requireActivity()).notification();
                    } else {
                        ((MainActivity) requireActivity()).sayAccepted(qnt, toAccept);
                    }
                    ((MainActivity) requireActivity()).pushGP(prevGP);
                    Database.updateTotal(prevGP.id,prevGP.total-prevGP.qnt);
                    /*
                    int gap1 = tvDescription.getText().toString().indexOf(" ");
                    if (gap1 == -1) {
                        history = tvDescription.getText().toString();
                    } else {
                        history = tvDescription.getText().toString().substring(0, gap1);
                    }
                    history += " " + gp.article + getResources().getString(R.string.accepted_to_cell) + Config.formatCell(gp.cell) + " " + getResources().getString(R.string.qty) + " " + qnt + " из " + toAccept;

                     */
                    history = getResources().getString(R.string.to_send) + Database.goodsToShow().size() + getResources().getString(R.string.positions);
                    tvHistory.setText(history);
                    MainActivity.say(getResources().getString(R.string.next));
                    prevGP = null;
                    tvCell.setText("");
                    tvGoods.setText("");
                    tvDescription.setText("");
//                    etQnt.setText("");
                    etScan.setEnabled(true);
                    etScan.setText("");
//                    etQnt.setEnabled(false);
                    etScan.requestFocus();
                    App.state = App.WAIT_GOODS_BARCODE;
                    btCancel.setText(getResources().getString(R.string.finish_accept));
                    tvAccepted.setText("");
                    etScan.setBackgroundColor(getResources().getColor(R.color.color_goods));
//                    etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                    tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_goods));
                }
                gp = Database.getGoodsPosition(scan);
                if (gp == null) {
                    final GoodsPosition []searchResult = Database.searchGoods(scan);
                    if (searchResult != null && searchResult.length > 0){
                        if (searchResult.length == 1) {
                            Log.d(TAG, "Goods position " + gp.getId());
                            gp = searchResult[0];
                            App.currentGoods = gp;
                            ((MainActivity) requireActivity()).getPrice(gp.id);
                            qnt = gp.qnt;
                            int q = (gp.qnt == 0) ? 1 : gp.qnt;
                            Log.d(TAG, "q=" + q + " qnt=" +gp.qnt);
//                            etQnt.setText(String.valueOf(q));
                            etScan.setText(String.valueOf(q));
                            tvGoods.setText(gp.article);
                            tvCell.setText(gp.cell);
                            App.labelCell = gp.cell;
                            tvDescription.setText(gp.description);
                            tvPrompt.setText(getResources().getString(R.string.scan_cell));
                            sayAddress(gp.cell);
                            App.state = App.WAIT_CELL;
                            btCancel.setText(getResources().getString(R.string.cancel));
                            toAccept = gp.total;
                            tvToAccept.setText(String.valueOf(toAccept));
                            tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                            etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//                            etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                            tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
                        } else {
                            goodsDescriptions = new String[searchResult.length];
                            for (int j = 0; j < searchResult.length; j++) {
                                Log.d(TAG, "Goods code=" + searchResult[j].id + " desc=" + searchResult[j].description + " barcode=" + searchResult[j].barcode);
                                goodsDescriptions[j] = searchResult[j].description;
                            }
                            adbGoods = new AlertDialog.Builder(ctx);
                            adbGoods.setTitle(R.string.goods_choice).setItems(goodsDescriptions, new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    FL.d(TAG, "Index = " + which + "goods=" + goodsDescriptions[which]);
                                    gp = searchResult[which];
                                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
//                                    etQnt.setText(String.valueOf(q));
                                    etScan.setText(String.valueOf(q));
                                    tvGoods.setText(gp.article);
                                    tvCell.setText(gp.cell);
                                    App.labelCell = gp.cell;
                                    tvDescription.setText(gp.description);
                                    tvPrompt.setText(getResources().getString(R.string.scan_cell));
                                    String addr = gp.cell;
                                    if (addr.length() == 0) addr = getResources().getString(R.string.not_set);
                                    MainActivity.say(getResources().getString(R.string.address) + addr);
                                    App.state = App.WAIT_CELL;
                                    btCancel.setText(getResources().getString(R.string.cancel));
                                    toAccept = gp.total;
                                    tvToAccept.setText(String.valueOf(toAccept));
                                    tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                                    etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//                                    etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                                    tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
                                }
                            }).create().show();
                        }
                    } else {
                        MainActivity.say(getResources().getString(R.string.wrong_goods));
                        break;
                    }
                } else {
                    App.currentGoods = gp;
                    ((MainActivity) requireActivity()).getPrice(gp.id);
                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                    Log.d(TAG, "q=" + q + " qnt=" +gp.qnt);
//                    etQnt.setText(String.valueOf(q));
                    etScan.setText(String.valueOf(q));
                    tvGoods.setText(gp.article);
                    tvCell.setText(gp.cell);
                    App.labelCell = gp.cell;
                    tvDescription.setText(gp.description);
                    tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
                    sayAddress(gp.cell);
                    App.state = App.WAIT_CELL;
                    btCancel.setText(getResources().getString(R.string.cancel));
                    toAccept = gp.total;
                    tvToAccept.setText(String.valueOf(toAccept));
                    tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                    etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//                    etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                    tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
                }
                break;
            case App.ERROR:
                MainActivity.say(getResources().getString(R.string.storeman_number_tts));
                break;
            default:
                Log.d(TAG,"WTF switch");
                break;
        }
    }
    public void cancel() {
        if (App.state == App.WAIT_GOODS_BARCODE || App.state == App.WAIT_GOODS_CODE) {
            ad.show();
            FL.d(TAG, "Exit");
        }
        if (App.state == App.WAIT_CELL) {
            FL.d(TAG, "Finish scan");
            adScan.show();
        }
        if (App.state == App.WAIT_QNT) {
            App.state = App.WAIT_CELL;
            btCancel.setText(getResources().getString(R.string.cancel));
//            tvPrompt.setText(getResources().getString(R.string.scan_cell));
            tvCell.setText(gp.cell);
            App.labelCell = gp.cell;
            int q = (gp.qnt == 0) ? 1 : gp.qnt;
//            etQnt.setText(String.valueOf(q));
            etScan.setText(String.valueOf(q));
            etScan.setEnabled(true);
//            etQnt.setEnabled(false);
            etScan.requestFocus();
            tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
            sayAddress(gp.cell);
//            toAccept = q;
            toAccept = gp.total;
            tvToAccept.setText(String.valueOf(toAccept));
            etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//            etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
            tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
        }
    }
    private void sayAddress(String text) {
        String addr = text;
        if (addr.trim().length() == 0) addr = getResources().getString(R.string.not_set);
        MainActivity.say (addr);
    }
    public void processScan(String scan, String codeId) {
        if(App.state == App.ERROR) {
            MainActivity.say(getResources().getString(R.string.storeman_number_tts));
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
            if(CheckCode.checkGoods39(scan) && scan.length() >= 11 && scan.contains(".")) {
                String goodsId = scan.substring(1,10).replaceAll("\\."," ");
                String goodsQnt = scan.substring(10).replaceAll("\\.","");
                int qnt = Integer.parseInt(goodsQnt, 36);
                Log.d(TAG, "Goods id ='" + goodsId + "' goods qnt ='" + goodsQnt + "' " + qnt);
                gp = Database.getGoodsPositionById(goodsId);
                if (gp == null) {
                    MainActivity.say(getResources().getString(R.string.wrong_goods));
                } else {
                    App.currentGoods = gp;
                    ((MainActivity) requireActivity()).getPrice(gp.id);
                    gp.qnt = qnt;
                    int q = (gp.qnt == 0) ? 1 : gp.qnt;
                    Log.d(TAG, "q=" + q + " qnt=" + gp.qnt);
//                    etScan.setText(String.valueOf(q));
                    tvGoods.setText(gp.article);
                    tvCell.setText(gp.cell);
                    App.labelCell = gp.cell;
                    tvDescription.setText(gp.description);
                    tvPrompt.setText(getResources().getString(R.string.scan_goods_or_cell));
                    sayAddress(gp.cell);
                    App.state = App.WAIT_CELL;
                    btCancel.setText(getResources().getString(R.string.cancel));
                    toAccept = gp.total;
                    tvToAccept.setText(String.valueOf(toAccept));
                    tvAccepted.setText(String.valueOf(Database.getAccepted(gp.id)));
                    etScan.setBackgroundColor(getResources().getColor(R.color.color_cell));
//                    etQnt.setBackgroundColor(getResources().getColor(R.color.color_white));
                    tvPrompt.setBackgroundColor(getResources().getColor(R.color.color_cell));
                }
            } else {
                processScan(scan);
            }
        }   else processScan(scan);
    }
    public void setStoreName() {
        tvStore.setText(App.getStoreName());
    }
}