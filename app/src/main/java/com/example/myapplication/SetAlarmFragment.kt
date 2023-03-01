package com.example.myapplication

import CloudClient
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.myapplication.AlarmClock.setAlarm
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*


private const val ALARM_START = "Alarm Start"
private const val ALARM_END = "Alarm End"
private const val UNINITIALIZED = -1
private val NO_DATETIME_STRING = null
private const val CLEAR = "clear"

class SetAlarmFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_set_alarm, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val alarmStart: Button = view.findViewById(R.id.alarmStart)
        val alarmEnd: Button = view.findViewById(R.id.alarmEnd)
        val alarmSet: Button = view.findViewById(R.id.alarmSetter)
        val clear: Button = view.findViewById(R.id.clear)
        var startHour : Int = UNINITIALIZED
        var startMinute: Int = UNINITIALIZED
        var endHour: Int = UNINITIALIZED
        var endMinute :Int = UNINITIALIZED

        fun revert(){
            alarmStart.text = ALARM_START
            alarmEnd.text = ALARM_END
            startHour = UNINITIALIZED
            endHour = UNINITIALIZED
            startMinute = UNINITIALIZED
            endMinute = UNINITIALIZED
        }

        fun setAlarmText(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int){
            alarmStart.text = if(AlarmClock.notInitiatedClock(startHour, startMinute)){
                ALARM_START
            } else{
                AlarmClock.getAlarmTime(startHour, startMinute, ALARM_START)
            }
            alarmEnd.text = if(AlarmClock.notInitiatedClock(endHour, endMinute)){
                ALARM_END
            } else{
                AlarmClock.getAlarmTime(endHour, endMinute, ALARM_END)
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun queryTimes(): Boolean{
            var saved = true
            val cloudClient = CloudClient("/path/key.pem", "longAPIToken")
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
                saved = false
            }
            setAlarmText(startHour, startMinute, endHour, endMinute)
            return saved

        }
        @RequiresApi(Build.VERSION_CODES.O)
        fun updateTimes(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int){
            val cloudClient = CloudClient("/path/key.pem", "longAPIToken")
            if(AlarmClock.notInitiated(startHour,startMinute, endHour, endMinute)){
                cloudClient.updateInterval(CLEAR, CLEAR)
            }
            else{
                val date = LocalDate.now()
                val startTime = LocalTime.of(startHour, startMinute)
                val endTime = LocalTime.of(endHour, endMinute)
                val startDateTime = LocalDateTime.of(date, startTime)
                val endDatetime = LocalDateTime.of(date, endTime)
                val from: String = startDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
                val to: String = endDatetime.format(DateTimeFormatter.ISO_DATE_TIME)
                cloudClient.updateInterval(from, to)
            }

        }

        val successfulQuery = queryTimes()

        if(successfulQuery){
            Toast.makeText(requireActivity().application, "Alarm set based on saved data", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(requireActivity().application, "No saved alarm data available", Toast.LENGTH_SHORT).show()
        }


//        activity?.runOnUiThread(Runnable{
//            Timer().scheduleAtFixedRate(object: TimerTask() {
//                override fun run(){
//                    queryTimes()
//                }
//            }, 0, 10000)
//
//        })



        clear.setOnClickListener{
            revert()
            //saveTimes(startHour, startMinute, endHour,endMinute)
            Toast.makeText(requireActivity().application, "Alarm Cleared!", Toast.LENGTH_SHORT).show()
        }


        alarmStart.setOnClickListener{
            val startTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTitleText("Select Alarm Start")
                .setHour(7)
                .setMinute(0)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()
            startTimePicker.show(requireActivity().supportFragmentManager, "start")
            startTimePicker.addOnPositiveButtonClickListener {
                startHour = startTimePicker.hour
                startMinute = startTimePicker.minute
                setAlarm(startTimePicker, alarmStart, ALARM_START)
            }

        }
        alarmEnd.setOnClickListener{
            val endTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTitleText("Select Alarm End")
                .setHour(7)
                .setMinute(30)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()
            endTimePicker.show(requireActivity().supportFragmentManager, "end")
            endTimePicker.addOnPositiveButtonClickListener {
                endHour = endTimePicker.hour
                endMinute = endTimePicker.minute
                setAlarm(endTimePicker, alarmEnd, ALARM_END)
            }
        }
        alarmSet.setOnClickListener{
            if(validAlarm(startHour, startMinute, endHour, endMinute)){
//                saveTimes(startHour, startMinute, endHour,endMinute)
                updateTimes(startHour, startMinute, endHour, endMinute)
                Toast.makeText(requireActivity().application , "Alarm set!", Toast.LENGTH_SHORT).show()
            }
            else{
                revert()
//                saveTimes(startHour, startMinute, endHour,endMinute)
                Toast.makeText(requireActivity().application, "Invalid alarm, please try again with a valid one hour interval.", Toast.LENGTH_SHORT).show()
            }

        }


    }

//    private fun saveTimes(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
//        val times = context?.getSharedPreferences("times", Context.MODE_PRIVATE)
//        val edit = times?.edit()
//        edit?.apply {
//            putInt("start hour", startHour)
//            putInt("start minute", startMinute)
//            putInt("end hour", endHour)
//            putInt("end minute", endMinute)
//        }?.apply()
//    }

// 1 hour intervals allowed max
    private fun validAlarm(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean {
        if(AlarmClock.notInitiated(startHour, startMinute, endHour, endMinute)){
           return false
        }
        else if(endHour == 0){
            return (startHour == 0 && endMinute >= startMinute) || (startHour == 23 && endMinute <= startMinute)
        }
        else if(endHour - startHour > 1){
            return false
        }
        else if(endHour - startHour == 1){
            return endMinute <= startMinute
        }
        else if (endHour == startHour){
            return endMinute >= startMinute
        }
        else return false
    }





}