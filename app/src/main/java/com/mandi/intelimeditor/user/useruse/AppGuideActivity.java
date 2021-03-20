package com.mandi.intelimeditor.user.useruse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.common.LaunchActivity;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.user.userSetting.SettingActivity;
import com.mandi.intelimeditor.R;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/11
 *      version : 1.0
 * <pre>
 */
public class AppGuideActivity extends BaseActivity implements View.OnClickListener {
    public static final int GUIDE_ITEM_COUNT = 6;
    private ViewPager vp;
    private TextView tv;
    private View[] dotViewArray;
    private Button btnEnterApp;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_app_guide;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbar.setVisibility(View.GONE);
        vp = findViewById(R.id.guide_vp);
        btnEnterApp = findViewById(R.id.guide_enter_app);
        btnEnterApp.setOnClickListener(this);
        Intent sourceIntent = getIntent();
        if (SettingActivity.ACTION_LOOK_GUIDE.equals(sourceIntent.getAction())) {
            mToolbar.setVisibility(View.VISIBLE);
            btnEnterApp.setVisibility(View.INVISIBLE);
        }
        initViewPager();
    }

    private void initViewPager() {
        vp.setAdapter(new GuideViewPagerAdapter(this, initImageViewArray()));
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int id = 0; id < dotViewArray.length; id++) {
                    if (id == position)
                        dotViewArray[id].setBackground(Util.getDrawable(R.drawable.background_circle_checked));
                    else
                        dotViewArray[id].setBackground(Util.getDrawable(R.drawable.background_circle_unchecked));
                }
                if (position == dotViewArray.length - 1) {
                    btnEnterApp.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private ImageView[] initImageViewArray() {
        int[] ImageIdArrays = new int[]{
                R.mipmap.guide_gif
                , R.mipmap.guide_tietu
                , R.mipmap.guide_dig
                , R.mipmap.guide_pic_resource
                , R.mipmap.guide_rend
                , R.mipmap.guide_text};
        ImageView[] imageViewArray = new ImageView[ImageIdArrays.length];
        for (int i = 0; i < imageViewArray.length; i++) {
            ImageView imageView = new ImageView(this);
            Glide.with(this).load(ImageIdArrays[i]).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageViewArray[i] = imageView;
        }
        dotViewArray = new View[ImageIdArrays.length];
        dotViewArray[0] = findViewById(R.id.guide_dot_first);
        dotViewArray[1] = findViewById(R.id.guide_dot_second);
        dotViewArray[2] = findViewById(R.id.guide_dot_third);
        dotViewArray[3] = findViewById(R.id.guide_dot_fourth);
        dotViewArray[4] = findViewById(R.id.guide_dot_fifth);
        dotViewArray[5] = findViewById(R.id.guide_dot_sixth);
        dotViewArray[0].setBackground(Util.getDrawable(R.drawable.background_circle_checked));
        return imageViewArray;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.guide_enter_app:
                setResult(LaunchActivity.RESULT_CODE_APP_GUIDE_ENTER);
                // 这个时候才写入，表示用户同意了协议，否则不能让用户进入APP
                AllData.hasReadConfig.write_appGuide(true);
                finish();
                break;
        }
    }

}
