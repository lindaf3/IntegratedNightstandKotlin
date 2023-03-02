package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Button
import androidx.annotation.RequiresApi
import com.google.android.material.timepicker.MaterialTimePicker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter

object AlarmClock {
    fun  getAlarmTime(h: Int, min: Int, returnText: String):String {
        return if(notInitiatedClock(h, min)){
            returnText
        } else{
            var hour = h
            var ampm = "AM"
            if(hour > 12){
                hour -= 12
                ampm = "PM"
            } else if(hour == 0){
                hour = 12
            }
            String.format("%02d:%02d %s", hour, min, ampm)
        }

    }
    fun notInitiated(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean{
        return startHour == -1 ||startMinute == -1 || endHour == -1 || endMinute == -1
    }
    fun notInitiatedClock(h: Int, m: Int): Boolean {
        return h == -1 || m == -1
    }
    fun setAlarm(picker: MaterialTimePicker, btn: Button, returnText: String) {
        btn.text = getAlarmTime(picker.hour, picker.minute, returnText)
    }
}

object Date {
    fun setDate(sesDate: String, btn: Button) {
        btn.text = sesDate
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun parseDate(date: String): LocalDate {
        val commaSplit = date.split(",")
        val spaceSplit = commaSplit[0].split(" ")
        val month: Month = getMonth(spaceSplit[0])
        val day: Int = spaceSplit[1].toInt()
        val year:Int = commaSplit[1].trim().toInt()
        return LocalDate.of(year, month, day)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonth(m: String): Month {
        return when(m){
            "Jan" -> return Month.JANUARY
            "Feb" -> return Month.FEBRUARY
            "Mar" -> return Month.MARCH
            "Apr" -> return Month.APRIL
            "May" -> return Month.MAY
            "Jun" -> return Month.JUNE
            "Jul" -> return Month.JULY
            "Aug" -> return Month.AUGUST
            "Sep" -> return Month.SEPTEMBER
            "Oct" -> return Month.OCTOBER
            "Nov" -> return Month.NOVEMBER
            "Dec" -> return Month.DECEMBER
            else -> throw Exception(m)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun futureDate(date: LocalDate): Boolean{
        return date > LocalDate.now()
    }
}