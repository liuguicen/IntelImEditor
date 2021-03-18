package com.mandi.intelimeditor.ptu.threeLevelFunction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;

import java.util.List;



/**
 * Created by Administrator on 2016/11/16 0016.
 */

public class ThreeLevelToolsAdapter extends RecyclerView.Adapter<ThreeLevelToolsAdapter.ToolItemViewHolder> {
    private final List<Integer> iconIdList;
    private Context mContext;
    private RcvItemClickListener1 itemClickListener;


    private List<Integer> nameIdList;
    private int bgId;

    public ThreeLevelToolsAdapter(Context context, List<Integer> iconIdList, List<Integer> nameIdList, int bgID) {
        mContext = context;
        this.nameIdList = nameIdList;
        this.iconIdList = iconIdList;
        this.bgId = bgID;
    }

    @NonNull
    @Override
    public ToolItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_tietu_3level_function, viewGroup, false);
        ToolItemViewHolder viewHolder = new ToolItemViewHolder(view);
        view.setOnClickListener(v -> itemClickListener.onItemClick(viewHolder, v));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ToolItemViewHolder toolItemViewHolder, int position) {
        toolItemViewHolder.icon.setBackground(Util.getDrawable(bgId));
        toolItemViewHolder.icon.setImageResource(iconIdList.get(position));
        toolItemViewHolder.name.setText(mContext.getString(nameIdList.get(position)));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return nameIdList.size();
    }

    static class ToolItemViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;

        public ToolItemViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.tietu_3_level_function_icon);
            name = itemView.findViewById(R.id.tietu_3_level_function_name);
        }
    }

    public void setOnItemClickListener(RcvItemClickListener1 itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
