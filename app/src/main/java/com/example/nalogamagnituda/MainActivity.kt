package com.example.nalogamagnituda

import android.graphics.Color
import android.hardware.Sensor
import androidx.appcompat.app.AppCompatActivity
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.github.mikephil.charting.charts.LineChart
import android.os.Bundle
import com.example.nalogamagnituda.R
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import android.hardware.SensorEvent
import android.view.View
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.data.LineDataSet
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private lateinit var mChart: LineChart
    private var thread: Thread? = null
    private var plotData = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)

        mChart = findViewById(R.id.chart1)

        val data = LineData()
        data.setValueTextColor(Color.GREEN)

        mChart.data = data    // empty data

        with(mChart) {
            description.isEnabled = false
            isDragEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(true)
            setDrawGridBackground(false)
            setPinchZoom(true)
            setBackgroundColor(Color.WHITE)
            setDrawBorders(false)

            legend.form = Legend.LegendForm.LINE
            legend.textColor = Color.BLUE

            xAxis.textColor = Color.WHITE
            xAxis.setDrawGridLines(true)
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.isEnabled = true

            axisLeft.textColor = Color.WHITE
            axisLeft.axisMinimum = -1F
            axisLeft.axisMaximum = 10F
            axisLeft.setDrawGridLines(false)

            axisRight.isEnabled = false
        }

        feedMultiple()
    }

    private fun addEntry(event: SensorEvent) {
        val values = event.values

        val x = values[0]
        val y = values[1]
        val z = values[2]

        var magnitude: Float = sqrt((x * x + y * y + z * z))

        val data = mChart.data
        if (data != null) {
            var set = data.getDataSetByIndex(0)

            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }


            data.addEntry(Entry(set.entryCount.toFloat(), magnitude), 0)
            data.notifyDataChanged()

            with(mChart) {
                notifyDataSetChanged()
                setVisibleXRangeMaximum(1500f)
                moveViewToX(data.entryCount.toFloat())   // move to the latest entry
            }
        }
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, "Magnitude")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.lineWidth = 3f
        set.color = Color.RED
        set.isHighlightEnabled = false
        set.setDrawValues(false)
        set.setDrawCircles(false)
        set.mode = LineDataSet.Mode.LINEAR
        //set.cubicIntensity = 0.2f
        return set
    }

    private fun feedMultiple() {
        if (thread != null) {
            thread!!.interrupt()
        }
        thread = Thread {
            while (true) {
                plotData = true
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        thread!!.start()
    }

    override fun onPause() {
        super.onPause()
        if (thread != null) {
            thread!!.interrupt()
        }
        mSensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (plotData) {
            addEntry(event)
            plotData = false
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onDestroy() {
        mSensorManager.unregisterListener(this@MainActivity)
        thread!!.interrupt()
        super.onDestroy()
    }
}