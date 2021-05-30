/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mandi.intelimeditor.ptu.transfer

import android.content.Context
import android.graphics.*
import com.mandi.intelimeditor.common.util.BitmapUtil
import com.mandi.intelimeditor.common.util.LogUtil
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import kotlin.math.roundToInt

/**
 * Collection of image reading and manipulation utilities in the form of static functions.
 * TODO: this class should be moved to the common code in the future
 */
abstract class ImageUtils {
    companion object {
        // 放到左上角即可，不放到中间，放到中间会多出一条伪造边
        fun drawInBm(
            src: Bitmap,
            srcRange: Rect?,
            canvas: Canvas
        ): Rect {
            canvas.drawColor(Color.BLACK)
            var dstWidth = canvas.width;
            var dstHeight = canvas.height;

            var srcW = srcRange?.width() ?: src.width
            var srcH = srcRange?.height() ?: src.height
            if (srcW < srcH) { // 高度较长，以高度为准缩放
                dstWidth = (srcW * (1f * dstHeight / srcH)).roundToInt()
            } else {
                dstHeight = (srcH * (1f * dstWidth / srcW)).roundToInt()
            }

            var dstRect = Rect(0, 0, dstWidth, dstHeight)
            canvas.drawBitmap(
                src,
                srcRange,
                dstRect,
                BitmapUtil.getBitmapPaint()
            )
            return dstRect
        }

        /**
         * 先将bitmap放入pixel，再放入bugger
         */
        fun bitmapToByteBuffer(
            bitmapIn: Bitmap,
            mean: Float = 0.0f,
            std: Float = 255.0f,
            pixArray: IntArray,
            inputBuffer: ByteBuffer
        ) {
            inputBuffer.rewind()

            var width = bitmapIn.width
            var height = bitmapIn.height
            bitmapIn.getPixels(pixArray, 0, width, 0, 0, width, height)
            var pixel = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val value = pixArray[pixel++]
//                    if (pixel % 50000 == 0)
//                        LogUtil.d(pixel)
                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.
                    inputBuffer.putFloat(((value shr 16 and 0xFF) - mean) / std)
                    inputBuffer.putFloat(((value shr 8 and 0xFF) - mean) / std)
                    inputBuffer.putFloat(((value and 0xFF) - mean) / std)
                }
            }

            inputBuffer.rewind()
        }

        fun createEmptyBitmap(imageWidth: Int, imageHeigth: Int, color: Int = 0): Bitmap {
            val ret = Bitmap.createBitmap(imageWidth, imageHeigth, Bitmap.Config.RGB_565)
            if (color != 0) {
                ret.eraseColor(color)
            }
            return ret
        }

        fun loadBitmapFromResources(context: Context, path: String): Bitmap {
            val inputStream = context.assets.open(path)
            return BitmapFactory.decodeStream(inputStream)
        }

        /**
         *  从输出的数组中取出结果bm， 尺寸并没有缩放回去
         */
        fun convertArrayToBitmap(
            imageArray: Array<Array<Array<FloatArray>>>,
            bmRange: Rect
        ): Bitmap {
            val styledImage = BitmapUtil.BitmapPixelsConverter(bmRange.width(), bmRange.height())

            for (x in imageArray[0].indices) { // x 表示高
                for (y in imageArray[0][0].indices) { // y 表示宽
                    if (y < bmRange.left || y >= bmRange.right || x <= bmRange.top || x >= bmRange.bottom)
                    // 不在bm范围内的点
                        continue;

                    val color = Color.rgb(
                        ((imageArray[0][x][y][0] * 255).toInt()),
                        ((imageArray[0][x][y][1] * 255).toInt()),
                        (imageArray[0][x][y][2] * 255).toInt()
                    )

                    // this y, x is in the correct order!!!
                    styledImage.setPixel(y - bmRange.left, x - bmRange.top, color)
                }
            }
            return styledImage.bimap
        }

        /**
         *  从输出的数组中取出结果bm， 尺寸并没有缩放回去, 重用原始的内容bm数据
         */
        fun convertArrayToBitmap(
            imageArray: Array<Array<Array<FloatArray>>>,
            srcBm: Bitmap,
            pixArray: IntArray
        ): Bitmap {
            for (x in imageArray[0].indices) { // x 表示高
                for (y in imageArray[0][0].indices) { // y 表示宽
//                    val color = Color.rgb(
//                        ((imageArray[0][x][y][0] * 255).toInt()),
//                        ((imageArray[0][x][y][1] * 255).toInt()),
//                        (imageArray[0][x][y][2] * 255).toInt()
//                    )
                    // this y, x is in the correct order!!!
                    if ((y + x * srcBm.width) % 10000 == 0) {
                        LogUtil.d("convertArrayToBitmap: " + (y + x * srcBm.width))
                    }
                    pixArray[y + x * srcBm.width] = -0x1000000 or
                            ((imageArray[0][x][y][0] * 255).toInt() shl 16) or
                            (((imageArray[0][x][y][1] * 255).toInt()) shl 8) or
                            (imageArray[0][x][y][2] * 255).toInt()
                }
            }
            srcBm.setPixels(pixArray, 0, srcBm.width, 0, 0, srcBm.width, srcBm.height);
            return srcBm
        }
    }
}
