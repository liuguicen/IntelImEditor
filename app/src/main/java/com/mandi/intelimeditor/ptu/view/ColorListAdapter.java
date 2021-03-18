package com.mandi.intelimeditor.ptu.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;

import java.util.ArrayList;
import java.util.Arrays;



public class ColorListAdapter extends RecyclerView.Adapter<ColorListAdapter.ColorLumpHolder> {

    /**
     * 预先定义的颜色
     */
    private ArrayList<Integer> preColors = new ArrayList<>(Arrays.asList(
            R.color.white,
            R.color.black,
            R.color.red,
            R.color.blue,
            R.color.transparent,
            R.color.grean,
            R.color.yellow,
            R.color.pink,
            R.color.purple,
            R.color.deepPurple,
            R.color.indigo,
            R.color.lightBlue,
            R.color.cyan,
            R.color.teal,
            R.color.lightGreen,
            R.color.lime,
            R.color.amber,
            R.color.orange,
            R.color.deepOrange,
            R.color.brown,
            R.color.grey,
            R.color.blueGrey
    ));

//    private ArrayList<Integer> colorList;
    private Context mContext;
    private int selectId = 0;
    private int mColor = Color.BLACK;
    private ItemClickListener itemClickListener;


    public interface ItemClickListener {
        void onItemClick(View view, int position, int color);
    }


    public ColorListAdapter(Context context) {
//        this.colorList = colorList;
        this.mContext = context;
    }

    @Override
    public ColorLumpHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_color, parent, false);
        return new ColorLumpHolder(view);
    }

    @Override
    public void onBindViewHolder(ColorLumpHolder holder, int position) {
        //设置颜色值
//        if (colorList.size() >= position && colorList.get(position) != null) {
//            mColor = colorList.get(position);
//        } else {
        mColor = Util.getColor(preColors.get(position));
//        }
        holder.circleView.setColor(mColor);
        //设置是否选中
        holder.circleView.setChecked(position == selectId);
        holder.circleView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                holder.circleView.setChecked(true);
                selectId = position;
                mColor = Util.getColor(preColors.get(position));
                itemClickListener.onItemClick(v, position, mColor);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return preColors.size();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    static class ColorLumpHolder extends RecyclerView.ViewHolder {
        ColorLumpCircle circleView;

        ColorLumpHolder(View itemView) {
            super(itemView);
            circleView = itemView.findViewById(R.id.stroke_color);
        }
    }
}