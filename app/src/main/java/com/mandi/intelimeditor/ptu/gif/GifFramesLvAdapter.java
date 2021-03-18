package com.mandi.intelimeditor.ptu.gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;

import org.jetbrains.annotations.NotNull;




/**
 * Created by liuguicen on 2016/6/17.
 */
public class GifFramesLvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "TietuRecyclerAdapter";
    private static final int HIDE_ITEM = 0;
    public static final int NORMAL_ITEM = 1;
    /**
     * 帧数多的时候，缩帧，减少显示出来的帧的量，目前会出问题，且用户P图有些帧看不到，有问题，所以目前不用
     */
    // private int mShrinkLen = -1;

    private Context mContext;
    private GifFrame[] gifFrames;

    public GifFramesLvAdapter(Context context, GifFrame[] gifFrames) {
        mContext = context;
        this.gifFrames = gifFrames;
        // 减少显示的帧，避免用户滑动太多，有问题，目前不用
        // if (mTotalLen > GifManager.MAX_FRAME_LENGTH) {
        //     mShrinkLen = mTotalLen / GifManager.MAX_FRAME_LENGTH;
        //     if (mTotalLen % GifManager.MAX_FRAME_LENGTH != 0) {
        //         mShrinkLen++;
        //     }
        // }
    }

    public void setData(GifFrame[] frames) {
        this.gifFrames = frames;
        notifyDataSetChanged();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(MyViewHolder viewHolder, int position);

        boolean onItemLongClick(MyViewHolder viewHolder, int position);
    }

    private OnRecyclerViewItemClickListener mItemClickListener = null;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == HIDE_ITEM) {
            View view = new View(mContext);
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(0, 0);
            view.setLayoutParams(layoutParams);
            return new HideViewHolder(view);
        }
        FrameLayout itemView = (FrameLayout) LayoutInflater.from(mContext).inflate(
                R.layout.item_gif_frame_list, parent, false);

        ImageView imageView = new ImageView(mContext);
        int pad = Util.dp2Px(3);
        int spad = Util.dp2Px(2);
        imageView.setPadding(spad, pad, spad, pad);
        imageView.setAdjustViewBounds(true);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        itemView.addView(imageView);
        return new MyViewHolder(itemView, imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof MyViewHolder) {
            MyViewHolder holder = (MyViewHolder) viewHolder;
            holder.itemView.setOnClickListener(v -> mItemClickListener.onItemClick(holder, position));
            holder.itemView.setOnLongClickListener(v -> mItemClickListener.onItemLongClick(holder, position));

            holder.itemView.setVisibility(View.VISIBLE);
            Bitmap bitmap = gifFrames[position].bm;
            holder.iv.setImageBitmap(bitmap);
            if (gifFrames[position].isChosen) {
                //getColor(int)已过时的替换方案
                holder.iv.setBackgroundColor(ContextCompat.getColor(mContext, R.color.gif_frame_choose));
            } else {
                holder.iv.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    @Override
    public int getItemCount() {
        return gifFrames.length;
    }

    @Override
    public int getItemViewType(int position) {
        // if (position % mShrinkLen == 0 || position == mTotalLen - 1) {
        return NORMAL_ITEM;
        // } else {
        //     return HIDE_ITEM;
        // }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void clear() {
        gifFrames = new GifFrame[0];
        notifyDataSetChanged();
    }

    /**
     * 其中的ImageView iv会放入路径
     */
    static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv;

        public MyViewHolder(View itemView, ImageView iv) {
            super(itemView);
            this.iv = iv;
        }
    }

    private static class HideViewHolder extends RecyclerView.ViewHolder {
        public HideViewHolder(View itemView) {
            super(itemView);
        }
    }
}

