package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val alarmStart: Button = findViewById(R.id.alarmStart)
        val alarmEnd: Button = findViewById(R.id.alarmEnd)
        val alarmSet: Button = findViewById(R.id.alarmSetter)

        alarmStart.setOnClickListener{
            val startTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTitleText("Select Alarm Start")
                .setHour(12)
                .setMinute(10)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()
            startTimePicker.show(supportFragmentManager, "MainActivity")
        }
        alarmEnd.setOnClickListener{
            val endTimePicker: MaterialTimePicker = MaterialTimePicker.Builder()
                .setTitleText("Select Alarm End")
                .setHour(12)
                .setMinute(10)
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .build()
            endTimePicker.show(supportFragmentManager, "MainActivity")
        }




    }
}


