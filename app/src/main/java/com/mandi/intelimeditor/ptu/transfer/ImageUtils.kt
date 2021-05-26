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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt

/**
 * Collection of image reading and manipulation utilities in the form of static functions.
 * TODO: this class should be moved to the common code in the future
 */
abstract class ImageUtils {
    companion object {
        fun drawInBm(
            src: Bitmap,
            canvas: Canvas
        ): Rect {
            canvas.drawColor(Color.BLACK)
            var dstWidth = canvas.width;
            var dstHeight = canvas.height;
            if (src.width < src.height) { // 高度较长，以高度为准缩放
                dstWidth = (src.width * (1f * dstHeight / src.height)).roundToInt()
            } else {
                dstHeight = (src.height * (1f * dstWidth / src.width)).roundToInt()
            }
            var startW = (canvas.width - dstWidth) / 2
            var startH = (canvas.height - dstHeight) / 2

            var dstRect = Rect(startW, startH, startW + dstWidth, startH + dstHeight)
            canvas.drawBitmap(
                src,
                null,
                dstRect,
                BitmapUtil.getBitmapPaint()
            )
            return dstRect
        }

        fun bitmapToByteBuffer(
            bitmapIn: Bitmap,
            mean: Float = 0.0f,
            std: Float = 255.0f
        ): ByteBuffer {

            var width = bitmapIn.width
            var height = bitmapIn.height
            val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
            inputImage.order(ByteOrder.nativeOrder())
            inputImage.rewind()

            val intValues = IntArray(width * height)
            bitmapIn.getPixels(intValues, 0, width, 0, 0, width, height)
            var pixel = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val value = intValues[pixel++]

                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.
                    inputImage.putFloat(((value shr 16 and 0xFF) - mean) / std)
                    inputImage.putFloat(((value shr 8 and 0xFF) - mean) / std)
                    inputImage.putFloat(((value and 0xFF) - mean) / std)
                }
            }

            inputImage.rewind()
            return inputImage
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

        fun convertArrayToBitmap(
            imageArray: Array<Array<Array<FloatArray>>>,
            bmRange: Rect
        ): Bitmap {
            val conf = Bitmap.Config.ARGB_8888 // see other conf types
            val styledImage = Bitmap.createBitmap(bmRange.width(), bmRange.height(), conf)

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
            return styledImage
        }
    }
}
