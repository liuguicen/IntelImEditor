package com.mandi.intelimeditor.user.useruse.tutorial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.mandi.intelimeditor.R;

import java.util.List;

import util.CoverLoader;


public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.TutorialViewHolder> {

    private List<Tutorial> data;
    private Context mContext;

    public GuideAdapter(Context context, List<Tutorial> data) {
        mContext = context;
        this.data = data;
    }

    public void setList(List<Tutorial> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private ItemClickListener itemClickListener;

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public TutorialViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        return new TutorialViewHolder(LayoutInflater.from(mContext).inflate(R.layout.dialog_tutorial_item, parent, false));
    }

    @Override
    public void onBindViewHolder(TutorialViewHolder holder, int i) {
        Tutorial bean = data.get(i);
        holder.titleTv.setText(bean.getTitle());
        if (bean.getGifFile() != null) {
            holder.imageContentIv.setVisibility(View.VISIBLE);
            holder.textContentTv.setVisibility(View.GONE);
            CoverLoader.INSTANCE.loadOriginImageView(mContext, bean.getGifFile().getUrl(), holder.imageContentIv, R.drawable.loading);
        } else {
            holder.textContentTv.setText(bean.getContent());
            holder.imageContentIv.setVisibility(View.GONE);
            holder.textContentTv.setVisibility(View.VISIBLE);
        }
        holder.imageContentIv.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(v, i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class TutorialViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv, textContentTv;
        ImageView imageContentIv;

        TutorialViewHolder(View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.titleTv);
            textContentTv = itemView.findViewById(R.id.contentTv);
            imageContentIv = itemView.findViewById(R.id.image_content_iv);
        }
    }
}
