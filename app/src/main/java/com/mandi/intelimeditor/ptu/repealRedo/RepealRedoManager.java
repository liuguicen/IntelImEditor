package com.mandi.intelimeditor.ptu.repealRedo;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.util.FileTool;

import java.io.File;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Administrator on 2016/7/28.
 */
public class RepealRedoManager<T> {
    private static final String TAG = "RepealRedoManager";
    //撤销重做的最大步数
    private int maxStep = 5;

    private LinkedList<T> stepList;

    /**
     * 注意要理解一下这个Iter，应该说iter是在它的next元素和previous元素位置之间，不是在元素位置上
     * next可以走到最后一个元素后面的位置，所以此时调用nextIndex = size，
     * previous可以走到0元素的左边位置，所以此时调用previousIndex返回-1
     * 没有元素的时候，它在0的左边
     */
    private ListIterator<T> iter;
    private Bitmap baseBitmap;


    /**
     * 需要撤销重做类来判断是否对图片发生了更改，现在用列表存储操作步骤数据，用迭代器移动进行撤销重做
     * 判断修改，简单的情况是直接判断当前迭代器所处的位置，然后这个位置是不是在-1上 (数组下标关系，就是0的意思）
     * <p>
     * 但是有更多的情况要处理，更一般的想，迭代器位置代表操作情况，判断是否更改就是判断迭代器当前位置是否 = 操作起点，
     * 前面的操作的起点-1，那么这个起点可以不一定是-1 其它也行
     * <p>
     * 现在的特殊情况，可以处理了<p>
     * 1、超过了撤销重做支持的最大步骤，丢弃前面的撤销重做，那么操作起点--
     * 2、用户保存图片回来，希望继续编辑，但是保存过的那个位置就不需要判断是否已经更改，因为已经保存了
     * 3、发生了无法提交的操作，但是又需要判断为已更改的情况，
     *
     * <p>这个地方也是好长时间，几次更改方案才到这个程度，技术不够哦
     * 这个方法
     */
    private int operateStart = -1;
    public String lastSavePath;

    public RepealRedoManager(int maxStep) {
        this.maxStep = maxStep;
        stepList = new LinkedList<>();
        iter = stepList.listIterator();
    }


    /**
     * 提交操作，返回是否需要超出最大步数，
     * <p>超出则删除最列表开始的stepData，
     * <p>然后需要将BaseBitmap前进一步，
     */
    public T commit(T sd) {
        if (sd == null) return null;
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        iter.add(sd);
        if (stepList.size() > maxStep) {
            while (iter.hasPrevious()) {
                iter.previous();
            }
            T resd = iter.next();
            iter.remove();
            operateStart -= 1;
            while (iter.hasNext()) {
                iter.next();
            }
            return resd;
        }
        return null;
    }

    public int getCurrentIndex() {
        return iter.previousIndex();
    }

    public T getCurrentStepDate() {
        if (iter.previousIndex() < 0) return null;
        return stepList.get(iter.previousIndex());
    }

    public boolean canRedo() {
        return iter.hasNext();
    }

    /**
     * 返回redo数据结果，并且当前指针前进一步
     */
    @Nullable
    public T redo() {
        if (iter.hasNext()) {
            return iter.next();
        }
        return null;
    }

    public boolean canRepeal() {
        return iter.hasPrevious();
    }


    public T getStepdata(int i) {
        return stepList.get(i);
    }

    /**
     * 当前指针向前一步,注意撤销时将操作步骤做到当前位置的前一个位置
     */
    public void repealPrepare() {
        if (!iter.hasPrevious()) return;
        iter.previous();
        //        if (getCurrentIndex() == 0) {
        //            hasChangePic = false; // 展时不处理，currentId == 0，可能因为提交操作最大步骤整体前进了一步
        //        }
    }

    public void clear(Context context) {
        String path = FileTool.createTempPicPath(context);
        if (path == null) {
            return;
        }

        String parentPath = path.substring(0,
                path.lastIndexOf('/'));
        FileTool.deleteDir(new File(parentPath));
        stepList.clear();
        iter = stepList.listIterator();
    }

    public void init() {
        iter = stepList.listIterator();
    }

    public @Nullable
    Bitmap getBaseBitmap() {
        return baseBitmap;
    }

    public void setBaseBm(Bitmap baseBitmap) {
        this.baseBitmap = baseBitmap;
    }

    /**
     * 注意，可以通过外部强行设置是否改变
     *
     * @see RepealRedoManager#iter
     */
    public boolean hasChange() {
        return iter.previousIndex() > operateStart;
    }


    public int getSize() {
        return stepList.size();
    }

    public void setCurrentAsOperateStart() {
        operateStart = iter.previousIndex();
    }

    /**
     * @see RepealRedoManager#operateStart
     */
    public void setOperateStart(int start) {
        operateStart = operateStart;
    }
}
