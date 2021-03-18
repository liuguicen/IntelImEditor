package com.mandi.intelimeditor.ptu.gif;

import java.util.List;

class GifDecoderFromGifFrames extends GifDecoder {
    public GifDecoderFromGifFrames(List<GifFrame> frmList) {
        super();
        frameList = frmList;
    }

    @Override
    boolean err() {
        return false;
    }

    @Override
    void stopDecode() {

    }

    @Override
    void releaseUnnecessaryData() {

    }
}
