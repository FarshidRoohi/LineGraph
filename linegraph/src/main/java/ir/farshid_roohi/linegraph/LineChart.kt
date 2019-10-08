package ir.farshid_roohi.linegraph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import ir.farshid_roohi.utilites.GraphCanvasWrapper
import ir.farshid_roohi.utilites.GraphPath
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs


/**
 * Created by Farshid Roohi.
 * Graph | Copyrights 2019-08-21.
 */
@SuppressLint("ViewConstructor")
class LineChart : View {

    private var mPaddingTop: Float = 40f
    var mPaddingRight: Float = 40f
    var mPaddingLeft: Float = 40f
    var mPaddingBottom: Float = 90f
    var maxValue: Long = 0

    var lineColor: Int = 0
    var bgColor: Int = 0
    var typeFace: Typeface? = null

    private var xLength: Int = 0
    private var yLength: Int = 0

    private var chartXLength: Int = 0
    private var chartYLength: Int = 0

    private var p = Paint()
    private var pCircle = Paint()
    private var pCircleBG = Paint()
    private var pLine = Paint()
    private var pBaseLine = Paint()
    private var pBaseLineX = Paint()
    private var pMarkText = Paint()

    private var legendArray: MutableList<String> = mutableListOf()

    private var chartEntities: MutableList<ChartEntity> = mutableListOf()

    constructor(context: Context?) : super(context) {
        initialize(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(attrs)
    }

    private fun initialize(attrs: AttributeSet?) {

        attrs?.let {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.LineChart)

            try {
                this.bgColor = typeArray.getColor(
                    R.styleable.LineChart_chart_bg_color,
                    ContextCompat.getColor(context, R.color.mid_blue)
                )
                this.lineColor = typeArray.getColor(
                    R.styleable.LineChart_chart_line_color,
                    ContextCompat.getColor(context, R.color.light_white)
                )
                this.mPaddingTop =
                    typeArray.getDimension(R.styleable.LineChart_chart_padding_top, 20f)
                this.mPaddingRight =
                    typeArray.getDimension(R.styleable.LineChart_chart_padding_right, 20f)
                this.mPaddingBottom =
                    typeArray.getDimension(R.styleable.LineChart_chart_padding_bottom, 20f)
                this.mPaddingLeft =
                    typeArray.getDimension(R.styleable.LineChart_chart_padding_left, 20f)

            } finally {
                typeArray.recycle()

            }
        }

        val graph1 = floatArrayOf(113000f, 183000f, 188000f, 695000f, 324000f, 230000f, 188000f)
        val graph2 = floatArrayOf(0f, 245000f, 1011000f, 1000f, 0f, 0f, 47000f)

        val arrGraph = ArrayList<ChartEntity>().apply {
            add(ChartEntity(Color.GRAY, graph1))
            add(ChartEntity(Color.WHITE, graph2))
        }

        setList(arrGraph)
        invalidate()
    }

    fun setLegend(list: ArrayList<String>) {
        setLegend(list.toList())
    }

    fun setLegend(list: List<String>) {
        legendArray.clear()

        if (list.isNotEmpty())
            legendArray.addAll(list)
    }

    fun setList(list: ArrayList<ChartEntity>) {
        setList(list.toList())
    }

    fun setList(list: List<ChartEntity>) {
        this.chartEntities.clear()
        invalidate()

        if (list.isEmpty()) //no need to go farther
            return

        this.chartEntities.addAll(list)

        val maxes = ArrayList<Float>()
        for (lineGraph in chartEntities) {
            val copies = lineGraph.values.copyOf(lineGraph.values.size)
            copies.sort()
            maxes.add(copies[copies.size - 1])
        }
        this.maxValue = (Collections.max(maxes) as Float).toLong()
        this.initializePaint()
    }


    private var isMoved = false
    private var locationX = 0f
    private var locationY = 0f

