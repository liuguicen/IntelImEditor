package com.mandi.intelimeditor.bean;


/**
 * gif play某一帧时通过eventBus传递事件
 */
public class GifPlayFrameEvent {
    public int id;

    public GifPlayFrameEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
