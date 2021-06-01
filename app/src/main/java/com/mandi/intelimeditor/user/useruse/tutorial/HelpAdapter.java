package com.mandi.intelimeditor.user.useruse.tutorial;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.mandi.intelimeditor.R;

import java.util.ArrayList;
import java.util.List;

import com.mandi.intelimeditor.common.util.CoverLoader;


public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.FAQViewHolder> {

    private List<Tutorial> guideUsBeans = new ArrayList<>();
    private Context mContext;

    public HelpAdapter(Context context) {
        mContext = context;
        guideUsBeans = GuideData.allGuideUseData;
    }

    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new FAQViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faq, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int i) {
        Tutorial info = guideUsBeans.get(i);
        if (info.isShowMore()) {
            holder.showMoreInfoIv.setImageResource(R.drawable.ic_arrow_up);
            holder.moreInfoTv.setVisibility(View.VISIBLE);
            if (info.getGifFile() != null) {
                CoverLoader.INSTANCE.loadOriginImageView(mContext, info.getGifFile().getUrl(), holder.gifResIv);
                holder.gifResIv.setVisibility(View.VISIBLE);
            } else {
                holder.gifResIv.setVisibility(View.GONE);
            }
        } else {
            holder.showMoreInfoIv.setImageResource(R.drawable.ic_arrow_down);
            holder.moreInfoTv.setVisibility(View.GONE);
            holder.gifResIv.setVisibility(View.GONE);
        }
        holder.titleTv.setText(info.getTitle());
        holder.moreInfoTv.setText(info.getContent());
        holder.itemView.setOnClickListener(v -> {
            info.setShowMore(!info.isShowMore());
            notifyItemChanged(i);
        });
    }

    @Override
    public int getItemCount() {
        return guideUsBeans.size();
    }

    static class FAQViewHolder extends RecyclerView.ViewHolder {

        TextView titleTv;
        TextView moreInfoTv;
        ImageView showMoreInfoIv, gifResIv;

        FAQViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.titleTv);
            moreInfoTv = itemView.findViewById(R.id.moreInfoTv);
            gifResIv = itemView.findViewById(R.id.gifResIv);
            showMoreInfoIv = itemView.findViewById(R.id.showMoreInfoIv);
        }
    }
}
