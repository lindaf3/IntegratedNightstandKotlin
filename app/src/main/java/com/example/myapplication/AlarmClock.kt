package com.example.myapplication

object AlarmClock {
    fun  getAlarmTime(h: Int, min: Int):String {
        var hour = h
        var am_pm = "AM"
        if(hour > 12){
            hour -= 12
            am_pm = "PM"
        }
        else if(hour == 0){
            hour = 12
        }
        return String.format("%02d:%02d %s", hour, min, am_pm)
    }
    fun notInitiated(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean{
        return startHour == -1 ||startMinute == -1 || endHour == -1 || endMinute == -1
    }
}