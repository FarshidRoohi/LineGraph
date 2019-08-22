package ir.farshid_roohi.linegraph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import ir.farshid_roohi.utilites.GraphCanvasWrapper
import ir.farshid_roohi.utilites.GraphPath
import java.util.*

/**
 * Created by Farshid Roohi.
 * Graph | Copyrights 2019-08-21.
 */
class LineChart : SurfaceView, SurfaceHolder.Callback {

    var padding: Int = 80
    var maxValue: Long = 0
    var marginTop: Int = 50
    var increment: Long = 20
    var animationDuration: Long = 200
    var legendArray: Array<String>? = null

    private val lineColor = Color.parseColor("#32FFFFFF")
    private val darkBlueColor = Color.parseColor("#FF2B4A83")

    private var chartEntities: List<ChartEntity>
    private var surfaceHolder: SurfaceHolder
    private var drawThread: DrawThread? = null

    private val touchLock = Any()

    constructor(context: Context?, lines: List<ChartEntity>) : super(context) {
        this.chartEntities = lines
        this.surfaceHolder = holder
        this.surfaceHolder.addCallback(this)

        val maxes = ArrayList<Float>()
        for (lineGraph in lines) {
            val copies =
                Arrays.copyOf(lineGraph.values, lineGraph.values.size)
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
            isRun = false
            drawThread = null
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (this.drawThread == null) {
            this.drawThread = DrawThread(surfaceHolder)
            this.drawThread!!.start()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val x = event.x.toInt()
        val y = event.y.toInt()

        if (drawThread == null) {
            return false
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                synchronized(touchLock) {
                    drawThread!!.isDirty = true
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                synchronized(touchLock) {
                    drawThread!!.isDirty = true
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                synchronized(touchLock) {
                    drawThread!!.isDirty = true
                }
                return true
            }
            else -> return super.onTouchEvent(event)
        }

    }

    internal inner class DrawThread(val surfaceHolder: SurfaceHolder) : Thread() {
        var isRun = true
        var isDirty = true

        private var anim = 0.0f
        private val height = this@LineChart.height
        private val width = this@LineChart.width

        private val xLength = width - (padding * 2)
        private val yLength = height - (padding * 2 + marginTop)

        private val chartXLength = width - (padding * 2)

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

            while (isRun) {
                if (!isDirty) {
                    sleep(100)
                    continue
                }
                canvas = surfaceHolder.lockCanvas()
                graphCanvasWrapper = GraphCanvasWrapper(
                    canvas,
                    width,
                    height,
                    padding,
                    padding
                )

                calcTimePass()
                synchronized(surfaceHolder) {

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
//                        drawGraphRegion(graphCanvasWrapper)
                    this.drawXText(graphCanvasWrapper)
                    this.drawGraph(graphCanvasWrapper)

                    this.surfaceHolder.unlockCanvasAndPost(graphCanvasWrapper.canvas)
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

                var degree: Int
                var px = x + rect.exactCenterX()
                var py = y + rect.exactCenterY()
                if (i == 0) {
                    degree = -45
                    px += 10f
                    py -= 10f
                } else {
                    degree = -45
                    px += 10f
                    py -= 10f
                }

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

            var value: Int
            var mode: Float

            this.pCircleBG.color = darkBlueColor

            for (i in 0 until chartEntities.size) {
                val linePath = GraphPath(width, height, padding, padding)
                var first = false

                var x: Float
                var y: Float

                this.p.color = chartEntities[i].color
                this.pCircle.color = chartEntities[i].color

                val gap = xLength / (chartEntities[i].values.size - 1)
                value = (anim / 1.0f).toInt()
                mode = anim % 1.0f

                run {
                    var j = 0
                    while (j < value + 1) {
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

                }

                graphCanvasWrapper.canvas.drawPath(linePath, p)

                var j = 0
                while (j < value + 1) {
                    if (j < chartEntities[i].values.size) {
                        x = (gap * j).toFloat()
                        y = yLength * chartEntities[i].values[j] / maxValue
                        graphCanvasWrapper.drawCircle(x, y, 8.0f, pCircleBG)
                        graphCanvasWrapper.drawCircle(x, y, 4.0f, pCircle)
                    }
                    j++
                }
            }


        }

        private fun drawGraphRegion(graphCanvasWrapper: GraphCanvasWrapper) {
            var prevX = 0f
            var prevY = 0f

            var nextX = 0f
            var nextY: Float

            var value: Int
            var mode: Float

            for (i in 0 until chartEntities.size) {
                val regionPath = GraphPath(
                    width,
                    height,
                    padding,
                    padding
                )


                var firstSet = false
                var x: Float
                var y: Float
                p.color = chartEntities[i].color
                pCircle.color = chartEntities[i].color
                val xGap = xLength / (chartEntities[i].values.size - 1)

                value = (anim / 1).toInt()
                mode = anim % 1

                for (j in 0..value) {
                    if (j > chartEntities[i].values.size - 1) {
                        return
                    }

                    x = (xGap * j).toFloat()
                    y = yLength * chartEntities[i].values[j] / maxValue

                    if (!firstSet) {
                        regionPath.moveTo(x, 0.0f)
                        regionPath.lineTo(x, y)

                        firstSet = true
                    } else {
                        if (j > value) {
                            nextX = x - prevX
                            nextY = y - prevY
                            regionPath.lineTo(prevX + nextX * mode, prevY + nextY * mode)
                        } else {
                            regionPath.lineTo(x, y)
                        }
                    }

                    prevX = x
                    prevY = y
                }
            }
        }


        private fun calcTimePass() {
            val curTime = System.currentTimeMillis()
            var gapTime = curTime - animStartTime
            val animDuration = animationDuration
            if (gapTime >= animDuration) {
                gapTime = animDuration
                isDirty = false
            }
            this.anim = chartEntities[0].values.size * gapTime.toFloat() / animDuration.toFloat()
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