package util


import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mandi.intelimeditor.common.util.BmobUtil
import com.mathandintell.intelimeditor.R

/**
 * 专辑封面图片加载器
 * Glide加载异常处理
 */
object CoverLoader {
    private val TAG = "CoverLoader"

    /**
     * 显示图片
     *
     * @param mContext 上下文
     * @param url 图片地址
     * @param imageView imageView
     */
    fun loadPicImageView(mContext: Context?, url: String?, imageView: ImageView?) {
        if (mContext == null) return
        if (imageView == null) return
        Glide.with(mContext)
                .load(BmobUtil.getUrlOfSmallerSize(url))
                .placeholder(R.mipmap.icon)
                .error(R.mipmap.icon)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
    }

    /**
     * 显示图片
     *
     * @param mContext 上下文
     * @param url 图片地址
     * @param imageView imageView
     */
    fun loadImageView(mContext: Context?, url: Any?, imageView: ImageView?) {
        if (mContext == null) return
        if (imageView == null) return
        Glide.with(mContext).clear(imageView)
        Glide.with(mContext)
                .load(url)
                .placeholder(R.mipmap.icon)
                .error(R.mipmap.icon)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
    }

    /**
     * 加载原始图片，不裁剪
     */
    fun loadOriginImageView(mContext: Context?, url: Any?, imageView: ImageView?) {
        if (mContext == null) return
        if (imageView == null) return
        Glide.with(mContext).clear(imageView)
        Glide.with(mContext)
                .load(url)
                .placeholder(R.mipmap.icon)
                .error(R.mipmap.icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
    }

    fun loadOriginImageView(mContext: Context?, url: Any?, imageView: ImageView?, placeHolder: Int) {
        if (mContext == null) return
        if (imageView == null) return
        Glide.with(mContext).clear(imageView)
        Glide.with(mContext)
                .load(url)
                .placeholder(placeHolder)
                .error(R.mipmap.icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
    }

    fun loadOriginImageView(mContext: Context?, url: Any?, imageView: ImageView?, placeHolder: Int, errorIcon: Int) {
        if (mContext == null) return
        if (imageView == null) return
        Glide.with(mContext).clear(imageView)
        Glide.with(mContext)
                .load(url)
                .placeholder(placeHolder)
                .error(errorIcon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
    }

    /**
     * 显示图片
     *
     * @param mContext 上下文
     * @param url 图片地址
     * @param defaultUrl 默认图片
     * @param imageView imageView
     */
    fun loadImageView(mContext: Context?, url: Any?, defaultUrl: Int, imageView: ImageView) {
        if (mContext == null) return
        Glide.with(mContext)
                .load(url)
                .error(defaultUrl)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
    }


    /**
     * 返回bitmap
     *
     * @param mContext
     * @param url
     * @param callBack
     */
    fun loadBitmap(mContext: Context?, url: String?, callBack: ((Bitmap) -> Unit)?) {
        if (mContext == null || url == null) return
        Glide.with(mContext)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into<CustomTarget<Bitmap>>(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {

                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        callBack?.invoke(resource)
                    }
                })
    }


}
