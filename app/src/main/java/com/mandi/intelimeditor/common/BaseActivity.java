package com.mandi.intelimeditor.common;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.view.StatusBarUtil;
import com.mathandintell.intelimeditor.R;


/**
 * Created by LiuGuicen on 2017/1/4 0004.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public String TAG = this.getClass().getSimpleName();
    public Toolbar mToolbar;
    private boolean mIsDestroyed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.setTransparentForWindow(this);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        initToolbar();
        mIsDestroyed = false;
    }

    /**
     * 抽象方法，获取界面id
     *
     * @return
     */
    public abstract int getLayoutResId();

    /**
     * 初始化toolBar
     */
    public void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @UiThread
    protected void showToast(int id) {
        showToast(getString(id));
    }

    /**
     * 默认显示长的
     */
    @UiThread
    public void showToast(String msg) {
        ToastUtils.show(msg);
    }


    public void showToastShort(String msg) {
        ToastUtils.show(msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mIsDestroyed = true; // 低版本的无法使用系统的isDestroy,手动处理
        super.onDestroy();
    }

    @Override
    public boolean isDestroyed() {
        return mIsDestroyed;// 之类报了低版本的无法使用系统的isDestroy？？？就是有问题，一些用户机器上出错了，不谨慎
    }


    /**
     * 图片长按选择时调用
     * ************************************START
     */
    public void deletePreferPath(String path) {
    }

    public void addPreferPath(String path) {
    }
    /** ************************************END*/
}
