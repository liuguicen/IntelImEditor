package com.mandi.intelimeditor.ptu;

import com.mandi.intelimeditor.BasePresenter;
import com.mandi.intelimeditor.BaseView;

interface PTuContract {

    interface Presenter extends BasePresenter {
        void loadPicData(String path);

        void loadBitmapData(boolean isInitLoad);
    }

    interface View extends BaseView {

    }
}
