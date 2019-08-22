package ir.farshid_roohi.graph

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ir.farshid_roohi.linegraph.ChartEntity
import ir.farshid_roohi.linegraph.LineChart
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val graph1 = floatArrayOf(113000f, 183000f, 188000f, 695000f, 324000f, 230000f, 188000f)
        val graph2 = floatArrayOf(0f, 245000f, 1011000f, 1000f, 0f, 0f, 47000f)

        val firstChartEntity = ChartEntity(Color.WHITE, graph1)
        val secondChartEntity = ChartEntity(Color.YELLOW, graph2)

        val list= ArrayList<ChartEntity>()
        list.add(firstChartEntity)
        list.add(secondChartEntity)

        val lineChart = LineChart(this,list)
        lineChart.animationDuration = 2000

        chartLayout.addView(lineChart)

    }
}
