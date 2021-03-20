package com.mandi.intelimeditor.home.localPictuture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mandi.intelimeditor.home.data.PicDirInfo;
import com.mandi.intelimeditor.R;

import java.util.List;



/**
 * 使用继承BaseAdapter处理ListView的图片显示
 *
 * @author acm_lgc
 */
public class MyFileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int FILE_CHOOSE = 0;
    private static final int SYSTEM_CHOOSE = 1;
    private final List<PicDirInfo> picDirInfos;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public MyFileListAdapter(Context context, List<PicDirInfo> picDirInfos) {
        mContext = context;
        this.picDirInfos = picDirInfos;
        layoutInflater = LayoutInflater.from(mContext);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == SYSTEM_CHOOSE) {
            View view = layoutInflater.inflate(R.layout.item_system_choose_pic, viewGroup, false);
            return new SystemChooseHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.item_list_picfile, viewGroup, false);
            return new FileChooseHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof FileChooseHolder) {
            FileChooseHolder fileHolder = ((FileChooseHolder) holder);
            fileHolder.mTvInfo.setText(picDirInfos.get(position).getPicNumInfo());
            String path = picDirInfos.get(position).getRepresentPicPath();
            // Logcat.e("加载位置=："+position + " 路径:" + path);
            RequestOptions options = new RequestOptions()
                    .error(R.mipmap.instead_icon)
                    .placeholder(R.mipmap.instead_icon);
            Glide.with(mContext)
                    .load(path)
                    .apply(options)
                    .into(fileHolder.mIvFile);
        } else if (holder instanceof SystemChooseHolder) {
            SystemChooseHolder systemChooseHolder = (SystemChooseHolder) holder;
            systemChooseHolder.mTvInfo.setText(R.string.choose_pic_from_system);
            systemChooseHolder.mIvFile.setImageResource(R.mipmap.system_photo);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(holder, position);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == picDirInfos.size())
            return SYSTEM_CHOOSE;
        else return FILE_CHOOSE;
    }

    @Override
    public int getItemCount() {
        return picDirInfos.size() + 1;
    }


    /**
     * 点击事件
     */
    public interface OnItemClickListener {
        void onItemClick(RecyclerView.ViewHolder viewHolder, int position);
    }


    private OnItemClickListener listener;

    /**
     * 设置点击事件
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    private class FileChooseHolder extends RecyclerView.ViewHolder {
        ImageView mIvFile;
        TextView mTvInfo;

        FileChooseHolder(View itemView) {
            super(itemView);
            mIvFile = itemView.findViewById(R.id.iv_pic);
            mTvInfo = itemView.findViewById(R.id.tv_pic_file_name);
        }
    }

    private class SystemChooseHolder extends RecyclerView.ViewHolder {
        ImageView mIvFile;
        TextView mTvInfo;

        SystemChooseHolder(View itemView) {
            super(itemView);
            mIvFile = itemView.findViewById(R.id.iv_pic);
            mTvInfo = itemView.findViewById(R.id.tv_pic_file_name);
        }
    }
}
