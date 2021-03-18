package com.mandi.intelimeditor.ptu.cut;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;


/**
 * Created by Administrator on 2016/11/16 0016.
 */

public class ChoseRatioAdapter extends BaseAdapter {
    private Context mContext;


    private String[] ratios;
    private int textWidth;

    ChoseRatioAdapter(Context context, String[] ratios, int textWidth) {
        mContext = context;
        this.ratios = ratios;
        this.textWidth = textWidth;
    }

    @Override
    public int getCount() {
        return ratios.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = new TextView(mContext);
        tv.setWidth(textWidth);
        tv.setPadding(0, 10, 0, 10);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);
        tv.setTextColor(Util.getColor(R.color.text_deep_black));
        tv.setText(ratios[position]);
        return tv;
    }
}
