package com.mandi.intelimeditor.ptu.dig;

import android.graphics.Matrix;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caiyonglong on 2019/4/5.
 * 历史记录
 */

@SuppressWarnings("ALL")
public class CutoutRecord {
    /**
     * 图片路径
     */
    private String imagePath;

    /**
     * 抠图的路径
     */
    private List<Path> cutoutPathList;
    private List<Matrix> cutoutMatrixList;
    /**
     * 抠图的轨迹
     */
    private List<Float> cutoutTrackList;

    public CutoutRecord() {
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void addCutoutMatrix(Matrix matrix) {
        if (matrix == null) {
            return;
        }
        if (cutoutMatrixList == null) {
            cutoutMatrixList = new ArrayList<>();
        }
        cutoutMatrixList.add(matrix);
    }

    public void addCutoutPath(Path path) {
        if (path == null) {
            return;
        }
        if (cutoutPathList == null) {
            cutoutPathList = new ArrayList<>();
        }
        cutoutPathList.add(path);
    }

    public void removeCutoutMatrix() {
        if (cutoutMatrixList == null && cutoutMatrixList.size() < 1) {
            return;
        }
        cutoutMatrixList.remove(cutoutMatrixList.size() - 1);
    }

    public void removeCutoutPath() {
        if (cutoutPathList == null && cutoutPathList.size() < 1) {
            return;
        }
        cutoutPathList.remove(cutoutPathList.size() - 1);
    }


    public void removeCutoutTrackList() {
        if (cutoutTrackList == null && cutoutTrackList.size() < 1) {
            return;
        }
        cutoutTrackList.remove(cutoutTrackList.size() - 1);
    }

    public List<Matrix> getCutoutMatrixList() {
        if (cutoutMatrixList == null) {
            cutoutMatrixList = new ArrayList<>();
        }
        return cutoutMatrixList;
    }

    public List<Path> getCutoutPathList() {
        if (cutoutPathList == null) {
            cutoutPathList = new ArrayList<>();
        }
        return cutoutPathList;
    }

    public void addCutoutTrack(float length) {
        if (cutoutTrackList == null) {
            cutoutTrackList = new ArrayList<>();
        }
        cutoutTrackList.add(length);
    }


    public List<Float> getCutoutTrackList() {
        if (cutoutTrackList == null) {
            cutoutTrackList = new ArrayList<>();
        }
        return cutoutTrackList;
    }

    public void clearPointList() {
        if (cutoutTrackList == null) {
            return;
        }
        cutoutTrackList.clear();
    }

    public void clearCutout() {
        clearPointList();
        getCutoutTrackList().clear();
        getCutoutMatrixList().clear();
        getCutoutPathList().clear();
    }

    /**
     * 是否有 记录
     */
    public boolean hasRecord() {
        return !getCutoutTrackList().isEmpty();
    }
}