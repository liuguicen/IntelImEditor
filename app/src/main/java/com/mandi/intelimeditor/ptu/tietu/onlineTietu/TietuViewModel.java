package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import com.mandi.intelimeditor.common.util.SimpleObserver;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

public class TietuViewModel {

    public void getTietuList() {
        Observable.create((ObservableOnSubscribe<List<PicResource>>)
                emitter -> {
                    PicResourceDownloader.queryPicResByCategory(PicResource.FIRST_CLASS_TEMPLATE, PicResource.SECOND_CLASS_BASE, emitter);
                })
                .subscribe(new SimpleObserver<List<PicResource>>() {
                    @Override
                    public void onError(Throwable throwable) {
                    }

                    @Override
                    public void onNext(List<PicResource> picResList) {
                    }
                });
    }
}