package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val alarmStart: Button = view.findViewById(R.id.alarmStart)
        val alarmEnd: Button = view.findViewById(R.id.alarmEnd)
        val alarmSet: Button = view.findViewById(R.id.alarmSetter)
        var startHour : Int = -1
        var startMinute: Int = -1
        var endHour: Int = -1
        var endMinute :Int = -1

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
                Toast.makeText(requireActivity().application , "Alarm set!", Toast.LENGTH_SHORT).show()
                val bundle = Bundle()
                bundle.putInt("start hour", startHour)
                bundle.putInt("start minute", startMinute)
                bundle.putInt("end hour", endHour)
                bundle.putInt("end minute", endMinute)
                setFragmentResult("requestKey", bundle)

            }
            else{
                Toast.makeText(requireActivity().application, "Invalid alarm, please try again.", Toast.LENGTH_SHORT).show()
                alarmStart.text = "Alarm Start"
                alarmEnd.text = "Alarm End"
                startHour = -1
                endHour = -1
                startMinute = -1
                endHour = -1
            }

        }
    }


    private fun validAlarm(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean {
        return if(startHour == -1 ||  endHour == -1 || startMinute == -1 || endMinute == -1 ){
            return false
        }
        else if(startHour == endHour){
            endMinute >= startMinute
        } else if(startHour < endHour){
            true
        } else endHour == 0

    }

    private fun setAlarm( picker: MaterialTimePicker, btn: Button) {
        var hour: Int = picker.hour
        var am_pm = "AM"
        if(hour > 12){
            hour -= 12
            am_pm = "PM"
        }
        else if(hour == 0){
            hour = 12
        }
        btn.text = String.format("%02d:%02d %s", hour, picker.minute, am_pm)
    }



}