package io.github.utilites

import android.graphics.Path
import android.graphics.PointF

class GraphPath(width: Int, height: Int, paddingLeft: Int, paddingBottom: Int) : Path() {

    private val mMt: MatrixTranslator = MatrixTranslator(width, height, paddingLeft, paddingBottom)

    override fun moveTo(x: Float, y: Float) {
        super.moveTo(mMt.calcX(x), mMt.calcY(y))
    }

    fun moveTo(point: PointF) {
        super.moveTo(mMt.calcX(point.x), mMt.calcY(point.y))
    }

    override fun lineTo(x: Float, y: Float) {
        super.lineTo(mMt.calcX(x), mMt.calcY(y))
    }

    fun lineTo(point: PointF) {
        super.lineTo(mMt.calcX(point.x), mMt.calcY(point.y))
    }
}
