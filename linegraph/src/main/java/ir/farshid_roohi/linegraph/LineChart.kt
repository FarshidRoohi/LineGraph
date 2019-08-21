package ir.farshid_roohi.linegraph

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

/**
 * Created by Farshid Roohi.
 * Graph | Copyrights 2019-08-21.
 */
class LineChart : SurfaceView, SurfaceHolder.Callback {

    var padding: Int = 0
    var maxValue: Long = 0
    var marginTop: Int = 0
    var increment: Long = 0
    var animationDuration: Int = 200

    private val lineClor = Color.parseColor("#32FFFFFF")
    private val darkBlueColor = Color.parseColor("#FF2B4A83")

    private var chartEntities: List<ChartEntity>
    private lateinit var surfaceHolder: SurfaceHolder
    private var drawThread: DrawThread? = null


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
            this.drawThread = DrawThread(context,surfaceHolder)
            this.drawThread!!.start()
        }
    }

    inner class DrawThread(val context: Context, val surfaceHolder: SurfaceHolder) : Thread() {
        var isRun = true
        private var isDirty = true

        private var anim = 0.0f
        private val height = this@LineChart.height
        private val width = this@LineChart.width

        private val xHeight = height - (this@LineChart.padding + marginTop)
        private val xWidth = width - (this@LineChart.padding + +marginTop)

        private var p = Paint()
        private var pCircle = Paint()
        private var pCircleBG = Paint()
        private var pLine = Paint()
        private var pBaseLine = Paint()
        private var pBaseLineX = Paint()
        private var pMarkText = Paint()

        private var animStartTime: Long = -1

        override fun run() {
            var canvas: Canvas? = null

            this.initializePaint()
            this.animStartTime = System.currentTimeMillis()

            while (isRun) {
                if (!isDirty) {
                    sleep(100)
                    continue
                }
                canvas = surfaceHolder.lockCanvas()
                calcTimePass()

                synchronized(surfaceHolder) {
                    canvas.drawColor(darkBlueColor)
                    canvas.drawLine(
                        0.0f + padding,
                        0.0f + padding,
                        xHeight.toFloat(),
                        0.0f,
                        pBaseLine
                    )

                    val x = 0
                    val len = chartEntities[0].values.size
                    var gap = xHeight / (len - 1)

                    for (i in 0..chartEntities[0].values.size) {
                        gap *= i
                        canvas.drawLine(
                            x.toFloat(),
                            0.0f,
                            x.toFloat(),
                            maxValue.toFloat(),
                            pBaseLine
                        )

                    }
                    drawGraphRegionWithAnimation(canvas)



                    this.surfaceHolder.unlockCanvasAndPost(canvas)
                }

            }

        }

        private fun drawGraphRegionWithAnimation(canvas: Canvas) {
            //for draw animation
            var prev_x = 0f
            var prev_y = 0f

            var next_x = 0f
            var next_y: Float

            var value: Float
            var mode: Float


            for (i in 0 until chartEntities.size) {
                val path = Path()

                var firstSet = false
                var x :Float
                var y :Float
                p.color = chartEntities[i].color
                pCircle.color = chartEntities[i].color
                val xGap = xHeight / (chartEntities[i].values.size - 1)

                value = (anim / 1)
                mode = anim % 1

                for (j in 0..(value + 1).toInt()) {
                    if (j < chartEntities[i].values.size) {

                        if (!firstSet) {

                            x = (xGap * j).toFloat()
                            y = xWidth * chartEntities[i].values[j] / maxValue

                            path.moveTo(x, 0.0f)
                            path.lineTo(x, y)

                            firstSet = true
                        } else {
                            x = (xGap * j).toFloat()
                            y =
                                xWidth * chartEntities[i].values[j] / maxValue

                            if (j > value) {
                                next_x = x - prev_x
                                next_y = y - prev_y
                                path.lineTo(prev_x + next_x * mode, prev_y + next_y * mode)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        prev_x = x
                        prev_y = y
                    }
                }
                var xbBg = prev_x + next_x * mode
                if (xbBg >= xWidth) {
                    xbBg = xWidth.toFloat()
                }
                path.lineTo(xbBg, 0.0f)
                path.lineTo(0.0f, 0.0f)

                val pBg = Paint()
                pBg.flags = Paint.ANTI_ALIAS_FLAG
                pBg.isAntiAlias = true //text anti alias
                pBg.isFilterBitmap = true // bitmap anti alias
                pBg.style = Paint.Style.FILL
                pBg.color = chartEntities[i].color
                canvas.drawPath(path, pBg)
            }
        }


        private fun calcTimePass() {
            val curTime = System.currentTimeMillis()
            var gapTime = curTime - animStartTime
            val animDu = animationDuration
            if (gapTime >= animDu) {
                gapTime = animDu.toLong()
                isDirty = false
            }
            this.anim = chartEntities[0].values.size * gapTime.toFloat() / animDu.toFloat()
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
            pBaseLine.color = lineClor
            pBaseLine.strokeWidth = 2f

            pBaseLineX = Paint()
            pBaseLineX.flags = Paint.ANTI_ALIAS_FLAG
            pBaseLineX.isAntiAlias = true
            pBaseLineX.isFilterBitmap = true
            pBaseLineX.color = lineClor
            pBaseLineX.strokeWidth = 2f
            pBaseLineX.style = Paint.Style.STROKE
            pMarkText = Paint()
            pMarkText.flags = Paint.ANTI_ALIAS_FLAG
            pMarkText.isAntiAlias = true
            pMarkText.color = Color.WHITE
        }
    }
}