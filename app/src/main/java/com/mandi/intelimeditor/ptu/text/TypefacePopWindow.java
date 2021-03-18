package com.mandi.intelimeditor.ptu.text;//package a.baozouptu.ptu.text;
//
//import android.app.Activity;
//import android.graphics.Typeface;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//import java.io.File;
//import java.util.ArrayList;
//
//import a.baozouptu.R;
//import a.baozouptu.ad.tencentAD.InsertAd;
//import a.baozouptu.common.dataAndLogic.AllData;
//import a.baozouptu.common.util.Logcat;
//import a.baozouptu.common.util.Util;
//import a.baozouptu.common.view.HorizontalListView;
//import a.baozouptu.network.FileDownloader;
//import a.baozouptu.network.TypefaceDownloader;
//import a.baozouptu.ptu.text.TextTypeAdapter;
//
///**
// * Created by LiuGuicen on 2017/1/20 0020.
// * 注意使用弱引用的方式持有，外部都是弱引用，不要在内部被反向向持有了
// * 注意这里contentView的监听器是持有TypefacePopWindow的，contentView被window持有，
// * <p>
// * <p>window消失之后监听器当做强引用方式回收，这时相当于TypefacePopWindow没被引用了那样回收
// * 既FunctionPopWindowBuilder还在，TypefacePopWindow相当于不存在了
// */
//
//public class TypefacePopWindow {
//    private Activity activity;
//    FunctionPopWindowBuilder textPopupBuilder;
//    private final FloatTextView floatTextView;
//
//    private int lastChooseFontId = 0;
//    private ArrayList<Typeface> typefaceList;
//
//    TypefacePopWindow(Activity activity, FunctionPopWindowBuilder textPopupBuilder, FloatTextView floatTextView) {
//        this.activity = activity;
//        this.textPopupBuilder = textPopupBuilder;
//        this.floatTextView = floatTextView;
//        initTypeface();
//    }
//
//
//    private void initTypeface() {
//        if (typefaceList == null) {
//            typefaceList = new ArrayList<>(TypefaceDownloader.typefaceNames.size());
//            typefaceList.add(null);
//        }
//        for (int i = 1; i < TypefaceDownloader.typefaceNames.size(); i++) {
//            typefaceList.add(null);
//        }
//
////        for (int i = 1; i < TypefaceDownloader.typefaceNames.size(); i++) {
////            try {
////                Typeface typeface = Typeface.createFromFile(AllData.zitiDir + TypefaceDownloader.typefaceNames.get(i));
////                typefaceList.add(typeface);
////            } catch (Exception e) {
////                typefaceList.add(null);
////                //如果是损坏的文件，删除它
////                File file = new File(AllData.zitiDir + TypefaceDownloader.typefaceNames.get(i));
////                if (file.exists())
////                    file.delete();
////            }
////        }
//    }
//
//    class TypefaceAdapter extends BaseAdapter {
//        @Override
//        public int getCount() {
//            return TypefaceDownloader.typefaceChinese.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return null;
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            TextView textView = new TextView(activity);
//            textView.setTextSize(25);
//            /**设置颜色*/
//            if (position == lastChooseFontId) {
//                textView.setTextColorWithOpacity(Util.getColor(R.color.text_checked_color));
//            } else {
//                textView.setTextColorWithOpacity(Util.getColor(R.color.text_default_color));
//            }
//
//            //设置字体
//            textView.setTag(TypefaceDownloader.typefaceNames.get(position));
//            if (position == 0) {
//                textView.setTextSize(30);//注意这里，英文字号增大了一些
//            } else {
//                Typeface typeface = typefaceList.get(position);
//                if (typeface != null)
//                    textView.setTypeface(typeface);
//            }
//
//            //其他的属性
//            textView.setGravity(Gravity.CENTER);
//            textView.setText(TypefaceDownloader.typefaceChinese.get(position));
//            HorizontalListView.LayoutParams layoutParams = new HorizontalListView.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            textView.setLayoutParams(layoutParams);
//            textView.setGravity(Gravity.CENTER);
//
//            return textView;
//        }
//    }
//
//    View createTypefacePopWindow() {
//
////        View contentView = LayoutInflater.from(activity).inflate(R.layout.popwindow_text_typeface, null);
////        HorizontalListView horizontalListView = contentView.findViewById(R.id.hList_text_type);
////
////        TypefaceAdapter typefaceAdapter = new TypefaceAdapter();
////        horizontalListView.setAdapter(typefaceAdapter);
////        horizontalListView.setOnItemClickListener(
////                (parent, view, position, id) -> {
////                    InsertAd.onClickTarget(activity);
////                    if (position == 0) {
////                        textPopupBuilder.curTypeface = Typeface.MONOSPACE;
////                        floatTextView.setTypeface(textPopupBuilder.curTypeface);
////                        floatTextView.updateSize();
////                    } else {
////                        try {
////                            Typeface typeface_get = typefaceList.get(position);
////                            if (typeface_get == null) {
//////                                if (position == 4) {
//////                                    File file = new File(AllData.zitiDir + TypefaceDownloader.typefaceNames.get(position));
//////                                    if (file.exists())
//////                                        file.delete();
//////                                }
////                                // 如果字体不存在，到文件中获取
////                                try {
////                                    typeface_get = Typeface.createFromFile(AllData.zitiDir + TypefaceDownloader.typefaceNames.get(position));
////                                    typefaceList.set(position, typeface_get);
////                                } catch (Exception e) {
////                                    //如果是损坏的文件，删除它
////                                    File file = new File(AllData.zitiDir + TypefaceDownloader.typefaceNames.get(position));
////                                    if (file.exists())
////                                        file.delete();
////
////                                    // 文件中没有，重新下载
////                                    TypefaceDownloader.getInstance().downloadZiti(activity, TypefaceDownloader.typefaceNames.get(position)
////                                            , typefaceList, (TextView) view);
////                                }
////                            }
////                            textPopupBuilder.curTypeface = typeface_get;
////                            floatTextView.setTypeface(textPopupBuilder.curTypeface);
////                            floatTextView.updateSize();
////                        } catch (Exception e) {
////
////                        }
////                    }
////                    lastChooseFontId = position;
////                    typefaceAdapter.notifyDataSetChanged();
////                }
////        );
////        horizontalListView.setDividerWidth(Util.dp2Px(10));
////        return contentView;
//
//        View contentView = LayoutInflater.from(activity).inflate(R.layout.popwindow_text_typeface, null);
//        RecyclerView recyclerView = contentView.findViewById(R.id.rcv_text_type);
//
//        TextTypeAdapter typeAdapter = new TextTypeAdapter(activity, FileDownloader.typefaceChinese);
//        typeAdapter.setOnClickListener((view, position) -> {
//            InsertAd.onClickTarget(activity);
//            if (position == 0) {
//                textPopupBuilder.curTypeface = Typeface.MONOSPACE;
//                floatTextView.setTypeface(textPopupBuilder.curTypeface);
//                floatTextView.updateSize();
//            } else {
//                try {
//                    Typeface typeface_get = typefaceList.get(position);
//                    Logcat.d("字体选择：", "typeface_get =" + typeface_get + " position =" + position);
//                    if (typeface_get == null) {
////                                if (position == 4) {
////                                    File file = new File(AllData.zitiDir + TypefaceDownloader.typefaceNames.get(position));
////                                    if (file.exists())
////                                        file.delete();
////                                }
//                        // 如果字体不存在，到文件中获取
//                        try {
//                            typeface_get = Typeface.createFromFile(AllData.zitiDir + TypefaceDownloader.typefaceNames.get(position));
//                            typefaceList.set(position, typeface_get);
//                        } catch (Exception e) {
//                            //如果是损坏的文件，删除它
//                            File file = new File(AllData.zitiDir + TypefaceDownloader.typefaceNames.get(position));
//                            if (file.exists())
//                                file.delete();
//
//                            // 文件中没有，重新下载
//                            TypefaceDownloader.getInstance().downloadZiti(activity, TypefaceDownloader.typefaceNames.get(position)
//                                    , typefaceList, (TextView) view);
//                        }
//                    }
//                    textPopupBuilder.curTypeface = typeface_get;
//                    floatTextView.setTypeface(textPopupBuilder.curTypeface);
//                    floatTextView.updateSize();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                lastChooseFontId = position;
//                typeAdapter.setTypeList(typefaceList);
//                typeAdapter.notifyDataSetChanged();
//            }
//        });
//        recyclerView.setAdapter(typeAdapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
//        return contentView;
//    }
//}
