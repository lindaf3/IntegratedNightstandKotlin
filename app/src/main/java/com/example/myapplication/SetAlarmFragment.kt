package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

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
        val times = context?.getSharedPreferences("times", Context.MODE_PRIVATE)
        var startHour : Int = times?.getInt("start hour", -1) ?: -1
        var startMinute: Int = times?.getInt("start minute", -1) ?: -1
        var endHour: Int = times?.getInt("end hour", -1) ?: -1
        var endMinute :Int = times?.getInt("end minute", -1) ?: -1
        var timeStart = times?.getString("start time", "Alarm Start") ?: "Alarm Start"
        var timeEnd = times?.getString("end time", "Alarm End") ?: "Alarm End"
        if (validAlarm(startHour, startMinute, endHour, endMinute)){
            alarmStart.text = timeStart
            alarmEnd.text = timeEnd
            Toast.makeText(requireActivity().application, "Alarm set based on saved data", Toast.LENGTH_SHORT).show()
        }
        else{
            alarmStart.text = "Alarm Start"
            alarmEnd.text = "Alarm End"
            startHour = -1
            endHour = -1
            startMinute = -1
            endMinute = -1
            saveTimes(startHour, startMinute, endHour,endMinute, "Alarm Start", "Alarm End")
            Toast.makeText(requireActivity().application, "There was no saved data or saved data had invalid alarm; please try again.", Toast.LENGTH_SHORT).show()
        }

        clear.setOnClickListener{
            alarmStart.text = "Alarm Start"
            alarmEnd.text = "Alarm End"
            startHour = -1
            endHour = -1
            startMinute = -1
            endMinute = -1
            saveTimes(startHour, startMinute, endHour,endMinute, "Alarm Start", "Alarm End")
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
                setAlarm(startTimePicker, alarmStart)
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
                setAlarm(endTimePicker, alarmEnd)
            }
        }
        alarmSet.setOnClickListener{
            if(validAlarm(startHour, startMinute, endHour, endMinute)){
                timeStart = alarmStart.text as String
                timeEnd = alarmEnd.text as String
                saveTimes(startHour, startMinute, endHour,endMinute, timeStart, timeEnd)
                Toast.makeText(requireActivity().application , "Alarm set!", Toast.LENGTH_SHORT).show()
            }
            else{
                alarmStart.text = "Alarm Start"
                alarmEnd.text = "Alarm End"
                startHour = -1
                endHour = -1
                startMinute = -1
                endMinute = -1
                saveTimes(startHour, startMinute, endHour,endMinute, "Alarm Start", "Alarm End")
                Toast.makeText(requireActivity().application, "Invalid alarm, please try again with a valid one hour interval.", Toast.LENGTH_SHORT).show()
            }

        }


    }

    private fun saveTimes(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int, timeStart: String, timeEnd: String) {
        val times = context?.getSharedPreferences("times", Context.MODE_PRIVATE)
        val edit = times?.edit()
        edit?.apply {
            putInt("start hour", startHour)
            putInt("start minute", startMinute)
            putInt("end hour", endHour)
            putInt("end minute", endMinute)
            putString("start time", timeStart)
            putString("end time", timeEnd)
        }?.apply()
    }

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

    private fun setAlarm( picker: MaterialTimePicker, btn: Button) {
        btn.text = AlarmClock.getAlarmTime(picker.hour, picker.minute)
    }


}