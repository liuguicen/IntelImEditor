package com.mandi.intelimeditor.home.tietuChoose;//package a.baozouptu.home.tietuChoose;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import a.baozouptu.R;
//import a.baozouptu.home.BasePicAdapter;
//
///**
// * Created by liuguicen on 2016/8/31.
// * 服务器上面的图片资源列表的适配器，目前用于模板和不同类别的贴图
// */
//public class PicTagAdapter extends BasePicAdapter {
//    private List<String> mTagList = new ArrayList<>();
//
//    public PicTagAdapter(Context mContext) {
//        super(mContext);
//    }
//
//    public void setNewData(List<String> data) {
//        mTagList.clear();
//        mTagList.addAll(data);
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return new PicTagViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_pic_tag, parent, false));
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        PicTagViewHolder picTagViewHolder = (PicTagViewHolder) holder;
//        picTagViewHolder.mTagTv.setText(mTagList.get(position));
//    }
//
//    @Override
//    public int getItemCount() {
//        return mTagList.size();
//    }
//
//    public class PicTagViewHolder extends RecyclerView.ViewHolder {
//        public TextView mTagTv;
//
//        public PicTagViewHolder(@NonNull View itemView) {
//            super(itemView);
//            mTagTv = itemView.findViewById(R.id.tv_pic_tag);
//        }
//    }
//
//}
