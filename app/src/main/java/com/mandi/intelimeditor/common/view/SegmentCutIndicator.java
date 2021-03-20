package com.mandi.intelimeditor.common.view;// package com.mandi.intelimeditor.common.view;
//
// import android.content.Context;
// import android.content.res.TypedArray;
// import android.graphics.Canvas;
// import android.graphics.PointF;
// import android.graphics.Rect;
// import android.graphics.drawable.Drawable;
// import android.os.Build;
// import android.support.v7.widget.RecyclerView;
// import android.util.AttributeSet;
// import android.view.MotionEvent;
// import android.view.View;
//
// import androidx.annotation.Nullable;
// import androidx.annotation.RequiresApi;
//
// import com.mandi.intelimeditor.R;
// import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
// import com.mandi.intelimeditor.common.util.Util;
//
// /**
//  * <pre>
//  *      author : liuguicen
//  *      time : 2019/09/26
//  *      version : 1.0
//  * <pre>
//  *     片段截取指示器，
//  *     短视频或者GIF截取一个片段，用这个指示器指示片段的左右的位置
//  *     必须传入帧列表对应的RecyclerView
//  */
// public class SegmentCutIndicator extends View {
//     private static final int DEFAULT_INDICATOR_WIDTH = Util.dp2Px(10);
//     private int mIdicatorWidth = DEFAULT_INDICATOR_WIDTH;
//     private int mNumbers = 0;
//     private int lPos = 0;
//     private int rPos = 0;
//     private Rect lRect, rRect;
//     private Drawable mIndicatorDrawable;
//     private RecyclerView mRecyclerView;
//     private float lastX;
//     private float lastY;
//
//     public SegmentCutIndicator(Context context) {
//         super(context);
//     }
//
//     public SegmentCutIndicator(Context context, @Nullable AttributeSet attrs) {
//         super(context, attrs);
//     }
//
//     public SegmentCutIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//         super(context, attrs, defStyleAttr);
//     }
//
//     @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//     public SegmentCutIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//         super(context, attrs, defStyleAttr, defStyleRes);
//         if (attrs != null) {
//             // ...
//             TypedArray a = context.getTheme().obtainStyledAttributes(attrs
//                     , R.styleable.SegmentCutIndicator
//                     , defStyleAttr, 0);
//             mIndicatorDrawable = a
//                     .getDrawable(R.styleable.SegmentCutIndicator_indicator);
//             if (mIndicatorDrawable.getIntrinsicWidth() > 0) {
//                 mIdicatorWidth = mIndicatorDrawable.getIntrinsicWidth();
//             }
//         }
//     }
//
//     void setRecyclerView(RecyclerView recyclerView) {
//         this.mRecyclerView = recyclerView;
//     }
//
//     @Override
//     protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//         super.onLayout(changed, left, top, right, bottom);
//         if (changed && lRect == null) { // 内部只自动初始化一次
//             RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(lPos);
//             if (viewHolder != null) {
//                 int[] outLocation = new int[2];
//                 viewHolder.itemView.getLocationOnScreen(outLocation);
//                 lRect = new Rect(outLocation[0], 0,
//                         outLocation[0] + mIdicatorWidth, getHeight());
//             }
//
//             RecyclerView.ViewHolder rHolder = mRecyclerView.findViewHolderForAdapterPosition(lPos);
//             if (rHolder != null) {
//                 int[] outLocation = new int[2];
//                 rHolder.itemView.getLocationOnScreen(outLocation);
//                 int inRight = rHolder.itemView.getRight() - mIdicatorWidth;
//                 rRect = new Rect(inRight - mIdicatorWidth, 0,
//                         inRight, getHeight());
//             }
//
//
//         }
//     }
//
//     @Override
//     protected void onDraw(Canvas canvas) {
//         int width = canvas.getWidth();
//         int ceilLen = width / mNumbers;
//         // 画左边
//         mIndicatorDrawable.setBounds(lRect);
//         mIndicatorDrawable.draw(canvas);
//         mIndicatorDrawable.setBounds(rRect);
//         mIndicatorDrawable.draw(canvas);
//         super.onDraw(canvas);
//     }
//
//     @Override
//     public boolean onTouchEvent(MotionEvent event) {
//         float x = event.getX(), y = event.getY();
//         float x0 = event.getX(0), y0 = event.getY(0);
//         float x1 = 0, y1 = 0;
//         float touchCenterX = 0, touchCenterY = 0;
//         PointF p1 = new PointF(x0, y0), p2 = null;
//         if (event.getPointerCount() > 1) {
//             x1 = event.getX(1);
//             y1 = event.getY(1);
//             touchCenterX = (x0 + x1) / 2;
//             touchCenterY = (y0 + y1) / 2;
//             p2 = new PointF(x1, y1);
//         }
//         switch (event.getActionMasked()) {
//             case MotionEvent.ACTION_DOWN:
//                 // Logcat.e("经过了ACTION_DOWN");
//                 lastX = x;
//                 lastY = y;
//                 Util.DoubleClick.isDoubleClick();
//                 break;
//             case MotionEvent.ACTION_MOVE:
//                 //这里doubleclick用来判断点击，距离很近不取消，远就取消它
//                 // Logcat.e("ACTION_MOVE");
//                 if (Math.abs(x - lastX) > 5) {
//                     Util.DoubleClick.cancel();
//                     moveIndicator(x - lastX);
//                     lastX = x;
//                     lastY = y;
//                 }
//                 break;
//             case MotionEvent.ACTION_UP:
//                 // Logcat.e("ACTION_UP");
//                 view.twoFingerDisChange(null, null, true);
//
//                 if (Util.DoubleClick.isDoubleClick())//判断发生点击事件
//                 {
//                     view.onClick(event.getX(), event.getY());
//                 }
//                 break;
//             //如果点击到了tietu上面
//             default:
//                 break;
//         }
//         return true;
//     }
//
//     private void moveIndicator(float d) {
//
//     }
//
//     /**
//      * @param numbers 分成的份数，即视频或者GIF的帧数
//      */
//     public void setNumbers(int numbers) {
//         if (numbers > 0) {
//             mNumbers = numbers;
//         }
//     }
//
//     public void setLeftPos(int lPos) {
//         this.lPos = lPos;
//     }
//
//     public void setRightPos(int rPos) {
//         this.rPos = rPos;
//     }
//
//     public int getTotalLen() {
//         return mNumbers;
//     }
//
//     public int getLeftPos() {
//         return lPos;
//     }
//
//     public int getRightPos() {
//         return rPos;
//     }
// }
