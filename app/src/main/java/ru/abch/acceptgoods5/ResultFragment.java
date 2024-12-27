package ru.abch.acceptgoods5;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResultFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResultFragment extends Fragment {
    ListView lvGoodsList;
    ArrayList<GoodsPosition> goodsList;
    Context ctx;
    GoodsAdapter goodsAdapter;
    int selectedPosition = 0;
    String TAG = "ResultFragment";
    Button btSend;
    public ResultFragment() {
        // Required empty public constructor
    }
    public static ResultFragment newInstance(ArrayList<GoodsPosition> goods) {
        ResultFragment rf = new ResultFragment();
        rf.goodsList = goods;
        return rf;
    }
/*
    public static ResultFragment newInstance(ArrayList<GoodsRow> goods) {
        ResultFragment rf = new ResultFragment();
        rf.goodsList = goods;
        return rf;
    }
*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        lvGoodsList = view.findViewById(R.id.lv_goods);
        btSend = view.findViewById(R.id.btSend);
        return view;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ctx = requireActivity();
        goodsAdapter = new GoodsAdapter(ctx, goodsList);
        goodsAdapter.registerGoodsSelect(position -> selectGoodsInList(goodsList.get(position)));
        lvGoodsList.setAdapter(goodsAdapter);
//        lvGoodsList.requestFocus();
        lvGoodsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedPosition = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                selectedPosition = -1;
            }
        });

        lvGoodsList.setOnKeyListener((view, i, keyEvent) -> {
            if(selectedPosition >= 0 &&
                    keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                selectGoodsInList(goodsList.get(selectedPosition));
            }
            return false;
        });
        btSend.setOnClickListener(view -> {
            ((MainActivity) requireActivity()).uploadAcceptedGoods();
            App.state = App.WAIT_GOODS_CODE;
            ((MainActivity) requireActivity()).gotoMainFragment();
        });
    }
    private void selectGoodsInList(GoodsPosition gp) {
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        AlertDialog.Builder adbEdit = new AlertDialog.Builder(requireActivity());

        View alertDialogView = inflater.inflate(R.layout.adb_edit_qnt, null);
        EditText etQnt = alertDialogView.findViewById(R.id.adb_et_qnt);
        etQnt.setText(String.valueOf(gp.qnt));
        etQnt.setSelection(etQnt.getText().length());
        adbEdit.setTitle(getResources().getString(R.string.edit_data)).setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {

        }).setMessage(gp.description).setView(alertDialogView)
                .setPositiveButton(getResources().getString(R.string.yes), (dialog, which) -> {
                    int qnt = Integer.parseInt(etQnt.getText().toString());
                    if (qnt > 0) {
                        Database.updateAcceptedGoods(App.getStoreMan(), gp.id, qnt, gp.cell);
                    } else {
                        Database.deleteAcceptedGoods(App.getStoreMan(), gp.id, gp.cell);
                    }
                    goodsList = Database.goodsToShow();
                    goodsAdapter.update(goodsList);
                    lvGoodsList.invalidate();
                }).setNeutralButton(getResources().getString(R.string.clear), (dialog, which) -> {
                    Database.deleteAcceptedGoods(App.getStoreMan(), gp.id, gp.cell);
                    goodsList = Database.goodsToShow();
                    goodsAdapter.update(goodsList);
                    lvGoodsList.invalidate();
                }).create().show();
    }
}