    private var distanceX = 0f
    private var distanceY = 0f
    private var lastX = 0f
    private var lastY = 0f


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean = event?.let {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                isMoved = true
                locationX = event.x
                locationY = event.y

                distanceX += abs(lastX - event.x)
                distanceY += abs(lastY - event.y)
                lastY = event.y
                lastX = event.x
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                isMoved = false
                invalidate()
            }
        }

        true
    } ?: false

    private var valueMap: HashMap<Int, Float> = HashMap()
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }

        this.xLength = (width - (mPaddingLeft + mPaddingRight)).toInt()
        this.yLength = (height - (mPaddingBottom + mPaddingTop)).toInt()

        this.chartXLength = (width - (mPaddingLeft + mPaddingRight)).toInt()
        this.chartYLength = (height - (mPaddingTop + mPaddingBottom)).toInt()

        canvas.drawColor(bgColor)


        val graphCanvasWrapper = GraphCanvasWrapper(
            canvas,
            this.width,
            this.height,
            this.mPaddingLeft.toInt(),
            this.mPaddingBottom.toInt()
        )


        graphCanvasWrapper.drawLine(0.0f, 0.0f, chartXLength.toFloat(), 0.0f, pBaseLine)

        var newX: Float
        val gap = chartXLength / (chartEntities[0].values.size - 1)
        for (i in chartEntities[0].values.indices) {
            newX = (gap * i).toFloat()
            graphCanvasWrapper.drawLine(newX, 0.0f, newX, chartYLength.toFloat(), pBaseLine)

            drawGraph(graphCanvasWrapper)
            drawXText(graphCanvasWrapper)
        }


//        if (isMoved) {
//            Log.d("TAGA_AFF", "is moved")
//
//            val index = (locationX / width).toInt()
//            graphCanvasWrapper.drawLine(locationX, yLength.toFloat(), locationX, 0f, pBaseLine)
//            val a = valueMap[index]
//            graphCanvasWrapper.drawCircle(locationX, y, 8f, pCircle)
//        }

    }


    private fun drawXText(graphCanvas: GraphCanvasWrapper) {

        if (legendArray.isEmpty()) {
            return
        }

        var x: Float
        var y: Float
        val xGap = (xLength / (chartEntities[0].values.size - 1)).toFloat()

        for (i in chartEntities[0].values.indices) {
            val rect = Rect()
            val text = legendArray[i]
            pMarkText.measureText(text)
            pMarkText.textSize = 20f
            pMarkText.typeface = typeFace

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
        this.pCircleBG.color = bgColor

        for (m in chartEntities.indices) {
            val linePath = GraphPath(width, height, mPaddingLeft.toInt(), mPaddingBottom.toInt())
            var first = false

            var x: Float
            var y: Float

            this.p.color = chartEntities[m].color
            this.pCircle.color = chartEntities[m].color

            val xGap = xLength / (chartEntities[m].values.size - 1)

            for (j in chartEntities[m].values.indices) {

                if (j < chartEntities[m].values.size) {
                    x = (xGap * j).toFloat()
                    y = yLength * chartEntities[m].values[j] / maxValue

                    Log.d("TAGAG","x : $x | y : $y")

                    if (first) {
                        linePath.lineTo(x, y)
                    } else {
                        linePath.moveTo(x, y)
                        first = true
                    }

                    if (isMoved) {
                        graphCanvasWrapper.drawLine(locationX, yLength.toFloat(), locationX, 0f, pBaseLine)
//                        graphCanvasWrapper.drawCircle(locationX, locationY, 8f, pCircle)
                    }

                }
            }

            graphCanvasWrapper.canvas?.drawPath(linePath, p)


            for (t in chartEntities[m].values.indices) {
                if (t < chartEntities[m].values.size) {
                    x = (xGap * t).toFloat()
                    y = yLength * chartEntities[m].values[t] / maxValue

                    with(graphCanvasWrapper) {
                        drawCircle(x, y, 8.0f, pCircle)
                        drawCircle(x, y, 4.0f, pCircleBG)
                    }

                    valueMap[t] = x
                }

            }
        }


    }

    private fun initializePaint() {
        p = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            isAntiAlias = true
            isFilterBitmap = true
            color = Color.BLUE
            strokeWidth = 10f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }

        pCircle = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            isAntiAlias = true
            isFilterBitmap = true
            color = Color.BLUE
            strokeWidth = 20f
            style = Paint.Style.STROKE
        }

        pCircleBG = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            color = bgColor
            strokeWidth = 10f
            style = Paint.Style.FILL_AND_STROKE
        }

        pLine = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            isAntiAlias = true //text anti alias
            isFilterBitmap = true // bitmap anti alias
            shader = LinearGradient(
                0f,
                300f,
                0f,
                0f,
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                Shader.TileMode.MIRROR
            )
        }

        pBaseLine = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            isAntiAlias = true
            isFilterBitmap = true
            color = lineColor
            strokeWidth = 2f
        }

        pBaseLineX = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            isAntiAlias = true
            isFilterBitmap = true
            color = lineColor
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        pMarkText = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            isAntiAlias = true
            color = Color.WHITE
        }
    }
}