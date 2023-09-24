package com.example.ud1000

//import android.graphics.Color

import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ud1000.ui.theme.Ud1000Theme
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        SharedPreferencesUtil.init(this)



        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            val tg = AudioToneGenerator.getInstance()
            tg.setFrequency(432.0)
            tg.start()
            tg.fadeIn(500, 1f)
//            var a = 1f
//            var ad = 0.05f
//            var f = 400.0
//            tg.setFrequency(f)
//            tg.setVolume(a)
//            while (true) {
//                tg.setFrequency(f)
//                tg.setVolume(a)
//
//                a += ad
//                f += 10.0
//
//                if (a > 0.9f) {
//                    ad = -0.02f
//                }
//
//                if (a < 0.05f) {
//                    ad = 0.02f
//                }
//
//                if (f > 1000.0) {
//                    f = 50.0
//                }
//                delay(20)
//            }
        }

        setContent {

            val view = LocalView.current
            val context = LocalContext.current
            val controller = WindowInsetsControllerCompat(window, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.navigationBars())

            Ud1000Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(3f)
                                .fillMaxSize()
                        ) {
                            MainControls()
                        }

                        Surface(
                            modifier = Modifier
                                .weight(4f)
                                .fillMaxSize(),
                        ) {
                            LineChartComposable()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Ud1000Theme {
        Greeting("Android")
    }
}

@Composable
fun LineChartComposable() {
    val coroutineScope = CoroutineScope(Dispatchers.Main)
    val context = LocalContext.current
    val lineChart = remember { LineChart(context) }
    var xValue = 0.0f;

    lineChart.disableScroll()
    lineChart.xAxis.setDrawLabels(false)
    lineChart.axisLeft.textColor = Color.WHITE
    lineChart.axisRight.textColor = Color.WHITE
    lineChart.xAxis.setDrawGridLines(false)
    lineChart.xAxis.setDrawAxisLine(false)
    lineChart.axisLeft.setDrawGridLines(false)
    lineChart.axisRight.setDrawGridLines(false)
    lineChart.axisRight.setDrawZeroLine(false)
    lineChart.axisLeft.setDrawAxisLine(false)
    lineChart.axisRight.setDrawAxisLine(false)
    lineChart.axisLeft.setDrawLabels(false)
    lineChart.description.isEnabled = false
    lineChart.legend.isEnabled = false
    lineChart.isHighlightPerTapEnabled = false
    lineChart.isHighlightPerDragEnabled = false

    fun setShowEntries(n: Float) {
        lineChart.setVisibleXRange(256f, 256f)
        lineChart.setVisibleYRange(-15_000f, 15_000f, lineChart.axisLeft.axisDependency)
        lineChart.moveViewToX(0f)
        lineChart.invalidate()
    }

    setShowEntries(100f)

    val entries = ArrayList<Entry>()

    val lineDataSet = LineDataSet(entries, "Sine Wave")
    lineDataSet.setDrawCircles(false)
    lineDataSet.setDrawValues(false)
    lineDataSet.lineWidth = 3.0f

    lineDataSet.setColor(MaterialTheme.colorScheme.primary.toArgb())

    val lineData = LineData(lineDataSet)

    lineChart.data = lineData

    remember {
        coroutineScope.launch {
            while (isActive) {
                // Generate the sine wave value
                val yValue = sin(xValue) * 15_000

                // Add the new Entry to the LineDataSet
                lineDataSet.addEntry(Entry(xValue, yValue.toFloat()))

                if (lineDataSet.entryCount > 256) {
                    lineDataSet.removeFirst()
                }

                // Notify the chart data has changed
                lineData.notifyDataChanged()
                lineChart.notifyDataSetChanged()
                lineChart.invalidate()

                // Update the xValue
                xValue += 0.1f

                // Delay for a short while
            delay(20)
            }
        }
    }


    AndroidView({ lineChart }) {
        // Update UI here if needed
    }
}