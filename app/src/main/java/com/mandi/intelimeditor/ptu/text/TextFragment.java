package com.mandi.intelimeditor.ptu.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.common.Constants.EventBusConstants;
import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.dialog.FirstUseDialog;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.PtuData;
import com.mandi.intelimeditor.ptu.common.SimpleEraser;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.repealRedo.TextStepData;
import com.mandi.intelimeditor.ptu.threeLevelFunction.ThreeLevelToolUtil;
import com.mandi.intelimeditor.ptu.view.ColorPicker;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.dialog.UnlockDialog;
import com.mandi.intelimeditor.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * ?????????????????????fragment
 * Created by Administrator on 2016/5/1.
 */
public class TextFragment extends BasePtuFragment {
    private TextPopUpBuilder textPopupBuilder;
    private FloatTextView floatTextView;
    private String TAG = "TextFragment";
    private SimpleEraser eraser;
    private boolean isInRubber = false;
    RepealRedoListener repealRedoListener;
    PtuSeeView ptuSeeView;
    private PTuActivityInterface pTuActivityInterface;
    /**
     * ????????????????????????
     */
    private List<FunctionInfoBean> mRubberFunctionList;

    @Override
    public void onResume() {
        textPopupBuilder = new TextPopUpBuilder(getActivity(), floatTextView, this);
        textPopupBuilder.setEraser(eraser);
        super.onResume();
    }

    @Override
    public int getEditMode() {
        return PtuUtil.EDIT_TEXT;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_first_function_normal;
    }

