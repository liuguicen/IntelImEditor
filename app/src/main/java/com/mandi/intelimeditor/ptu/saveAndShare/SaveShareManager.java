package com.mandi.intelimeditor.ptu.saveAndShare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Created by liuguicen on 2016/8/13.
 */
public class SaveShareManager {
    private Context mContext;
    private static final int SIZE_COUNT = 6;
    private float saveRatio = 1;
    // TODO: 2020/4/23 这个地方View和Manager分离更好
    private AlertDialog dialog;
    private TextView cancleView;
    private TextView sureView;
    List<TextView> sizeViewList = new ArrayList<>();
    List<Double> sizeList = new ArrayList<>();
    List<Integer> sizeIdList = new ArrayList<>();
    private RecyclerView recyclerShare;
    private long sourceSize;
    private boolean hasInit;
    private final long MAX_SIZE = 40 * 1000 * 1000;
    private int choseSizeId;
    private clickListenerInterface listener;
    private List<ShareItemData> acInfo_SupportShare;
    private List<ResolveInfo> resolveInfos;
    private ShareRecyclerAdapter shareRecyclerAdapter;
    private List<Boolean> canClickList;
    private MyQQShare myQQShare;

    public interface clickListenerInterface {
        void saveResult(float saveRatio);

        void mCancel();

        /**
         * @param shareTask 保存是异步的，此接口用于异步的回调保存
         */
        void onShareItemClick(float saveRatio, ShareTask shareTask);
    }

    SaveShareManager(Context context) {
        mContext = context;
        hasInit = false;
        initAcInfo_SupportShare();
    }

    public void init(long sourceSize) {
        this.sourceSize = sourceSize;
        sizeList.addAll(Arrays.asList(1d / 5, 1d / 3, 1d / 2, 1d, 2d, 3d));
        choseSizeId = 3;
        canClickList = new ArrayList<>();
        for (int i = 0; i < SIZE_COUNT; i++) canClickList.add(true);
        hasInit = true;
    }

    public void createDialog(Activity ac) {
        //判断对话框是否已经存在了
        if (dialog != null && dialog.isShowing()) return;
        if (!hasInit) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(ac);
        View view = LayoutInflater.from(ac).inflate(R.layout.dialog_save_set, null);
        cancleView = view.findViewById(R.id.tv_save_set_cancel);
        sureView = view.findViewById(R.id.tv_save_set_sure);
        sizeIdList.addAll(Arrays.asList(R.id.item_save_set_size1, R.id.item_save_set_size2, R.id.item_save_set_size3,
                R.id.item_save_set_size4, R.id.item_save_set_size5, R.id.item_save_set_size6));
        for (int i = 0; i < SIZE_COUNT; i++) {
            sizeViewList.add(view.findViewById(sizeIdList.get(i)));
        }

        recyclerShare = view.findViewById(R.id.recycler_save_set_share);

        dialog = builder.setView(view)
                .create();
        setStyle();

        initView(ac);
        dialog.show();
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void initView(Activity ac) {
        setChoseSizeUi();
        LinearLayoutManager layoutManager = new LinearLayoutManager(ac, LinearLayout.HORIZONTAL, false);
        recyclerShare.setLayoutManager(layoutManager);
        shareRecyclerAdapter = new ShareRecyclerAdapter(ac, acInfo_SupportShare);
        recyclerShare.setAdapter(shareRecyclerAdapter);
    }

    /**
     * 初始化支持分享的APP的信息，包括常用应用的排序
     */
    private void initAcInfo_SupportShare() {
        MyDatabase myDatabase = MyDatabase.getInstance();
        List<Pair<String, String>> preferShare = new ArrayList<>();
        try {
            myDatabase.queryAllPreferShare(preferShare);
        } catch (Exception e) {

        } finally {
            myDatabase.close();
        }
        resolveInfos = ShareUtil.getAcInfo_SupportShare(mContext, ShareUtil.Type.Image);
        acInfo_SupportShare = ShareUtil.sortAndClearAcData(mContext, preferShare, resolveInfos);
    }

    public void setClickListener(final clickListenerInterface listenner) {
        this.listener = listenner;
        sureView.setOnClickListener(v -> {
            dialog.dismiss();
            listenner.saveResult(saveRatio);
        });
        cancleView.setOnClickListener(v -> {
            dialog.dismiss();
            listenner.mCancel();
        });
        shareRecyclerAdapter.setOnItemClickListener((view, data) -> {
//            LogUtil.d("Savaset", "分享受到点击");
            listenner.onShareItemClick(saveRatio, savePath -> {
                int clickPosition = acInfo_SupportShare.indexOf(data);
                ResolveInfo resolveInfo = resolveInfos.get(clickPosition);
                myQQShare = ShareUtil.share((AppCompatActivity) mContext, resolveInfo, savePath);
                dismissDialog();
            });
        });
    }

    public interface ShareTask {
        void share(@NotNull String path);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (myQQShare != null)
            myQQShare.onActivityResult(requestCode, resultCode, data);
    }

    private void setChoseSizeUi() {
        for (int i = 0; i < SIZE_COUNT; i++) {
            final int id = i;
            final TextView sizeView = sizeViewList.get(i);
            if (sourceSize * sizeList.get(i) * sizeList.get(i) > MAX_SIZE
                    && i > choseSizeId) {
                canClickList.set(i, false);
                sizeView.setBackground(Util.getDrawable(R.drawable.save_set_canot_chosed));
            } else {
                if (choseSizeId == i) {
                    sizeView.setBackground(Util.getDrawable(R.drawable.save_set_chosed));
                } else {
                    sizeViewList.get(i).setBackground(Util.getDrawable(R.drawable.save_set_notchosed));
                }
                sizeView.setOnClickListener(v -> {
                    if (id != choseSizeId) {
                        sizeViewList.get(choseSizeId).setBackground(Util.getDrawable(
                                R.drawable.save_set_notchosed));
                        v.setBackground(Util.getDrawable(R.drawable.save_set_chosed));
                        saveRatio = sizeList.get(id).floatValue();
                        choseSizeId = id;
                    }
                });
            }
        }
    }

    /**
     * 设置风格：无标题
     */
    private void setStyle() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
}

