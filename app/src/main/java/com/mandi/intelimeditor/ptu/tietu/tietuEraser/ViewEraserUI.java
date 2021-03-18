package com.mandi.intelimeditor.ptu.tietu.tietuEraser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.mandi.intelimeditor.ptu.common.PTuUIUtil;
import com.mathandintell.intelimeditor.R;

import org.jetbrains.annotations.NotNull;



/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/11/03
 *      version : 1.0
 * <pre>
 */
public class ViewEraserUI {

    @NotNull
    private final EraseUIInterface mTietuEraserInterFace;
    private final Context mContext;

    public ViewEraserUI(Context context, @NotNull View curChosenView, @NotNull EraseUIInterface eraseUIInterface, View root) {
        this.mTietuEraserInterFace = eraseUIInterface;
        this.mContext = context;
        root.findViewById(R.id.tietu_eraser_size).setOnClickListener(v -> {
            View contentView = createSizeContentView(context, curChosenView);
            PTuUIUtil.addPopOnFunctionLayout(mContext, contentView, root);
        });

        root.findViewById(R.id.tietu_eraser_blur).setOnClickListener(v -> {
            View contentView = createBlurPopView(context, curChosenView);
            PTuUIUtil.addPopOnFunctionLayout(mContext, contentView, root);
        });
        root.findViewById(R.id.tietu_eraser_finish).setOnClickListener(v -> {
            eraseUIInterface.finishErase();
        });
    }

    private View createSizeContentView(Context context, @NotNull View view) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.seek_bar_layout, null);
        RangeSeekBar seekBar = contentView.findViewById(R.id.seek_bar_popw);
        TextView valueTv = contentView.findViewById(R.id.seek_bar_value_tv);
        int maxWidth = view.getWidth();
        seekBar.setRange(0, maxWidth);
        seekBar.setProgress((int) mTietuEraserInterFace.getPaintWidth());
        valueTv.setText(String.valueOf(mTietuEraserInterFace.getPaintWidth()));
        seekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                int intValue = (int) leftValue;
                mTietuEraserInterFace.setEraserWidth(intValue);
                valueTv.setText(String.valueOf(intValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
        return contentView;
    }

    private View createBlurPopView(Context context, View view) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.seek_bar_layout, null);
        RangeSeekBar seekBar = contentView.findViewById(R.id.seek_bar_popw);
        TextView valueTv = contentView.findViewById(R.id.seek_bar_value_tv);
        int maxWidth = view != null ? view.getWidth() / 2 : 150;
        seekBar.setRange(0, maxWidth);
        seekBar.setProgress((int) mTietuEraserInterFace.getBlurWidth());
        valueTv.setText(String.valueOf(mTietuEraserInterFace.getBlurWidth()));
        seekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                mTietuEraserInterFace.setEraserBlurWidth(leftValue);
                valueTv.setText(String.valueOf((int) leftValue));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                mTietuEraser.setEraserBlurWidth(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
        return contentView;
    }


    public interface EraseUIInterface {
        void finishErase();

        int getPaintWidth();

        void setEraserWidth(float progress);

        int getBlurWidth();

        void setEraserBlurWidth(float progress);
    }
}
