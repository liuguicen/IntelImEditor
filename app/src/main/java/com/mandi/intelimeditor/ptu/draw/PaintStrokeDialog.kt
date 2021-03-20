package draw

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.mandi.intelimeditor.common.util.LogUtil
import com.mandi.intelimeditor.common.util.Util
import com.mandi.intelimeditor.ptu.draw.BaseDrawView
import com.mandi.intelimeditor.ptu.draw.DrawView
import com.mandi.intelimeditor.dialog.IBaseDialog
import com.mandi.intelimeditor.R
import kotlinx.android.synthetic.main.popup_sketch_stroke.*
import kotlin.math.roundToInt

/**
 * 作者：yonglong
 * 包名：com.mandi.intelimeditor.widget
 * 时间：2019/4/3 13:21
 * 描述：
 */
class PaintStrokeDialog : IBaseDialog() {

    private var size = 0
    var drawToolChangeListener: DrawToolChangeListener? = null
    var paintStrokeSize = 0
    var isInErase = false
    //透明度 0-255
    var paintAlpha = 0
    var paintStrokeStyle = 0

    override fun getLayoutResId(): Int {
        return R.layout.popup_sketch_stroke
    }

    companion object {
        private val TAG = "PaintStrokeDialog"

        fun newInstance(): PaintStrokeDialog {
            val args = Bundle()
            val fragment = PaintStrokeDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        initListener()
        LogUtil.d(TAG, " paintStrokeSize = $paintStrokeSize $paintAlpha $paintStrokeStyle")
        strokeSizeSeekBar.progress = paintStrokeSize
        strokeAlphaSeekBar.progress = paintAlpha
        updatePaintStyle(paintStrokeStyle, true)
    }

    private fun initData() {
        //画笔宽度缩放基准参数
        val circleDrawable = Util.getDrawable(R.drawable.circle)!!
        size = circleDrawable.intrinsicWidth
        strokeSizeSeekBar.max = DrawView.DEFAULT_MAX_PAINT_STROKE_SIZE
        strokeAlphaSeekBar.max = 255
        setSeekBarProgress(paintStrokeSize)
    }

    private fun initListener() {
        containerView.setOnClickListener {
            LogUtil.d(TAG, "save paintStrokeSize = $paintStrokeSize $paintAlpha $paintStrokeStyle")
            drawToolChangeListener?.onSizeChanged(paintStrokeStyle, paintStrokeSize, paintAlpha)
            dismiss()
        }
        cardView.setOnClickListener {}
        strokeAlphaSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        //画笔宽度拖动条
        strokeSizeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setSeekBarProgress(progress)
            }
        })
        saveStrokeTv.setOnClickListener {
            LogUtil.d(TAG, "save paintStrokeSize = $paintStrokeSize $paintAlpha $paintStrokeStyle")
            drawToolChangeListener?.onSizeChanged(paintStrokeStyle, paintStrokeSize, paintAlpha)
            dismiss()
        }
        cancelTv.setOnClickListener {
            dismiss()
        }
        strokeStyleRg.setOnCheckedChangeListener { group, checkedId ->
            val index = when (checkedId) {
                R.id.radioStyle -> BaseDrawView.DRAW_PAINT_STYLE_DEFAULT
                R.id.radioStyle1 -> BaseDrawView.DRAW_PAINT_STYLE_2
                R.id.radioStyle2 -> BaseDrawView.PAINT_STYLE_MOSAIC
                R.id.radioStyle3 -> BaseDrawView.DRAW_PAINT_STYLE_3
                R.id.radioStyle4 -> BaseDrawView.DRAW_PAINT_STYLE_4
                R.id.radioStyle5 -> BaseDrawView.DRAW_PAINT_STYLE_6
                R.id.radioStyle_clear_draw-> BaseDrawView.PAINT_STYLE_CLEAR_DRAW
                else -> BaseDrawView.DRAW_PAINT_STYLE_DEFAULT
            }
            updatePaintStyle(index, false)
            LogUtil.d(TAG, "save paintStrokeSize = $paintStrokeSize $paintAlpha $paintStrokeStyle")
        }
    }

    interface DrawToolChangeListener {
        fun onSizeChanged(style: Int, size: Int, alpha: Int)
    }

    /**
     * @param progress 不是像素单位，怎么算的？
     */
    fun setSeekBarProgress(progress: Int) {
        paintStrokeSize = if (progress > 1) progress else 1
        strokeSizeTv.text = "尺寸 :$paintStrokeSize px"
        strokeImageView.setPaintSize(paintStrokeSize)
    }

    fun updateAlpha(progress: Int) {
        paintAlpha = progress
        strokeAlphaTv.text = "不透明度 :${(paintAlpha / 255f * 100).roundToInt()}%"
        strokeImageView.setPaintAlpha(paintAlpha)
    }

    fun updatePaintStyle(index: Int, init: Boolean) {
        paintStrokeStyle = index;
        strokeImageView.selectPaintStyle(index)
        if (init) {
            val checkedId = when (index) {
                BaseDrawView.DRAW_PAINT_STYLE_DEFAULT -> R.id.radioStyle
                BaseDrawView.DRAW_PAINT_STYLE_2 -> R.id.radioStyle1
                BaseDrawView.PAINT_STYLE_MOSAIC -> R.id.radioStyle2
                BaseDrawView.DRAW_PAINT_STYLE_3 -> R.id.radioStyle3
                BaseDrawView.DRAW_PAINT_STYLE_4 -> R.id.radioStyle4
                BaseDrawView.DRAW_PAINT_STYLE_6 -> R.id.radioStyle5
                BaseDrawView.PAINT_STYLE_CLEAR_DRAW->R.id.radioStyle_clear_draw
                else -> R.id.radioStyle
            }
            strokeStyleRg.check(checkedId)
        }
    }
}
