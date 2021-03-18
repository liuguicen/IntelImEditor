package com.mandi.intelimeditor.ptu.draw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mathandintell.intelimeditor.R;

import java.util.ArrayList;
import java.util.List;



public class StyleAdapter extends RecyclerView.Adapter<StyleAdapter.StyleViewHolder> {

    private List<Integer> icons = new ArrayList<>();
    private Context mContext;

    public StyleAdapter(Context context) {
        mContext = context;
        icons.add(R.mipmap.icon);
        icons.add(R.mipmap.icon);
        icons.add(R.mipmap.icon);
        icons.add(R.mipmap.icon);
        icons.add(R.drawable.graffiti_btn_eraser);
    }

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @NonNull
    @Override
    public StyleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new StyleViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_style, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StyleViewHolder holder, int i) {
        holder.iconIv.setImageResource(icons.get(i));
        holder.itemView.setOnClickListener(v -> {
            notifyItemChanged(i);
            if (itemClickListener != null) {
                itemClickListener.onItemClick(holder.iconIv, i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    class StyleViewHolder extends RecyclerView.ViewHolder {

        ImageView iconIv;

        StyleViewHolder(@NonNull View itemView) {
            super(itemView);
            iconIv = itemView.findViewById(R.id.iv_icon);
        }
    }
}
