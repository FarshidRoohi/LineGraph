##Line Chart


  ![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat)


##### screenShot: 
 
 <img src="https://raw.githubusercontent.com/FarshidRoohi/LineGraph/master/art/ScreenShot.png" alt="line chart android" width="527px" height="278px">


 ###### Gradle :   
  
```Gradle  
  implementation 'io.github.farshidroohi:lineGraph:1.0.2'
 ```  
 ```xml  
        <io.github.farshidroohi.LineChart
            android:id="@+id/lineChart"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            app:chart_bg_color="#FF2B4A83"
            app:chart_line_color="#32FFFFFF"
            app:chart_line_size="4dp"
            app:chart_padding_bottom="20dp"
            app:chart_padding_left="20dp"
            app:chart_padding_right="20dp"
            app:chart_padding_top="20dp" />

 ```
 ```kotlin
 
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firstChartEntity = ChartEntity(Color.WHITE, graph1)
        val secondChartEntity = ChartEntity(Color.YELLOW, graph2)

        val list = ArrayList<ChartEntity>().apply {
            add(firstChartEntity)
            add(secondChartEntity)
        }

        val lineChart = findViewById<LineChart>(R.id.lineChart)
        lineChart.setLegend(legendArr)
        lineChart.setList(list)
    }

    private val firstChartEntity = floatArrayOf(113000f, 183000f, 188000f, 695000f, 324000f, 230000f, 188000f, 15000f, 126000f, 5000f, 33000f)
    private val secondChartEntity = floatArrayOf(0f, 245000f, 1011000f, 1000f, 0f, 0f, 47000f, 20000f, 12000f, 124400f, 160000f)
    private val legendArr = listOf("05/21", "05/22", "05/23", "05/24", "05/25", "05/26", "05/27", "05/28", "05/29", "05/30", "05/31")

}

 ```
 <hr>
 
 Goal :</br>
 swipe chart line and show data chart tips
