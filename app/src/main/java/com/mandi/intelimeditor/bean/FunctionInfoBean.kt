package com.mathandintell.intelimedit.bean

import com.mandi.intelimeditor.ptu.PtuUtil
import com.mathandintell.intelimeditor.R


/**
 * 底部功能 数据类
 */
class FunctionInfoBean(var titleResId: Int,
                       var iconResId: Int,
                       var bg: Int = R.drawable.function_background_text_yellow,
                       val type: Int = PtuUtil.EDIT_MAIN) {
    //无背景底部功能图标
    constructor(titleResId: Int,
                iconResId: Int,
                type: Int = PtuUtil.EDIT_MAIN) : this(titleResId, iconResId, -1, type) {
    }

    //是否可见
    var isVisible: Boolean = true

    //功能是否开放
    var isLocked: Boolean = false

    //可选中
    var isCanSelected: Boolean = false
}
