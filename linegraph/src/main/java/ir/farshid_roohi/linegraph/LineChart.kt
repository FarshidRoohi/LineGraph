package ir.farshid_roohi.linegraph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import ir.farshid_roohi.utilites.GraphCanvasWrapper
import ir.farshid_roohi.utilites.GraphPath
import java.util.*

/**
 * Created by Farshid Roohi.
 * Graph | Copyrights 2019-08-21.
 */
@SuppressLint("ViewConstructor")
class LineChart(context: Context?, private val chartEntities: List<ChartEntity>) :
    SurfaceView(context), SurfaceHolder.Callback {

    private var mPaddingTop: Int = 80
    var mPaddingRight: Int = 40
    var mPaddingLeft: Int = 40
    var mPaddingBottom: Int = 90
    var maxValue: Long = 0
    var marginTop: Int = 50
    var increment: Long = 20
    var animationDuration: Long = 200
    var legendArray: Array<String>? = null

    private val lineColor = Color.parseColor("#32FFFFFF")
    private val darkBlueColor = Color.parseColor("#FF2B4A83")

    private var drawThread: DrawThread? = null

    init {
        this.holder.addCallback(this)
        val maxes = ArrayList<Float>()
        for (lineGraph in chartEntities) {
            val copies =
                lineGraph.values.copyOf(lineGraph.values.size)
            Arrays.sort(copies)
            maxes.add(copies[copies.size - 1])
        }
        this.maxValue = (Collections.max(maxes) as Float).toLong()
        this.increment = maxValue / 5
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        this.drawThread?.apply {
            this.isRun = false
            this@LineChart.drawThread = null
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (this.drawThread == null) {
            this.drawThread = DrawThread()
            this.drawThread!!.start()
        }
    }

    internal inner class DrawThread : Thread() {
        var isRun = true

        private var anim = 0.0f
        private val height = this@LineChart.height
        private val width = this@LineChart.width

        private val xLength = width - (mPaddingLeft + mPaddingRight)
        private val yLength = height - (mPaddingBottom + mPaddingTop + marginTop)

        private val chartXLength = width - (mPaddingLeft + mPaddingRight)

        private var p = Paint()
        private var pCircle = Paint()
        private var pCircleBG = Paint()
        private var pLine = Paint()
        private var pBaseLine = Paint()
        private var pBaseLineX = Paint()
        private var pMarkText = Paint()

        private var animStartTime: Long = -1
        override fun run() {
            var canvas: Canvas?
            var graphCanvasWrapper: GraphCanvasWrapper
            this.initializePaint()
            this.animStartTime = System.currentTimeMillis()

            handler.postDelayed({
                this.isRun = false
            }, animationDuration)

            while (isRun) {
                canvas = holder.lockCanvas()
                graphCanvasWrapper = GraphCanvasWrapper(
                    canvas,
                    width,
                    height,
                    mPaddingLeft,
                    mPaddingBottom
                )
                calcTimePass()

                synchronized(holder) {

                    canvas.drawColor(darkBlueColor)

                    graphCanvasWrapper.drawLine(
                        0.0f,
                        0.0f,
                        chartXLength.toFloat(),
                        0.0f,
                        pBaseLine
                    )

                    var newX: Float
                    val gap = chartXLength / (chartEntities[0].values.size - 1)
                    for (i in 0 until chartEntities[0].values.size) {
                        newX = (gap * i).toFloat()
                        graphCanvasWrapper.drawLine(
                            newX,
                            0.0f,
                            newX,
                            maxValue.toFloat(),
                            pBaseLine
                        )

                    }
                    this.drawGraph(graphCanvasWrapper)
                    this.drawXText(graphCanvasWrapper)
                    this@LineChart.holder.unlockCanvasAndPost(graphCanvasWrapper.canvas)
                }
            }

        }

        private fun drawXText(graphCanvas: GraphCanvasWrapper) {

            if (legendArray == null || legendArray.isNullOrEmpty()) {
                return
            }
            var x: Float
            var y: Float
            val xGap = (xLength / (chartEntities[0].values.size - 1)).toFloat()

            for (i in 0 until chartEntities[0].values.size) {
                val rect = Rect()
                val text = legendArray!![i]
                pMarkText.measureText(text)
                pMarkText.textSize = 20f

                x = xGap * i
                y = (-(20 + rect.height())).toFloat()

                pMarkText.getTextBounds(text, 0, text.length, rect)
                val degree: Int = -45
                val px = rect.exactCenterX() + x + 10
                val py = y + rect.exactCenterY() - 10

                graphCanvas.drawText(
                    text,
                    x - rect.width() / 2,
                    y,
                    pMarkText,
                    degree.toFloat(),
                    px,
                    py
                )
            }
        }

        private fun drawGraph(graphCanvasWrapper: GraphCanvasWrapper) {
            var prevX = 0f
            var prevY = 0f

            var nextX: Float
            var nextY: Float

            var value: Float
            var mode: Float

            this.pCircleBG.color = darkBlueColor

            for (i in 0 until chartEntities.size) {
                val linePath = GraphPath(width, height, mPaddingLeft, mPaddingBottom)
                var first = false

                var x: Float
                var y: Float

                this.p.color = chartEntities[i].color
                this.pCircle.color = chartEntities[i].color

                val gap = xLength / (chartEntities[i].values.size - 1)
                value = anim / 1.0f
                mode = anim % 1.0f

                var j = 0
                while (j < (value + 1)) {
                    if (j < chartEntities[i].values.size) {
                        x = (gap * j).toFloat()
                        y = yLength * chartEntities[i].values[j] / maxValue
                        if (!first) {

                            linePath.moveTo(x + 2, y + 2)
                            first = true
                        } else {
                            if (j > value && mode != 0f) {
                                nextX = x - prevX
                                nextY = y - prevY

                                linePath.lineTo(prevX + nextX * mode, prevY + nextY * mode)
                            } else {
                                linePath.lineTo(x, y)
                            }
                        }
                        prevX = x
                        prevY = y

                    }
                    j++
                }

                graphCanvasWrapper.canvas.drawPath(linePath, p)

                var counter = 0
                while (counter < value + 1) {
                    if (counter < chartEntities[i].values.size) {
                        x = (gap * counter).toFloat()
                        y = yLength * chartEntities[i].values[counter] / maxValue
                        graphCanvasWrapper.drawCircle(x, y, 8.0f, pCircleBG)
                        graphCanvasWrapper.drawCircle(x, y, 4.0f, pCircle)
                    }
                    counter++
                }
            }


        }

        private fun calcTimePass() {
            val curTime = System.currentTimeMillis()
            var gapTime = curTime - animStartTime
            if (gapTime >= animationDuration) {
                gapTime = animationDuration
            }
            this.anim = chartEntities[0].values.size * gapTime.toFloat() / animationDuration
        }


        private fun initializePaint() {
            p = Paint()
            p.flags = Paint.ANTI_ALIAS_FLAG
            p.isAntiAlias = true
            p.isFilterBitmap = true
            p.color = Color.BLUE
            p.strokeWidth = 10f
            p.isAntiAlias = true
            p.strokeCap = Paint.Cap.ROUND
            p.style = Paint.Style.STROKE

            pCircle = Paint()
            pCircle.flags = Paint.ANTI_ALIAS_FLAG
            pCircle.isAntiAlias = true
            pCircle.isFilterBitmap = true
            pCircle.color = Color.BLUE
            pCircle.strokeWidth = 20f
            pCircle.style = Paint.Style.STROKE

            pCircleBG = Paint()
            pCircleBG.isAntiAlias = true
            pCircleBG.isFilterBitmap = true
            pCircleBG.color = darkBlueColor
            pCircleBG.strokeWidth = 10f
            pCircleBG.style = Paint.Style.FILL_AND_STROKE

            pLine = Paint()
            pLine.flags = Paint.ANTI_ALIAS_FLAG
            pLine.isAntiAlias = true //text anti alias
            pLine.isFilterBitmap = true // bitmap anti alias
            pLine.shader = LinearGradient(
                0f,
                300f,
                0f,
                0f,
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                Shader.TileMode.MIRROR
            )

            pBaseLine = Paint()
            pBaseLine.flags = Paint.ANTI_ALIAS_FLAG
            pBaseLine.isAntiAlias = true
            pBaseLine.isFilterBitmap = true
            pBaseLine.color = lineColor
            pBaseLine.strokeWidth = 2f

            pBaseLineX = Paint()
            pBaseLineX.flags = Paint.ANTI_ALIAS_FLAG
            pBaseLineX.isAntiAlias = true
            pBaseLineX.isFilterBitmap = true
            pBaseLineX.color = lineColor
            pBaseLineX.strokeWidth = 2f
            pBaseLineX.style = Paint.Style.STROKE
            pMarkText = Paint()
            pMarkText.flags = Paint.ANTI_ALIAS_FLAG
            pMarkText.isAntiAlias = true
            pMarkText.color = Color.WHITE
        }
    }
}