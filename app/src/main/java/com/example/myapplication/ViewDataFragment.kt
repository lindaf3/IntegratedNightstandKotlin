package com.example.myapplication

import CloudClient
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.androidplot.xy.*
import java.io.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class ViewDataFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_data, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val times = context?.getSharedPreferences("times", Context.MODE_PRIVATE)
        var startHour : Int = times?.getInt("start hour", -1) ?: -1
        var startMinute: Int = times?.getInt("start minute", -1) ?: -1
        var endHour: Int = times?.getInt("end hour", -1) ?: -1
        var endMinute :Int = times?.getInt("end minute", -1) ?: -1
        var domBounds = getDomBounds(startHour, startMinute, endHour, endMinute)

        val sleepDataX = mutableListOf<Double>()
        val sleepData = mutableListOf<Double>()
        val dataSeries: XYSeries = SimpleXYSeries(sleepDataX.toList(), sleepData.toList(), "Sleep Data" )
        val dataFormat = FastLineAndPointRenderer.Formatter(Color.BLUE, Color.BLACK, null, null)

        val graph = view.findViewById<XYPlot>(R.id.graph)
        val refresh = view.findViewById<Button>(R.id.refresh)
        val timeSet = view.findViewById<TextView>(R.id.timeSet)
        graph.addSeries(dataSeries, dataFormat)

        graph.setDomainBoundaries(domBounds.first, domBounds.second, BoundaryMode.FIXED)
        graph.setRangeBoundaries(0, 100, BoundaryMode.FIXED)

        refresh.setOnClickListener{
            sleepData.clear()
            sleepDataX.clear()
            graph.clear()
            startHour = times?.getInt("start hour", -1) ?: -1
            startMinute = times?.getInt("start minute", -1) ?: -1
            endHour = times?.getInt("end hour", -1) ?: -1
            endMinute = times?.getInt("end minute", -1) ?: -1
            domBounds = getDomBounds(startHour, startMinute, endHour, endMinute)

            val updatedData = updateGraph()
            for ( (time,amplitude) in updatedData) {
                if(withinRange(time, startHour, startMinute, endHour, endMinute)){
                    sleepDataX.add(time)
                    sleepData.add(amplitude)
                }
            }
            val updatedDataSeries: XYSeries = SimpleXYSeries(sleepDataX.toList(), sleepData.toList(), "Sleep Data" )
            graph.addSeries(updatedDataSeries, dataFormat)
            if(!AlarmClock.notInitiated(startHour, startMinute, endHour, endMinute)){
                timeSet.text = String.format("Start Time: %s, End Time: %s", AlarmClock.getAlarmTime(startHour, startMinute), AlarmClock.getAlarmTime(endHour, endMinute) )
            }
            else{
                timeSet.text = "No given valid alarm interval"
            }

            graph.setDomainBoundaries(domBounds.first, domBounds.second, BoundaryMode.FIXED)
            graph.redraw()

            Toast.makeText(requireActivity().application, "Updated Data!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getDomBounds(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Pair<Double, Double>
    {
        var domLow = timeToDouble(startHour, startMinute)
        var domHigh = timeToDouble(endHour, endMinute)
        if(AlarmClock.notInitiated(startHour, startMinute, endHour, endMinute)){
            domHigh = 24.0
            domLow = 0.0
        }
        if(startHour == 23 && endHour == 0){
            domHigh = 12.0 + endMinute/60.0
            domLow = 11.0 + startMinute/60.0
            println(domLow)
            println(domHigh)
        }
        return Pair(domLow, domHigh)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateGraph(): MutableList<Pair<Double, Double>> {
        val cloudClient = CloudClient("/path/key.pem", "longAPIToken")
        val date = ZonedDateTime.now()
        val dateString = date.format(DateTimeFormatter.ISO_DATE)
        println(dateString)
        val parsedCloudData = mutableListOf<Pair<Double,Double>>()
        val cloudData = cloudClient.querySoundData(dateString)
        for(data: Pair<String, Double> in cloudData) {
            parsedCloudData.add(getCloudDataPoint(data.first, data.second))
        }
        return parsedCloudData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCloudDataPoint(timestamp: String, amplitude: Double): Pair<Double, Double>{
        val datetime = ZonedDateTime.parse(timestamp)
        val time: Double = timeToDouble(datetime.hour,datetime.minute)
        println(datetime.hour)
        println(datetime.minute)
        println(time)
        return Pair(time,amplitude)
    }

    private fun timeToDouble(hour: Int, min: Int): Double{
        return hour + min/60.0
    }
    private fun withinRange(time: Double, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean{
        if(AlarmClock.notInitiated(startHour, startMinute, endHour, endMinute)){
            return false
        }
        val minInterval = timeToDouble(startHour, startMinute)
        val maxInterval = timeToDouble(endHour, endMinute)
        return time in maxInterval..minInterval
    }

}