    @Override
    public List<FunctionInfoBean> getFunctionList() {
        //????????????????????????
        pFunctionList.clear();
        pFunctionList.add(new FunctionInfoBean(R.string.text_style, R.drawable.round_format_bold, PtuUtil.EDIT_REND));
        pFunctionList.add(new FunctionInfoBean(R.string.text_color, R.drawable.round_color_lens, PtuUtil.EDIT_REND));
        pFunctionList.add(new FunctionInfoBean(R.string.text_font, R.drawable.round_text_format, PtuUtil.EDIT_REND));
        // ???sp????????????????????????
        pFunctionList.add(new FunctionInfoBean(R.string.rubber, R.mipmap.eraser, PtuUtil.EDIT_REND));
        pFunctionList.add(new FunctionInfoBean(R.string.tools, R.drawable.round_build, PtuUtil.EDIT_TEXT));
        return pFunctionList;
    }

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        this.ptuSeeView = ptuActivity.getPtuSeeView();
        this.repealRedoListener = ptuActivity.getRepealRedoListener();
        repealRedoListener.canRepeal(false);
        repealRedoListener.canRedo(false);
    }

    @Override
    public boolean onSure() {
        if (isInRubber) {
            switchRubber();
            return true;
        }
        return false;
    }

    /**
     * ??????addTextFragment??????view??????
     */
    @Override
    public void initView() {
        super.initView();
        EventBus.getDefault().register(this);
    }


    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        super.onItemClick(adapter, view, position);
        if (pFunctionList.get(position).isLocked()) {
            UnlockDialog unlockDialog = UnlockDialog.Companion.newInstance();
            unlockDialog.setUnlockListener(result -> {
                LogUtil.d(TAG, "video result $result");
                if (result) {
                    pFunctionList.get(position).setLocked(false);
                    pFunctionAdapter.notifyItemChanged(position);
                }
                return null;
            });
            unlockDialog.showIt(getActivity());
            return;
        }
        int id = isInRubber ? mRubberFunctionList.get(position).getTitleResId()
                : pFunctionList.get(position).getTitleResId();
        switch (id) {
            case R.string.size:
                textPopupBuilder.setToumingduPopWindow(view);
                break;
            case R.string.text_style:
                US.putPTuTextEvent(US.PTU_TEXT_STYLE);
                textPopupBuilder.setStylePopWindow(view);
                break;
            case R.string.text_color:
                US.putPTuTextEvent(US.PTU_TEXT_COLOR);
                textPopupBuilder.setColorPopWindow(view, new ColorPicker.ColorTarget() {
                    @Override
                    public void setColor(int color) {
                        if (floatTextView.isClickable()) {
                            floatTextView.setTextColor(color);
                        } else {
                            eraser.setColor(color);
                        }
                    }

                    @Override
                    public int getCurColor() {
                        if (floatTextView.isClickable()) {
                            return floatTextView.getCurrentTextColor();
                        } else {
                            return eraser.getColor();
                        }
                    }
                });
                break;
            case R.string.text_font:
                US.putPTuTextEvent(US.PTU_TEXT_TYPEFACE);
                textPopupBuilder.setTypefacePopWindow(view);
                break;
            case R.string.rubber:
                US.putPTuTextEvent(US.PTU_TEXT_RUBBER);
                if (!PtuData.ptuConfig.hasReadTextRubber()) {
                    final FirstUseDialog firstUseDialog = new FirstUseDialog(getContext());
                    firstUseDialog.createDialog("??????????????????,????????????????????????????????????", "", () -> {
                        PtuData.ptuConfig.writeConfig_TextRubber(true);
                    });
                }
                switchRubber();
                break;
            case R.string.tools:
                showTextTools(view);
                break;
            default:
                break;
        }
    }

    private void showTextTools(View txtTools) {
        List<Integer> iconIdList = new ArrayList<>(Arrays.asList(R.drawable.flip, R.mipmap.transparency,
                R.drawable.reversal, R.drawable.diag_icon,
                R.drawable.big_and_small, R.drawable.vertical));
        List<Integer> nameIdList = new ArrayList<>(Arrays.asList(R.string.cut_flip, R.string.text_transparency,
                R.string.reversal, R.string.dialog,
                R.string.big_and_small, R.string.vertical));

        RcvItemClickListener1 rcvItemClickListener = (itemHolder, view) -> {
            int position = itemHolder.getLayoutPosition();
            if (position < 0) return;
            InsertAd.onClickTarget(getActivity());
            switch (nameIdList.get(position)) {
                case R.string.cut_flip:
                    US.putPTuTextEvent(US.PTU_TEXT_FLIP);
                    flipText();
                    break;
                case R.string.reversal:
                    US.putPTuTextEvent(US.PTU_TEXT_REVERSAL);
                    reversalText();
                    break;
                case R.string.big_and_small:
                    US.putPTuTextEvent(US.PTU_TEXT_BIG_AND_SMALL);
                    bigAndSmallText();
                    break;
                case R.string.vertical:
                    US.putPTuTextEvent(US.PTU_TEXT_VERTICAL);
                    verticalText();
                    break;
                case R.string.text_transparency:
                    US.putPTuTextEvent(US.PTU_TEXT_TRANSPARENT);
                    textPopupBuilder.setToumingduPopWindow(pFunctionRcv);
                    break;
                case R.string.dialog:
                    US.putPTuTextEvent(US.PTU_TEXT_DIALOG);
                    showTextDialogList();
            }
        };
        ThreeLevelToolUtil.showToolsRcvWindow(getContext(),
                txtTools,
                2,
                rcvItemClickListener,
                iconIdList,
                R.drawable.function_background_text_yellow,
                nameIdList);
    }

    /**
     * ???????????????????????????
     */
    private void showTextDialogList() {
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.popup_text_dialog_list, null);
        RecyclerView dialogRcv = layout.findViewById(R.id.text_dialog_rcv);
        dialogRcv.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // ????????????resource???????????????????????????????????????????????????
        int[] dialogIdList = {
                R.drawable.ic_remove,
                R.drawable.text_dialog3,
                R.drawable.text_dialog4,
                R.drawable.text_dialog5,
                R.drawable.text_dialog6,
                R.drawable.text_dialog1,
                R.drawable.text_dialog2,
                R.drawable.text_dialog7,
                R.drawable.text_dialog8,
        };
        TextDialogAdapter textDialogAdapter = new TextDialogAdapter(getContext(), dialogIdList);
        textDialogAdapter.setOnItemClickListener((itemHolder, view, position) -> {
            if (0 == itemHolder.getLayoutPosition()) {
                floatTextView.setDialogBg(null);
            }
            Bitmap dialogBm = BitmapFactory.decodeResource(getResources(),
                    dialogIdList[itemHolder.getLayoutPosition()]);
            floatTextView.setDialogBg(dialogBm);
        });
        dialogRcv.setAdapter(textDialogAdapter);
        PopupWindow dialogListPopup = ThreeLevelToolUtil.getPopWindow_for3LevelFunction(getActivity());
        dialogListPopup.setHeight((int) (getResources().getDimensionPixelSize(R.dimen.bottom_function_layout_height) * 1.2));
        dialogListPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        dialogListPopup.setContentView(layout);
        dialogListPopup.showAtLocation(pFunctionRcv, Gravity.START | Gravity.BOTTOM, 0, 0);
    }

    private void verticalText() {
        String text = floatTextView.getString();
        StringBuilder tsb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            tsb.append(text.charAt(i));
            tsb.append("\n");
        }
        floatTextView.setText(tsb);
    }

    private void bigAndSmallText() {
        floatTextView.setBigAndSmallText();
    }

    private void reversalText() {
        Editable editable = floatTextView.getText();

        String text = editable == null ? "" : editable.toString();
        StringBuilder reversedText = new StringBuilder();
        for (int i = text.length() - 1; i >= 0; i--) {
            reversedText.append(text.charAt(i));
        }
        floatTextView.setText(reversedText);
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     * bitmap??????matrix???setPolyToPoly?????????
     */
    private void flipText() {
        floatTextView.flip();
    }


    private void switchRubber() {
        if (!floatTextView.isClickable()) {//????????????
            isInRubber = false;
            floatTextView.setClickable(true);
            pFunctionAdapter.setNewInstance(pFunctionList);
        } else {//????????????
            isInRubber = true;
            floatTextView.setClickable(false);
            if (mRubberFunctionList == null) {
                mRubberFunctionList = new ArrayList<>();
                mRubberFunctionList.add(new FunctionInfoBean(R.string.size, R.mipmap.fixed_size, PtuUtil.EDIT_REND));
                mRubberFunctionList.add(new FunctionInfoBean(R.string.text_color, R.drawable.round_color_lens, PtuUtil.EDIT_REND));
                mRubberFunctionList.add(new FunctionInfoBean(R.string.rubber, R.drawable.ic_tick, PtuUtil.EDIT_REND));
            }
            pFunctionAdapter.setNewInstance(mRubberFunctionList);
        }
    }

    public void setFloatView(FloatTextView floatView) {
        this.floatTextView = floatView;
        floatTextView.setTypeface(Typeface.DEFAULT);
    }

    public void releaseResource() {
        floatTextView.releaseResource();
        EventBus.getDefault().unregister(this);
    }

    public void setRepealRedoListener(RepealRedoListener repealRedoListener) {
        this.repealRedoListener = repealRedoListener;
        repealRedoListener.canRepeal(false);
        repealRedoListener.canRedo(false);
    }

    public void addRubberView(Context context, PtuFrameLayout ptuFrame) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ptuSeeView.getDstRect().width(), ptuSeeView.getDstRect().height());
        params.setMargins(ptuSeeView.getDstRect().left, ptuSeeView.getDstRect().top, 0, 0);
        eraser = new SimpleEraser(context, pTuActivityInterface);
        eraser.setRepealRedoListener(repealRedoListener);
        eraser.setIsShowTouchPEnlarge(false);
        ptuFrame.addView(eraser, params);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pFunctionAdapter = null; // ??????????????????????????????????????????????????????????????????????????????????????????
        EventBus.getDefault().unregister(this); // ??????????????????,????????????????????????
    }

    @Override
    public void onDestroy() {
        //textPopBuilder?????????????????????
        super.onDestroy();
        textPopupBuilder = null;
//        pTuActivityInterface = null; ????????????????????????????????????????????????????????????
    }

    @Override
    public void smallRepeal() {
        eraser.smallRepeal();
    }

    @Override
    public void smallRedo() {
        eraser.smallRedo();
    }

    @Override
    public void generateResultDataInMain(float ratio) {
        floatTextView.generateResultDataInMain(ptuSeeView);
    }

    @Override
    public StepData getResultDataAndDraw(float ratio) {
        //?????????????????????
        if (getActivity() != null) {
            Util.hideInputMethod(getActivity(), floatTextView);
        }
        Bitmap textResultBm = floatTextView.getResultBm();
        TextStepData tsd = floatTextView.getResultStepData();
        if (tsd == null) {
            tsd = new TextStepData(PtuUtil.EDIT_TEXT);
        }
        if (textResultBm != null) {
            String tempPath = FileTool.createTempPicPath();
            TextStepData finalTsd = tsd;
            BitmapUtil.asySaveTempBm(tempPath, textResultBm, new Observer<String>() {
                @Override
                public void onComplete() {
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(String realPath) {
                    finalTsd.picPath = realPath;
                }
            });
            tsd.picPath = tempPath;
        }
        if (eraser != null) {
            tsd.setRubberDate(eraser.getResultData());
        }

        //????????????
        tsd.rotateAngle = floatTextView.getRotation();
        addBigStep(tsd, textResultBm, pTuActivityInterface);
        return tsd;
    }

    public static void addBigStep(StepData sd, PTuActivityInterface pTuActivityInterface) {
        addBigStep((TextStepData) sd, sd.picPath == null ? null : BitmapUtil.decodeLosslessBitmap(sd.picPath), pTuActivityInterface);
    }

    /**
     * ????????????bitmap?????????????????????????????????
     *
     * @param textAddBm ??????????????????
     */
    private static void addBigStep(TextStepData tsd, Bitmap textAddBm, PTuActivityInterface pTuActivityInterface) {
        PtuSeeView ptuSeeView = pTuActivityInterface.getPtuSeeView();
        //???????????????????????????
        ArrayList<Pair<Path, Paint>> pathPaintList = tsd.getRubberData();
        GifManager gifManager = pTuActivityInterface.getGifManager();
        if (gifManager == null) {
            Canvas sourceCanvas = ptuSeeView.getSourceCanvas();
            drawRubber(sourceCanvas, pathPaintList);
            PtuUtil.addBm2Canvas(sourceCanvas, textAddBm, tsd.boundRectInPic, tsd.rotateAngle);
            // ??????
            if (pathPaintList != null) {
                ptuSeeView.postInvalidate();
            } else if (textAddBm != null) {
                ptuSeeView.post(() -> ptuSeeView.resetShow());
            }

        } else {
            GifFrame[] gifFrames = gifManager.getFrames();
            Canvas canvas = new Canvas();
            for (GifFrame gifFrame : gifFrames) {
                if (gifFrame.isChosen) {
                    canvas.setBitmap(gifFrame.bm);
                    drawRubber(canvas, pathPaintList);
                    PtuUtil.addBm2Canvas(canvas, textAddBm, tsd.boundRectInPic, tsd.rotateAngle);
                }
            }
            ptuSeeView.postInvalidate();
        }
    }

    private static void drawRubber(Canvas canvas, List<Pair<Path, Paint>> pathPaintList) {
        if (pathPaintList != null) {//??????????????????
            for (Pair<Path, Paint> pair : pathPaintList) {
                canvas.drawPath(pair.first, pair.second);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {
        if (EventBusConstants.GIF_PLAY_CHOSEN.equals(event)) {
            floatTextView.setVisibility(View.VISIBLE);
        } else if (EventBusConstants.GIF_PLAY_UN_CHOSEN.equals(event)) {
            floatTextView.setVisibility(View.INVISIBLE);
        }
    }

    public boolean onBackPressed(boolean isFromKey) {
        if (isInRubber) {
            switchRubber();
            return true;
        }
        if (pTuActivityInterface != null) {
            GifManager gifManager = pTuActivityInterface.getGifManager();
            if (isFromKey && gifManager != null && gifManager.getFrames() != null
                    && gifManager.getFrames().length != gifManager.getFrameChooseCount()
                    && !TextUtils.isEmpty(floatTextView.getText())) {
                if (Util.DoubleClick.isDoubleClick(2500)) {
                    return false;
                } else {
                    ToastUtils.show("???????????????????????????????????????");
                    return true;
                }
            }
        }
        return false;
    }
}