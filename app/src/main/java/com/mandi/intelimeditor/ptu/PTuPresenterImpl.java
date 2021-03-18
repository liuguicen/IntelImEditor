package com.mandi.intelimeditor.ptu;

/**
 * 减少P图界面代码耦合，抽离出一个数据处理类
 */
public class PTuPresenterImpl implements PTuContract.Presenter {
    private PTuContract.View mView;

    public PTuPresenterImpl(PTuContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void start() {

    }

    @Override
    public void loadPicData(String path) {

    }

    @Override
    public void loadBitmapData(boolean isInitLoad) {

    }
}
