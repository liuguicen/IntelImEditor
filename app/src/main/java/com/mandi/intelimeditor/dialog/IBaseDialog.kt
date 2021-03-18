package com.mathandintell.intelimedit.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity

/**
 * 继承这个类像Fragment一样使用即可
 * 注意继承这个的对话框的根布局需要是FrameLayout不然没有对话框的效果，是全屏，估计是因为Fragement一般使用FramLayout的原因

 * 作者：yonglong
 * 包名：a.baozouptu.widget
 * 时间：2019/4/3 13:20
 * 描述：基础对话框里
 */
abstract class IBaseDialog : androidx.fragment.app.DialogFragment() {
    var rootView: View? = null
    var mContext: AppCompatActivity? = null
    private var isCancelOnOutSide: Boolean = true
    var dismissListener: (() -> Unit)? = null
    var isShowing: Boolean = false

    abstract fun getLayoutResId(): Int

    override fun onStart() {
        val lp = dialog?.window?.attributes
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = WindowManager.LayoutParams.MATCH_PARENT
//        lp?.windowAnimations = R.style.dialogAnim
        dialog?.window?.attributes = lp
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        context?.let { dialog?.let { it1 -> removeDialogBlueLight(it, it1) } }
        if (!isCancelOnOutSide) { // 点击外部不消失，点击返回消失
            dialog?.let {
                it.setCancelable(false)
                it.setCanceledOnTouchOutside(false)
                it.setOnKeyListener { _, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return@setOnKeyListener true
                    }
                    false
                }
            }
        }
        super.onStart()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {// 先调用一下父类方法(因为恒返回空，就不会存在问题)
        rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutResId(), container, false)
        } else if (rootView!!.parent != null) {
            val parentView = rootView!!.parent as ViewGroup
            parentView.removeView(rootView)
        }
        this.dialog!!.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(arg0: DialogInterface?, keyCode: Int, arg2: KeyEvent?): Boolean {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return onBackPressed()
                }
                return false
            }
        })
        return rootView
    }

    /**
     * 去除holo主题下的对话框的蓝线
     * @param context
     * @param dialog
     */
    private fun removeDialogBlueLight(context: Context, dialog: Dialog) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                val dividerID = context.resources.getIdentifier("android:id/titleDivider", null, null)
                val divider = dialog.findViewById<View>(dividerID)
                divider.setBackgroundColor(Color.TRANSPARENT)
            } catch (e: Exception) {
                //上面的代码，是用来去除Holo主题的蓝色线条
                e.printStackTrace()
            }

        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isShowing = false
        dismissListener?.invoke()
    }

    /**
     * 按下返回键，对话框是否消失,默认为true
     */
    fun setCanceledOnTouchOutSize(boolean: Boolean) {
        isCancelOnOutSide = boolean
    }

    /**
     *显示出对话框
     */
    open fun showIt(context: Context?) {
        isShowing = true
        mContext = context as AppCompatActivity?
        val transaction = context?.supportFragmentManager?.beginTransaction()
        transaction?.add(this, tag)?.commitAllowingStateLoss()
    }

    /**
     * 点击返回键，用于用户点击返回键取消也有动作时，不能通过onDismiss监听，因为用户点击确定也会调用onDismiss
     */
    open fun onBackPressed(): Boolean {
        return false
    }
}