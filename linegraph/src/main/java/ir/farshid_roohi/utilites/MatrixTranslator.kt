package ir.farshid_roohi.utilites

import android.graphics.Bitmap

class MatrixTranslator(
    private val mWidth: Int,
    private val mHeight: Int,
    private val mPaddingLeft: Int,
    private val mPaddingBottom: Int
) {

    fun calcX(x: Float): Float {
        return x + mPaddingLeft
    }

    fun calcY(y: Float): Float {
        return mHeight - (y + mPaddingBottom)
    }

    fun calcBitmapCenterX(bitmap: Bitmap, x: Float): Float {
        return x + mPaddingLeft - bitmap.width / 2
    }

    fun calcBitmapCenterY(bitmap: Bitmap, y: Float): Float {
        return mHeight.toFloat() - (y + mPaddingBottom) - (bitmap.height / 2).toFloat()
    }
}
