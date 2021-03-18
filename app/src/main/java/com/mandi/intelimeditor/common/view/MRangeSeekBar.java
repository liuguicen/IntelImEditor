package com.mandi.intelimeditor.common.view;

import android.content.Context;
import android.util.AttributeSet;

import com.jaygoo.widget.RangeSeekBar;

public class MRangeSeekBar extends RangeSeekBar {
    public MRangeSeekBar(Context context) {
        super(context);
        setSaveEnabled(false);//  这个估计自定义得有bug，无法自动恢复状态，加上这句
    }

    public MRangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(false);
    }

}
