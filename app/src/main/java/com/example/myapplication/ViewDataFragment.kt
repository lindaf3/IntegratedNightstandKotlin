package com.example.myapplication

import Amplitude
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.androidplot.xy.*
import com.example.myapplication.AlarmClock.setAlarm
import com.example.myapplication.Date.setDate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val TIME_START = "Time Start"
private const val TIME_END = "Time End"
private const val UNINITIALIZED = -1
private val NO_DATETIME_STRING = null
private const val CLEAR = "clear"

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
        //TODO: change to query cloud
        var startHour : Int = UNINITIALIZED
        var startMinute: Int = UNINITIALIZED
        var endHour: Int = UNINITIALIZED
        var endMinute :Int = UNINITIALIZED
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
                startHour = UNINITIALIZED
                startMinute = UNINITIALIZED
                endHour = UNINITIALIZED
                endMinute = UNINITIALIZED
            }

        }
        queryTimes()
        var domBounds = getDomBounds(startHour, startMinute, endHour, endMinute)

        val sleepDataX = mutableListOf<Double>()
        val sleepData = mutableListOf<Double>()
        var dataSeries: XYSeries = SimpleXYSeries(sleepDataX, sleepData, "Sleep Data" )
        val dataFormat = FastLineAndPointRenderer.Formatter(Color.BLUE, Color.BLACK, null, null)

        val graph = view.findViewById<XYPlot>(R.id.graph)
        val fetch = view.findViewById<Button>(R.id.fetch)
        val timeStart = view.findViewById<Button>(R.id.timeStart)
        val timeEnd = view.findViewById<Button>(R.id.timeEnd)
        val sessionDate = view.findViewById<Button>(R.id.date)
        val clear = view.findViewById<Button>(R.id.clear2)



        var dataStartHour = UNINITIALIZED
        var dataStartMin = UNINITIALIZED
        var dataEndHour = UNINITIALIZED
        var dataEndMin = UNINITIALIZED
        var dateText = ""

        graph.addSeries(dataSeries, dataFormat)
        graph.setDomainBoundaries(domBounds.first, domBounds.second, BoundaryMode.FIXED)
        graph.setRangeBoundaries(0, 5, BoundaryMode.FIXED)

        clear.setOnClickListener{
            timeStart.text = TIME_START
            timeEnd.text = TIME_END
            sessionDate.text = "Date"
            dataStartHour = UNINITIALIZED
            dataStartMin = UNINITIALIZED
            dataEndHour = UNINITIALIZED
            dataEndMin = UNINITIALIZED
            dateText = ""
            graph.clear()
            graph.redraw()
            Toast.makeText(requireActivity().application, "Query Cleared!", Toast.LENGTH_SHORT).show()
        }

        timeStart.setOnClickListener{
            queryTimes()
            val timeStartHour = if (startHour < 0) 7 else startHour
            val timeStartMinute = if (startMinute < 0) 0 else startMinute
            val startTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTitleText("Select Time Start")
                .setHour(timeStartHour)
                .setMinute(timeStartMinute)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()
            startTimePicker.show(requireActivity().supportFragmentManager, "data start")
            startTimePicker.addOnPositiveButtonClickListener {
                dataStartHour = startTimePicker.hour
                dataStartMin= startTimePicker.minute
                setAlarm(startTimePicker, timeStart, TIME_START)
            }

        }
        timeEnd.setOnClickListener{
            queryTimes()
            val timeEndHour = if (endHour < 0) 7 else endHour
            val timeEndMinute = if (endMinute < 0) 30 else endMinute
            val endTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTitleText("Select Time End")
                .setHour(timeEndHour)
                .setMinute(timeEndMinute)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()
            endTimePicker.show(requireActivity().supportFragmentManager, "data end")
            endTimePicker.addOnPositiveButtonClickListener {
                dataEndHour = endTimePicker.hour
                dataEndMin = endTimePicker.minute
                setAlarm(endTimePicker, timeEnd, TIME_END)
            }

        }
        sessionDate.setOnClickListener{
            val datePicker: MaterialDatePicker<Long> = datePicker()
                .setTitleText("Select Session Date")
                .build()
            datePicker.show(requireActivity().supportFragmentManager, "data date")
            datePicker.addOnPositiveButtonClickListener {
                dateText = datePicker.headerText
                setDate(dateText, sessionDate)
            }
        }


        fetch.setOnClickListener{
            sleepData.clear()
            sleepDataX.clear()
            graph.clear()

            domBounds = getDomBounds(dataStartHour, dataStartMin, dataEndHour, dataEndMin)
            if(AlarmClock.notInitiated(dataStartHour,dataStartMin, dataEndHour, dataEndMin)){
                Toast.makeText(requireActivity().application, "Data time interval not given!", Toast.LENGTH_SHORT).show()
                graph.redraw()
            }

            else if(dateText == ""){
                Toast.makeText(requireActivity().application, "Session date is not given!", Toast.LENGTH_SHORT).show()
                graph.redraw()
            }


            else{
                val sesDate = Date.parseDate(dateText)
                val alarmTimes = convertTimesToLocalDates(dataStartHour,dataStartMin, dataEndHour, dataEndMin)
                if(Date.futureDate(sesDate)){
                    Toast.makeText(requireActivity().application, "Session date has not occurred yet!", Toast.LENGTH_SHORT).show()
                    graph.redraw()
                }
                else if(alarmTimes.first > alarmTimes.second && !(dataEndHour == 0 && dataStartHour != 1) ){
                    Toast.makeText(requireActivity().application, "Start time cannot occur later than end time.", Toast.LENGTH_SHORT).show()
                    graph.redraw()
                }

                else{
                    val updatedData: MutableList<Pair<Double, Double>> = updateGraph(sesDate, alarmTimes, cloudClient)
                    if(dataStartHour >= 12 && dataEndHour == 0){
                        val minInterval = timeToDouble(dataStartHour - 12, dataStartMin)
                        val maxInterval = timeToDouble(12, dataEndMin)
                        for ( (time,amplitude) in updatedData) {
                            val newTime = time-12
                            if(withinRange(newTime, minInterval, maxInterval)){
                                sleepDataX.add(newTime)
                                sleepData.add(amplitude)
                            }
                        }
                        //mock code: can delete later
                        sleepData.add(maxInterval)
                        sleepDataX.add(Amplitude.getAmplitude())
                    }
                    else{

                        val minInterval = timeToDouble(dataStartHour, dataStartMin)
                        val maxInterval = timeToDouble(dataEndHour, dataEndMin)
                        for ( (time,amplitude) in updatedData) {
                            if(withinRange(time, minInterval, maxInterval)){
                                sleepDataX.add(time)
                                sleepData.add(amplitude)
                            }
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
    }



    private fun getDomBounds(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Pair<Double, Double>
    {
        var domLow = timeToDouble(startHour, startMinute)
        var domHigh = timeToDouble(endHour, endMinute)
        if(AlarmClock.notInitiated(startHour, startMinute, endHour, endMinute)){
            domHigh = 24.0
            domLow = 0.0
        }
        if(endHour == 0 && startHour != 0){
            domHigh = timeToDouble(12,endMinute)
            if(startHour >= 12){
                domLow = timeToDouble(startHour - 12,endHour)
            }
        }
        return Pair(domLow, domHigh)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateGraph(sesDate: LocalDate, times: Pair<LocalTime, LocalTime>, cloudClient: CloudClient): MutableList<Pair<Double, Double>> {
//        val dateString = sesDate.format(DateTimeFormatter.ISO_DATE)
        val parsedCloudData = mutableListOf<Pair<Double,Double>>()
        val endDate = sesDate
        // for right now, I am allowing 12:00 am end times be valid for the database. This could be considered unacceptable in later editions.
        if(times.second.hour == 0 && times.first.hour != 0){
            endDate.minusDays(-1)
        }
        val startInterval = LocalDateTime.of(sesDate, times.first)
        val endInterval = LocalDateTime.of(endDate, times.second)
        val startString = startInterval.format(DateTimeFormatter.ISO_DATE_TIME)
        val endString = endInterval.format(DateTimeFormatter.ISO_DATE_TIME)
//        val cloudData = cloudClient.querySoundData(dateString, startString, endString)
        val cloudData = cloudClient.querySoundData(startString, endString)
        for(data: Pair<String, Double> in cloudData) {
            parsedCloudData.add(getCloudDataPoint(data.first, data.second))
        }
        return parsedCloudData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertTimesToLocalDates(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Pair<LocalTime, LocalTime>{
        val startTime = LocalTime.of(startHour, startMinute)
        val endTime = LocalTime.of(endHour, endMinute)
        return Pair(startTime, endTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCloudDataPoint(timestamp: String, amplitude: Double): Pair<Double, Double>{
        val datetime = LocalDateTime.parse(timestamp)
        val time: Double = timeToDouble(datetime.hour,datetime.minute)
        return Pair(time,amplitude)
    }

    private fun timeToDouble(hour: Int, min: Int): Double{
        return hour + min/60.0
    }
    private fun withinRange(time: Double, minInterval: Double, maxInterval: Double): Boolean{
        return time in minInterval..maxInterval
    }

}




