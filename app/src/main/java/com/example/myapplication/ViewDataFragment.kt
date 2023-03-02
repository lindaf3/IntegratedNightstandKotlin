package com.example.myapplication

import CloudClient
import android.annotation.SuppressLint
import android.graphics.Color
import android.media.*
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.androidplot.xy.*
import com.example.myapplication.Datetime.datetimeToDouble
import java.io.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


private const val TIME_START = "FROM"
private const val TIME_END = "TO"
private const val UNINITIALIZED_INT = -1
private val NO_DATETIME_STRING = null
private const val CLEAR = "clear"
private const val UNINITIALIZED_DATE_TEXT = "INVALID"

class ViewDataFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_view_data, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var startHour : Int = UNINITIALIZED_INT
        var startMinute: Int = UNINITIALIZED_INT
        var endHour: Int = UNINITIALIZED_INT
        var endMinute :Int = UNINITIALIZED_INT
        val cloudClient = CloudClient("/path/key.pem", "longAPIToken")




        @RequiresApi(Build.VERSION_CODES.O)
        fun queryTimes() {
            val interval = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cloudClient.queryInterval()
            } else {
                TODO("VERSION.SDK_INT < O")
            }

            if(interval.first != NO_DATETIME_STRING && interval.first != CLEAR
                && interval.second != NO_DATETIME_STRING && interval.second != CLEAR){
                val startDatetime = LocalDateTime.parse(interval.first)
                val endDatetime = LocalDateTime.parse(interval.second)
                startHour = startDatetime.hour
                startMinute = startDatetime.minute
                endHour = endDatetime.hour
                endMinute = endDatetime.minute
            }
            else{
                startHour = UNINITIALIZED_INT
                startMinute = UNINITIALIZED_INT
                endHour = UNINITIALIZED_INT
                endMinute = UNINITIALIZED_INT
            }

        }
        queryTimes()

        val alarmStartDatetime: LocalDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(startHour, startMinute))
        val alarmEndDatetime: LocalDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(endHour, endMinute))
        var timeStartDatetime: LocalDateTime? = alarmStartDatetime
        var timeEndDatetime: LocalDateTime? = alarmEndDatetime


        var domBounds = getDomBounds(alarmStartDatetime, alarmEndDatetime)

        val sleepDataX = mutableListOf<Double>()
        val sleepData = mutableListOf<Double>()
        var dataSeries: XYSeries = SimpleXYSeries(sleepDataX, sleepData, "Sleep Data" )
        val dataFormat = FastLineAndPointRenderer.Formatter(Color.BLUE, Color.BLACK, null, null)

        val graph = view.findViewById<XYPlot>(R.id.graph)
        val fetch = view.findViewById<Button>(R.id.fetch)
        val timeStart = view.findViewById<Button>(R.id.timeStart)
        val timeEnd = view.findViewById<Button>(R.id.timeEnd)
        val clear = view.findViewById<Button>(R.id.clear)

        if (timeStartDatetime != null) {
            Datetime.setDatetime(timeStartDatetime, timeStart)
        }
        if (timeEndDatetime != null) {
            Datetime.setDatetime(timeEndDatetime, timeEnd)
        }


        graph.addSeries(dataSeries, dataFormat)
        graph.setDomainBoundaries(domBounds.first, domBounds.second, BoundaryMode.FIXED)
        graph.setRangeBoundaries(0, 5, BoundaryMode.FIXED)


        fun revert(){
            timeStartDatetime = null
            timeEndDatetime = null
            timeStart.text = TIME_START
            timeEnd.text = TIME_END
            graph.clear()
            sleepDataX.clear()
            graph.redraw()
        }



        clear.setOnClickListener{
            revert()
            Toast.makeText(requireActivity().application, "Query Cleared!", Toast.LENGTH_SHORT).show()
        }

        fun alarmPicker(datetime: LocalDateTime, button: Button, title: String, returnText: String) {
            val dialog = DatetimePickerFragment(datetime, title)
            dialog.show(requireActivity().supportFragmentManager, title)
            var datetimeText: String
            setFragmentResultListener("requestKey") { _, bundle ->
                val undo = bundle.getBoolean("cancel")
                if (!undo) {
                    val validData = bundle.getBoolean("use?")
                    if (validData) {
                        datetimeText = bundle.getString("datetimeText") ?: UNINITIALIZED_DATE_TEXT
                        val returnDatetime = LocalDateTime.parse(datetimeText)
                        Datetime.setDatetime(returnDatetime, button)
                        if(returnText == TIME_START){
                            timeStartDatetime = returnDatetime
                        }
                        else{
                            timeEndDatetime = returnDatetime
                        }
                    } 
                else {
                        button.text = returnText
                        Toast.makeText(
                            requireActivity().application,
                            "Invalid Datetime",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                

            }

        }

        
        timeStart.setOnClickListener{
            queryTimes()
            val datetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(startHour, startMinute))
            alarmPicker(datetime, timeStart,"Select Time Start:", TIME_START)
           
        }
        timeEnd.setOnClickListener{
            queryTimes()
            val datetime = LocalDateTime.of(LocalDate.now(), LocalTime.of(endHour, endMinute))
            alarmPicker(datetime, timeEnd, "Select Time End:", TIME_END)
        }


        fun updateGraph(startInterval: LocalDateTime?, endInterval: LocalDateTime?, cloudClient: CloudClient): MutableList<Pair<Double, Double>> {
            val parsedCloudData = mutableListOf<Pair<Double,Double>>()
            val startString: String = startInterval?.format(DateTimeFormatter.ISO_DATE_TIME) ?: UNINITIALIZED_DATE_TEXT
            val endString: String = endInterval?.format(DateTimeFormatter.ISO_DATE_TIME) ?: UNINITIALIZED_DATE_TEXT
            val cloudData = cloudClient.querySoundData(startString, endString)
            for(data: Pair<String, Double> in cloudData) {
                parsedCloudData.add(getCloudDataPoint(data.first, data.second, startInterval))
            }
            return parsedCloudData
        }

        fetch.setOnClickListener{
            sleepData.clear()
            sleepDataX.clear()
            graph.clear()
            val validQuery = timeStartDatetime != null && timeEndDatetime != null && timeStartDatetime!! < timeEndDatetime
            if(!validQuery){
                revert()
                Toast.makeText(requireActivity().application, "Invalid query, Please try again", Toast.LENGTH_SHORT).show()
                graph.redraw()
            }
            else{
                val updatedData = updateGraph(timeStartDatetime, timeEndDatetime, cloudClient)


                domBounds = getDomBounds(timeStartDatetime, timeEndDatetime)
                for ( (time,amplitude) in updatedData) {
                    if(withinRange(time, domBounds.first, domBounds.second)){
                        sleepDataX.add(time)
                        sleepData.add(amplitude)
                    }
                }
                dataSeries = SimpleXYSeries(sleepDataX, sleepData, "Sleep Data" )
                graph.addSeries(dataSeries, dataFormat)
                graph.setDomainBoundaries(domBounds.first, domBounds.second, BoundaryMode.FIXED)
                graph.redraw()
                if(dataSeries.size() == 0){
                    Toast.makeText(requireActivity().application, "No sleep data was recorded.", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(requireActivity().application, "Updated data!", Toast.LENGTH_SHORT).show()
                }


            }


        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDomBounds(start: LocalDateTime?, end: LocalDateTime?):  Pair<Double, Double>{
        val bounds = Pair(datetimeToDouble(start, start), datetimeToDouble(start, end))
        println(bounds)
        return bounds

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCloudDataPoint(timestamp: String, amplitude: Double, startDatetime: LocalDateTime?): Pair<Double, Double>{
        val endDatetime = LocalDateTime.parse(timestamp)
        val time: Double = datetimeToDouble(startDatetime, endDatetime)
        return Pair(time,amplitude)
    }


    private fun withinRange(time: Double, minInterval: Double, maxInterval: Double): Boolean{
        return time in minInterval..maxInterval
    }

}




