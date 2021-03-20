package com.mandi.intelimeditor.ptu.saveAndShare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.R;

import java.util.List;

/**
 * Created by liuguicen on 2016/9/8.
 */
class ShareRecyclerAdapter extends RecyclerView.Adapter<ShareViewHolder> implements View.OnClickListener {

    private Context mContext;
    private List<ShareItemData> shareAcInfo;

    ShareRecyclerAdapter(Context context, List<ShareItemData> shareAcInfo) {
        mContext = context;
        this.shareAcInfo = shareAcInfo;
    }

    interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, ShareItemData data);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    @Override
    public ShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout itemView = (LinearLayout) LayoutInflater.from(mContext).inflate(
                R.layout.item_list_save_set_share, parent, false);
        itemView.setOnClickListener(this);
        ImageView icon = itemView.findViewById(R.id.save_set_item_share_icon);
        TextView title = itemView.findViewById(R.id.save_set_item_share_title);

        icon.setTag("icon");
        title.setTag("title");

        return new ShareViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ShareViewHolder holder, int position) {
        holder.icon.setTag(shareAcInfo.get(position));
        holder.icon.setImageDrawable(shareAcInfo.get(position).getIcon());
        holder.title.setText(shareAcInfo.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return shareAcInfo.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            LogUtil.d("recyclerView受到点击");
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,
                    (ShareItemData) ((LinearLayout) v).getChildAt(0).getTag());
        }
    }

    void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        LogUtil.d("item受到点击0");
        this.mOnItemClickListener = listener;
    }
}
