package view


import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.mandi.intelimeditor.R

class MenuItemView : FrameLayout {
    private val position: Int = 0

    private var listener: OnItemClickListener? = null

    constructor(context: Context) : super(context, null) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.MenuItemView)
        val title = ta.getString(R.styleable.MenuItemView_miv_title)
        val drawable = ta.getDrawable(R.styleable.MenuItemView_miv_icon)
        val mDrawableTintList = ta.getColorStateList(R.styleable.MenuItemView_miv_color)
        ta.recycle()

        val mView = LayoutInflater.from(context).inflate(R.layout.item_menu, this, true)

        mView.findViewById<ImageView>(R.id.iv_icon).setImageDrawable(drawable)
        mView.findViewById<TextView>(R.id.tv_title).text = title

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mView.findViewById<ImageView>(R.id.iv_icon).imageTintList = mDrawableTintList
            mView.findViewById<ImageView>(R.id.iv_icon).imageTintMode = PorterDuff.Mode.SRC_ATOP
        }
        mView.setOnClickListener { view ->
            listener?.click(view, position)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun click(view: View, position: Int)
    }
}
